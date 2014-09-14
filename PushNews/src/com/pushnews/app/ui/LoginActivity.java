package com.pushnews.app.ui;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Observable;
import java.util.Observer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.pushnews.app.R;
import com.pushnews.app.cofig.Urls;
import com.pushnews.app.model.User;
import com.pushnews.app.observable.MyObserver;
import com.pushnews.app.servies.PushService;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class LoginActivity extends BaseActivity implements Observer {

	/**
	 * 登录界面返回按钮
	 */
	private ImageButton login_backButton;
	private Button login_btn;
	private Button register_btn;
	private HttpClient client = new DefaultHttpClient();
	private EditText userName_et;
	private EditText userPassword_et;

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MyObserver.LOGINSUCCESS:
				Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_LONG)
						.show();
				goBack();
				break;
			case MyObserver.LOGINFAILE:
				Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_LONG)
						.show();
				break;
			}
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/* 设置全屏 */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login);
		login_btn = (Button) findViewById(R.id.loginButton);
		register_btn = (Button) findViewById(R.id.registerButton);
		userName_et = (EditText) findViewById(R.id.login_username);
		userPassword_et = (EditText) findViewById(R.id.login_userpasssword);

		login_btn.setOnClickListener(new ButtonClickListner());
		register_btn.setOnClickListener(new ButtonClickListner());

		login_backButton = (ImageButton) findViewById(R.id.login_back_bn);
		login_backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				goBack();

			}
		});
		MyObserver.getObserver().addObserver(this);
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
		Intent intent = new Intent(LoginActivity.this, NewsMainActivity.class);
		startActivity(intent);
		finish();
	}

	public User login(String url, String username, String password) {
		HttpPost hp = new HttpPost(url);
		HttpResponse hr;
		User u = null;
		JSONObject json = new JSONObject();
		hp.setHeader("Content-Type", "application/json");
		HttpEntity entity;
		try {
			json.put("username", username);
			json.put("password", password);
			System.out.println("====================" + json.toString());
			entity = new StringEntity(json.toString(), HTTP.UTF_8);
			hp.setEntity(entity);
			hr = client.execute(hp);
			if (hr.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				entity = hr.getEntity();
				String content = EntityUtils.toString(entity, HTTP.UTF_8);
				JSONObject jsonObject = new JSONObject(content);
				u = new User();
				u.setUsername(jsonObject.getString("username"));
				u.setPassword(password);
				/* 其它需要的属性待议定后在添加，有哪些是必要的请告知我 */
				System.out.println("Login Success!");
				return u;
			} else {
				System.out.println("Login Fail!");
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 注册用户 （其中各种信息看有必要就修改一下）
	 * 
	 * @param url
	 * @param username
	 * @param password
	 * @return
	 */
	public User register(String url, String username, String password) {
		HttpPost hp = new HttpPost(url);
		HttpResponse hr;
		User u = null;
		JSONObject json = new JSONObject();
		hp.setHeader("Content-Type", "application/json");
		HttpEntity entity;
		try {
			try {
				json.put("username", username);
				json.put("password", password);
				json.put("nickname", "昵称");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			entity = new StringEntity(json.toString(), HTTP.UTF_8);
			hp.setEntity(entity);
			hr = client.execute(hp);
			if (hr.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				entity = hr.getEntity();
				String content = EntityUtils.toString(entity, HTTP.UTF_8);
				u = new User();
				try {
					JSONObject object = new JSONObject(content);
					u.setUsername(object.getString("username"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				u.setPassword(password);
				/* 其它需要的属性待议定后在添加，有哪些是必要的请告知我 */
				System.out.println("Register Success!");
				return u;
			} else {
				System.out.println("Register Fail!");
				return null;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public class ButtonClickListner implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.loginButton:
				PushService.login(userName_et.getText().toString(),
						userPassword_et.getText().toString());
				// @Override
				// protected void onPostExecute(Void result) {
				// if (result != null) {
				// Toast.makeText(LoginActivity.this, "登录成功",
				// Toast.LENGTH_SHORT).show();
				// } else {
				// Toast.makeText(LoginActivity.this, "登录失败",
				// Toast.LENGTH_SHORT).show();
				// }
				// super.onPostExecute(result);
				// }

				break;

			case R.id.registerButton:
				System.out.println("开始注册");
				new AsyncTask<String, Void, User>() {

					@Override
					protected User doInBackground(String... params) {
						return register(params[0], params[1], params[2]);
					}

					@Override
					protected void onPostExecute(User result) {
						if (result != null) {
							Toast.makeText(LoginActivity.this, "注册成功",
									Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(LoginActivity.this, "注册失败",
									Toast.LENGTH_SHORT).show();
						}
						super.onPostExecute(result);
					}

				}.execute(Urls.RegisterUrl, userName_et.getText().toString(),
						userPassword_et.getText().toString());
				break;

			default:
				break;
			}
		}
	}

	@Override
	public void update(Observable observable, Object data) {
		if (data instanceof Integer) {
			int state = (Integer) data;
			switch (state) {
			case MyObserver.LOGINFAILE:
				handler.sendEmptyMessage(MyObserver.LOGINFAILE);
				break;
			case MyObserver.LOGINSUCCESS:
				handler.sendEmptyMessage(MyObserver.LOGINSUCCESS);
				break;
			}
		}
	}

}