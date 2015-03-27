package com.android.ffmpeglib;

public class H264Decoder {

	static 
	{
		System.loadLibrary("ffmpeglib");
	}
	
	public native boolean init();
	public native void release();
	public native boolean decode(byte [] in, byte [] out);
	public native int geth();
	public native int getw();
}
