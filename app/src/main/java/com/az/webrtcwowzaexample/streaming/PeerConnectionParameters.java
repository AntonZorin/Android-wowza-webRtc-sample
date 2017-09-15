package com.az.webrtcwowzaexample.streaming;

/**
 * Created by zorin.a on 12.09.2017.
 */

public class PeerConnectionParameters {
    public final int videoWidth;
    public final int videoHeight;
    public final int videoFps;
    public final int videoMaxBitrate;
    public final String videoCodec;
    public final boolean videoCodecHwAcceleration;
    public final int audioStartBitrate;
    public final String audioCodec;
    public final boolean enableLevelControl;
    public final boolean disableBuiltInAEC;
    public final boolean disableBuiltInAGC;
    public final boolean noAudioProcessing;
    public final boolean useOpenSLES;


    public PeerConnectionParameters(int videoWidth, int videoHeight, int videoFps, int videoMaxBitrate,
                                    String videoCodec, boolean videoCodecHwAcceleration, int audioStartBitrate,
                                    String audioCodec, boolean enableLevelControl, boolean disableBuiltInAEC,
                                    boolean disableBuiltInAGC, boolean noAudioProcessing, boolean useOpenSLES) {
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        this.videoFps = videoFps;
        this.videoMaxBitrate = videoMaxBitrate;
        this.videoCodec = videoCodec;
        this.videoCodecHwAcceleration = videoCodecHwAcceleration;
        this.audioStartBitrate = audioStartBitrate;
        this.audioCodec = audioCodec;
        this.enableLevelControl = enableLevelControl;
        this.disableBuiltInAEC = disableBuiltInAEC;
        this.disableBuiltInAGC = disableBuiltInAGC;
        this.noAudioProcessing = noAudioProcessing;
        this.useOpenSLES = useOpenSLES;
    }
}
