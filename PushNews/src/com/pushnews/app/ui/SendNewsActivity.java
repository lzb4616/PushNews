package com.pushnews.app.ui;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import com.pushnews.app.R;
import com.pushnews.app.cofig.Urls;
import com.pushnews.app.cofig.UserInfoManager;
import com.pushnews.app.model.News;
import com.pushnews.app.model.User;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * @author linyongan
 * 
 * @see 发送紧急新闻界面
 * 
 * */
public class SendNewsActivity extends BaseActivity {
	private static final String TAG = "SendNewsActivity";
	/** 返回按钮 */
	private static ImageButton callBackBt;
	/** 发送按钮 */
	private static Button sendBt;
	/** 新闻标题 */
	private static EditText news_title;
	/** 新闻内容 */
	private static EditText news_content;
	/** 新闻关键字 */
	private static EditText news_keyword;
	/** 新闻类型2 */
	private static Spinner news_type2;
	/** 新闻类型3 */
	private static Spinner news_type3;
	/** 新闻紧急程度 */
	private static Spinner news_degree;
	/** 新闻类型2适配器 */
	private static ArrayAdapter<CharSequence> news_type2SpinnerAdapter;
	/** 新闻类型3适配器 */
	private static ArrayAdapter<CharSequence> news_type3SpinnerAdapter;
	/** 新闻紧急程度适配器 */
	private static ArrayAdapter<CharSequence> news_degreeSpinnerAdapter;
	/** 发送新闻的地址 */
	private String url = Urls.SendnewsUrl;

	private HttpClient client = new DefaultHttpClient();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/* 设置全屏 */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.send_news);
		callBackBt = (ImageButton) findViewById(R.id.send_news_ibt_back);
		sendBt = (Button) findViewById(R.id.send_news_bt);

		news_title = (EditText) findViewById(R.id.send_news_title);
		news_content = (EditText) findViewById(R.id.send_news_content);
		news_keyword = (EditText) findViewById(R.id.send_news_keyword);

		news_type2 = (Spinner) findViewById(R.id.news_type2);
		news_type2SpinnerAdapter = ArrayAdapter.createFromResource(this,
				R.array.newstype2, android.R.layout.simple_spinner_item);
		news_type2SpinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		news_type2.setAdapter(news_type2SpinnerAdapter);

		news_type3 = (Spinner) findViewById(R.id.news_type3);
		news_type3SpinnerAdapter = ArrayAdapter.createFromResource(
				SendNewsActivity.this, R.array.newstype3,
				android.R.layout.simple_spinner_item);
		news_type3SpinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		news_type3.setAdapter(news_type3SpinnerAdapter);

		news_degree = (Spinner) findViewById(R.id.news_degree);
		news_degreeSpinnerAdapter = ArrayAdapter.createFromResource(
				SendNewsActivity.this, R.array.degree,
				android.R.layout.simple_spinner_item);
		news_degreeSpinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		news_degree.setAdapter(news_degreeSpinnerAdapter);

		sendBt.setOnClickListener(new ButtonActionListener());
		callBackBt.setOnClickListener(new ButtonActionListener());
	}

	private class ButtonActionListener implements OnClickListener {
		public void onClick(View v) {
			if (v.getId() == R.id.send_news_ibt_back) {
				goBack();
			}
			if (v.getId() == R.id.send_news_bt) {
				new AsyncTask<String, Void, Boolean>() {

					@Override
					protected Boolean doInBackground(String... params) {
						// TODO Auto-generated method stub
						return addNews(params[0]);
					}

					@Override
					protected void onPostExecute(Boolean result) {
						if (result) {
							Toast.makeText(SendNewsActivity.this, "成功发送紧急新闻",
									Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(SendNewsActivity.this, "发送紧急新闻失败",
									Toast.LENGTH_SHORT).show();
						}
						super.onPostExecute(result);
					}

				}.execute(url);

			}
		}
	}

	// 添加新闻
	public Boolean addNews(String url) {
		if (UserInfoManager.user == null) {
			return false;
		}
		HttpPost hp = new HttpPost(url);
		HttpResponse hr;
		hp.setHeader("Content-Type", "application/json");
		HttpEntity entity;
		String mainTitle = news_title.getText().toString();
		String content = news_content.getText().toString();
		String keyword = news_keyword.getText().toString();
		Integer degree = news_degree.getSelectedItemPosition() + 1;
		int type2 = news_type2.getSelectedItemPosition() + 4;
		int type3 = news_type3.getSelectedItemPosition() + 6;
		// User publisher = null;
		Log.v(TAG, mainTitle + "-" + content + "-" + keyword + "-" + degree
				+ "-" + type2 + "-" + type3);
		/* 在这里设置好news的属性值 */
		try {
			JSONObject json = new JSONObject();
			try {
				json.put("mainTitle", mainTitle);
				json.put("content", content);
				json.put("keyword", keyword);
				json.put("degree", degree);
				json.put("type", 13);
				json.put("type2", type2);
				json.put("type3", type3);
				json.put("publisher", UserInfoManager.user.getUid());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			entity = new StringEntity(json.toString(), HTTP.UTF_8);
			hp.setEntity(entity);
			hr = client.execute(hp);
			if (hr.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				return true;
			} else {
				return false;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

	}

	/** 退出按钮 */
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (event.getAction() == KeyEvent.ACTION_DOWN
					&& event.getRepeatCount() == 0) {
				goBack();
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	/** 返回上一个界面 */
	private void goBack() {
		Intent intent = new Intent(SendNewsActivity.this,
				NewsMainActivity.class);
		startActivity(intent);
		finish();
	}
}
