package com.android.playh264;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class CustomRelativeLayout extends RelativeLayout {
	private DispatchTouchCallback mDispatchTouchCallback;
	public CustomRelativeLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public CustomRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomRelativeLayout(Context context) {
		super(context);
	}
	
	
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if(mDispatchTouchCallback != null){
			mDispatchTouchCallback.notifyDispatch(ev);
		}
		return super.dispatchTouchEvent(ev);
	}
	
	public void setDispatchTouchCallback(DispatchTouchCallback callback){
		mDispatchTouchCallback = callback;
	}
	
	public static interface DispatchTouchCallback{
		void notifyDispatch(MotionEvent ev);
	}
}
