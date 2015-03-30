package com.android.playh264;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;
import net.youmi.android.banner.AdViewListener;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.android.playh264.CustomRelativeLayout.DispatchTouchCallback;
import com.example.remotecamera.R;

public class SelectedActivity extends BaseActivity {
	private final static String TAG = "SelectedActivity";
	private LinearLayout mLinearLayoutAd;
	private ListView mListView;
	private ArrayList<File> mBackStack = new ArrayList<File>();
	private CustomRelativeLayout mCustomRelativeLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.selected_activity_layout);
		initView();
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mBackStack.size() > 1) {
				mBackStack.remove(mBackStack.size() - 1);
				File file = mBackStack.get(mBackStack.size() - 1);
				if (mListView != null && mListView.getAdapter() != null) {
					((CustomAdapter) mListView.getAdapter()).setData(file
							.getAbsolutePath());
				}
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void initView() {
		mListView = (ListView) findViewById(R.id.listview);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				File file = (File) arg0.getItemAtPosition(arg2);
				if (file != null && file.isDirectory()) {
					mBackStack.add(file);
					((CustomAdapter) arg0.getAdapter()).setData(file
							.getAbsolutePath());
				}
				if (file != null && file.isFile()
						&& file.getName().endsWith(".h264")) {
					Intent intent = new Intent();
					intent.setClass(SelectedActivity.this, ScreenActivity.class);
					intent.putExtra("playfile", file.getAbsolutePath());
					SelectedActivity.this.startActivity(intent);
				}
			}
		});
		File file = Environment.getExternalStorageDirectory();
		if (file != null && file.isDirectory()) {
			mBackStack.add(file);
			mListView.setAdapter(new CustomAdapter(file.getAbsolutePath()));
		} else {
			Toast.makeText(SelectedActivity.this, R.string.toast_no_sdcard,
					Toast.LENGTH_LONG);
		}

		mCustomRelativeLayout = (CustomRelativeLayout) findViewById(R.id.customlayout);
		mCustomRelativeLayout
				.setDispatchTouchCallback(new DispatchTouchCallback() {

					@Override
					public void notifyDispatch(MotionEvent ev) {
						MotionEvent event = MotionEvent.obtain(ev);
						Log.e(TAG, "ev:" + ev);
						float x = event.getX();
						float y = event.getY();
						if (mLinearLayoutAd != null) {
							if (y > mLinearLayoutAd.getTop()) {
								return;
							}

							LayoutParams lp = (LayoutParams) mLinearLayoutAd
									.getLayoutParams();
							y = y % lp.height;
							event.setLocation(x, y);
							mLinearLayoutAd.dispatchTouchEvent(event);
						}

					}
				});
		mLinearLayoutAd = (LinearLayout) findViewById(R.id.ad_linearlayout);

		AdView adView = new AdView(this, AdSize.FIT_SCREEN);

		mLinearLayoutAd.addView(adView);
		adView.setAdListener(new AdViewListener() {

			@Override
			public void onSwitchedAd(AdView arg0) {
				Log.i("AdDemo", "广告条切换");
			}

			@Override
			public void onReceivedAd(AdView arg0) {
				Log.i("AdDemo", "请求广告成功");

			}

			@Override
			public void onFailedToReceivedAd(AdView arg0) {
				Log.i("AdDemo", "请求广告失败");
			}
		});

	}

	private class CustomAdapter extends BaseAdapter {
		private String mDir;
		private ArrayList<File> mFileList = new ArrayList<File>();
		private LayoutInflater mLayoutInflater;

		public CustomAdapter(String dir) {
			mLayoutInflater = LayoutInflater.from(SelectedActivity.this);
			setData(dir);
		}

		public void setData(String dir) {
			if (dir == null) {
				Log.e("CustomAdapter", "setData dir == null");
				return;
			}
			File file = new File(dir);
			if (!file.exists() || file.isFile()) {
				Log.e("CustomAdapter", "setData dir is wrong");
				return;
			}
			mFileList.clear();
			mDir = dir;
			File[] files = file.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File arg0, String arg1) {
					if (arg1.startsWith(".")) {
						return false;
					}
					return true;
				}
			});
			if (files != null && files.length > 0) {
				for (File temp : files) {
					mFileList.add(temp);
				}
			}
			Collections.sort(mFileList, new Comparator<File>() {

				@Override
				public int compare(File arg0, File arg1) {
					String name0 = arg0.getName();
					String name1 = arg1.getName();
					if (arg1.isDirectory()) {
						if (!arg0.isDirectory()) {
							return -1;
						}
					}
					if (arg0.isDirectory()) {
						if (!arg1.isDirectory()) {
							return 1;
						}
					}

					return name0.compareTo(name1);
				}
			});
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mFileList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return mFileList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			ViewHolder holder;
			if (arg1 == null) {
				RelativeLayout rl = (RelativeLayout) mLayoutInflater.inflate(
						R.layout.list_item_selected_layout, null);
				holder = new ViewHolder();
				holder.mTitle = (TextView) rl.findViewById(R.id.title);
				rl.setTag(holder);
				arg1 = rl;
			}
			holder = (ViewHolder) arg1.getTag();
			holder.mTitle.setText(((File) getItem(arg0)).getName());
			return arg1;
		}

		private class ViewHolder {
			public TextView mTitle;
		}
	}
}
