package com.pushnews.app.ui;

import com.pushnews.app.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * @author LZB
 * 
 * @see 意见反馈界面
 * 
 * */
public class SuggestionFeedbackActivity extends BaseActivity {
	/** 返回按钮 */
	private static ImageButton callBackBt;
	/** 发送按钮 */
	private static Button sendBt;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/* 设置全屏 */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.suggestion_feedback);
		callBackBt = (ImageButton) findViewById(R.id.suggestion_feedback_ibt_back);
		sendBt = (Button) findViewById(R.id.suggestion_feedback_bt);
		sendBt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(SuggestionFeedbackActivity.this, "成功发送反馈信息",
						Toast.LENGTH_SHORT).show();
				
			}
		});
		callBackBt.setOnClickListener(new OnClickListener() {
			/** 返回至设置界面 */
			@Override
			public void onClick(View v) {

				goBack();
			}
		});
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
		Intent intent = new Intent(SuggestionFeedbackActivity.this,
				SystemSetActivity.class);
		startActivity(intent);
		finish();
	}
}
