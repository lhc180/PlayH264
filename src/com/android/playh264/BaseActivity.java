package com.android.playh264;

import android.app.Activity;

public class BaseActivity extends Activity {

	@Override
	protected void onResume() {
		BaiduStatisticController.onResume(this);

		super.onResume();
	}

	@Override
	protected void onPause() {
		
		BaiduStatisticController.onPause(this);
		super.onPause();
	}
}
