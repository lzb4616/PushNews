package com.pushnews.app.servies;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import com.pushnews.app.R;
import com.pushnews.app.cofig.Urls;
import com.pushnews.app.https.HttpUtils;
import com.pushnews.app.observable.MyObserver;
import com.pushnews.app.ui.LoginActivity;
import com.pushnews.app.ui.NewsDetailsActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

public class PushService extends Service implements Observer {

	private static PushClient mClient;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		MyObserver.getObserver().addObserver(this);
		super.onCreate();
	}

	private void init() {
		if(mClient!=null)
		{
			Toast.makeText(this, "上次退出失败",
					Toast.LENGTH_SHORT).show();
		}
		mClient = new PushClient(new InetSocketAddress(Urls.LocalIP, Urls.SocketAddressPort));
		mClient.start();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		init();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		if (mClient != null) {
			mClient.disConnect();
		}
		super.onDestroy();
	}

	/**
	 * 登录时。需要把相关的用户名和密码发送到服务器端
	 * 
	 * @param username
	 *            用户名
	 * @param password
	 *            密码
	 */
	public static void login(String username, String password) {

		String message = username + "," + password;
		new AsyncTask<String, Void, Void>() {

			@Override
			protected Void doInBackground(String... params) {
				try {
					mClient.sendMsg(params[0]);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}

		}.execute(message);

	}

	/**
	 * 通过newsid来获得相关内容，并把内容显示在notification
	 * 
	 * @param newsid
	 */
	private void notificationUtil(final String newsid) {
		// 解析新闻详细内容
		new AsyncTask<String, Void, String>() {
			
			@Override
			protected String doInBackground(String... params) {
				// 解析新闻详细信息的json
				String detialUrl = Urls.NewsDetailUrl + "?id=" + params[0];
				String json = HttpUtils
						.postByHttpURLConnection(detialUrl, null);
				return json;
			}
			@Override
			protected void onPostExecute(String result) {
				if (result == null) {
					return;
				}
				JSONObject jsonObject;
				try {
					jsonObject = new JSONObject(result);
					String mainTitle = jsonObject.getString("mainTitle");
					if (mainTitle == null || mainTitle.equals("")) {
						return;
					} else {
						String url = Urls.NewsDetailUrl + "?id=" + newsid;
						showNotification(mainTitle, url);
					}
				} catch (JSONException e) {
					e.printStackTrace();
					return;
				}
			}


		}.execute(newsid);

	}

	/**
	 * 把标题和相关内容显示在Notification
	 * 
	 * @param title
	 *            标题
	 * @param url
	 *            要跳转的url
	 */
	private void showNotification(String title, String url) {
		// 创建一个NotificationManager的引用
		NotificationManager notificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);

		// 定义Notification的各种属性
		Notification notification = new Notification(R.drawable.ic_launcher,
				"新闻应急管理系统", System.currentTimeMillis());

		// FLAG_AUTO_CANCEL 该通知能被状态栏的清除按钮给清除掉
		// FLAG_NO_CLEAR 该通知不能被状态栏的清除按钮给清除掉
		// FLAG_ONGOING_EVENT 通知放置在正在运行
		// FLAG_INSISTENT 是否一直进行，比如音乐一直播放，知道用户响应
		notification.flags |= Notification.FLAG_AUTO_CANCEL; // 表明在点击了通知栏中的"清除通知"后，此通知不清除，经常与FLAG_ONGOING_EVENT一起使用

		// DEFAULT_ALL 使用所有默认值，比如声音，震动，闪屏等等
		// DEFAULT_LIGHTS 使用默认闪光提示
		// DEFAULT_SOUND 使用默认提示声音
		// DEFAULT_VIBRATE 使用默认手机震动，需加上<uses-permission
		// android:name="android.permission.VIBRATE" />权限
		notification.defaults = Notification.DEFAULT_SOUND;

		// 设置通知的事件消息
		CharSequence contentTitle = title; // 通知栏标题
		CharSequence contentText = ""; // 通知栏内容
		Intent notificationIntent = new Intent(this, NewsDetailsActivity.class); // 点击该通知后要跳转的Activity
		Bundle bundle = new Bundle();
		bundle.putString("detialUrl", url);
		notificationIntent.putExtras(bundle);

		PendingIntent contentItent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(this, contentTitle, contentText,
				contentItent);
		// 把Notification传递给NotificationManager
		int id = new Random().nextInt(100000);
		notificationManager.notify(id, notification);
	}

	@Override
	public void update(Observable observable, Object data) {
		// 页面接收到服务器端发送过来的相关数据
		if (data instanceof String) {
			String newsid = (String) data;
			 notificationUtil(newsid);
		//	showNotification("4444444", "");
		}
	}

}
