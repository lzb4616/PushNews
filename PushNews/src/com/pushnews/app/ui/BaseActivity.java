package com.pushnews.app.ui;

import com.pushnews.app.cofig.UserInfoManager;

import android.app.Activity;
import android.os.Bundle;

public class BaseActivity extends Activity {
			@Override
			protected void onCreate(Bundle savedInstanceState) {
				// TODO Auto-generated method stub
				super.onCreate(savedInstanceState);
				UserInfoManager.context = this;
			}
}
