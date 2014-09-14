package com.pushnews.app.ui;

import com.pushnews.app.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

/**
 * @author LZB
 * 
 * @see 推送消息列表界面
 * 
 * 
 * */
public class PushNewListActivity extends BaseActivity {

	private static ImageButton callBackBt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		/* 设置全屏 */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.pushnews_list);

		callBackBt = (ImageButton) findViewById(R.id.pushnews_list_bn);

		callBackBt.setOnClickListener(new OnClickListener() {
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
		Intent intent = new Intent(PushNewListActivity.this,
				NewsMainActivity.class);
		startActivity(intent);
		finish();
	}
}
