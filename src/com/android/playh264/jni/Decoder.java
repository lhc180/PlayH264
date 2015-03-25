package com.android.playh264.jni;

public class Decoder {

    static {
        System.loadLibrary("H264Android");
    }
    public native int InitDecoder(int width, int height);
    public native int UninitDecoder(); 
    public native int DecoderNal(byte[] in, int insize, byte[] out);
}
