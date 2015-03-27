package com.android.playh264;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;
import net.youmi.android.banner.AdViewListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.LinearLayout;

import com.android.ffmpeglib.H264Decoder;

public class ScreenActivity extends BaseActivity implements Callback {
	private final static String TAG = "ScreenActivity";
	private SurfaceView mSurfaceView;
	private WakeLock wl;
	boolean isPause = false;
	// 传输数据
	private H264Decoder mH264Android = null;

	private byte[] mPixel = null;
	private ByteBuffer mBuffer = null;
	private Bitmap mVideoBit = null;

	private SurfaceHolder mSurfaceHolder = null;
	private LinearLayout mLinearLayoutAd;

	private int mScreenW = 0;
	private int mScreenH = 0;
	private int mSurfaceW = 320;
	private int mSurfaceH = 240;

	private PlayThread mPlayThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.screen_activity_layout);
		initView();
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		isPause = false;
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "my_wakelock");
		wl.acquire();
		super.onResume();
	}

	@Override
	protected void onPause() {
		isPause = true;
		wl.release();
		wl = null;
		super.onPause();
	}

	private void initView() {
		mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
		mSurfaceView.getHolder().addCallback(this);
		mSurfaceView.setZOrderOnTop(true);
		Display display = getWindowManager().getDefaultDisplay();
		mScreenW = display.getWidth();
		mScreenH = display.getHeight();

		mLinearLayoutAd = (LinearLayout) findViewById(R.id.ad_linearlayout);

		AdView adView = new AdView(this, AdSize.FIT_SCREEN);

		mLinearLayoutAd.addView(adView);
		adView.setAdListener(new AdViewListener() {

			@Override
			public void onSwitchedAd(AdView arg0) {
				Log.i("YoumiAdDemo", "广告条切换");
			}

			@Override
			public void onReceivedAd(AdView arg0) {
				Log.i("YoumiAdDemo", "请求广告成功");

			}

			@Override
			public void onFailedToReceivedAd(AdView arg0) {
				Log.i("YoumiAdDemo", "请求广告失败");
			}
		});
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		Log.e(TAG, "surfaceChanged");

	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		Log.e(TAG, "surfaceCreated");
		mSurfaceHolder = arg0;
		mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
		if (mPlayThread == null) {
			mPlayThread = new PlayThread();
			mPlayThread.start();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		Log.e(TAG, "surfaceCreated");
		mSurfaceHolder = null;
	}

	private class PlayThread extends Thread {

		int mTrans = 0x0F0F0F0F;

		@Override
		public void run() {
			File file = new File("mnt/sdcard/outputfile.h264");
			InputStream fileIS = null;

			boolean iTemp = false;
			int nalLen;

			boolean bFirst = true;
			boolean bFindPPS = true;

			int bytesRead = 0;
			int NalBufUsed = 0;
			int SockBufUsed = 0;

			byte[] NalBuf = new byte[409800]; // 40k
			byte[] SockBuf = new byte[2048];

			try {
				fileIS = new FileInputStream(file);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			mH264Android = new H264Decoder();
			mH264Android.init();

			while (!Thread.currentThread().isInterrupted()) {
				try {
					bytesRead = fileIS.read(SockBuf, 0, 2048);
				} catch (IOException e) {
				}

				if (bytesRead <= 0)
					break;

				SockBufUsed = 0;

				while (bytesRead - SockBufUsed > 0) {
					nalLen = MergeBuffer(NalBuf, NalBufUsed, SockBuf,
							SockBufUsed, bytesRead - SockBufUsed);

					NalBufUsed += nalLen;
					SockBufUsed += nalLen;

					while (mTrans == 1) {
						mTrans = 0xFFFFFFFF;

						if (bFirst == true) // the first start flag
						{
							bFirst = false;
						} else // a complete NAL data, include 0x00000001 trail.
						{
							if (bFindPPS == true) // true
							{
								if ((NalBuf[4] & 0x1F) == 7) {
									bFindPPS = false;
								} else {
									NalBuf[0] = 0;
									NalBuf[1] = 0;
									NalBuf[2] = 0;
									NalBuf[3] = 1;

									NalBufUsed = 4;

									break;
								}
							}
							// decode nal
							byte[] buffer = new byte[NalBufUsed - 4];
							System.arraycopy(NalBuf, 0, buffer, 0,
									buffer.length);
							iTemp = mH264Android.decode(buffer, mPixel);

							if (iTemp && mBuffer != null) {
								mBuffer.mark();
								mVideoBit.copyPixelsFromBuffer(mBuffer);
								mBuffer.reset();
								if (mSurfaceHolder != null) {
									Canvas can = mSurfaceHolder.lockCanvas();
									if (can != null) {
										can.save();
										can.drawBitmap(mVideoBit, null,
												new Rect(0, 0, mSurfaceH,
														mSurfaceW), null);
										can.restore();
									}
									mSurfaceHolder.unlockCanvasAndPost(can);
								}
							} else {
								int h = mH264Android.geth();
								int w = mH264Android.getw();
								if (h > 0 && w > 0) {
									mPixel = new byte[w * h * 2];
									if (mBuffer != null) {
										mBuffer.clear();
										mBuffer = null;
									}
									mBuffer = ByteBuffer.wrap(mPixel);
									if (mVideoBit != null
											&& !mVideoBit.isRecycled()) {
										mVideoBit.recycle();
										mVideoBit = null;
									}
									mVideoBit = Bitmap.createBitmap(w, h,
											Config.RGB_565);

									mSurfaceW = mScreenW;
									mSurfaceH = (int) (mScreenW * (1.0f * w / h));
									if (mSurfaceH > mScreenH) {
										mSurfaceW = (int) (mScreenH * (1.0f * h / w));
										mSurfaceH = mScreenH;
									}
									new Handler(Looper.getMainLooper())
											.post(new Runnable() {

												@Override
												public void run() {

													mSurfaceHolder
															.setFixedSize(
																	mSurfaceW,
																	mSurfaceH);
												}
											});
								}
							}
						}

						NalBuf[0] = 0;
						NalBuf[1] = 0;
						NalBuf[2] = 0;
						NalBuf[3] = 1;

						NalBufUsed = 4;

						try {
							Thread.sleep(20);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
			try {
				if (fileIS != null)
					fileIS.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mH264Android.release();
		}

		int MergeBuffer(byte[] NalBuf, int NalBufUsed, byte[] SockBuf,
				int SockBufUsed, int SockRemain) {
			int i = 0;
			byte Temp;

			for (i = 0; i < SockRemain; i++) {
				Temp = SockBuf[i + SockBufUsed];
				NalBuf[i + NalBufUsed] = Temp;

				mTrans <<= 8;
				mTrans |= Temp;

				if (mTrans == 1) {
					i++;
					break;
				}
			}

			return i;
		}

	}
}
