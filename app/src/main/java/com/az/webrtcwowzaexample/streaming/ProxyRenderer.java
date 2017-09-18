package com.az.webrtcwowzaexample.streaming;

import org.webrtc.Logging;
import org.webrtc.VideoFrame;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRenderer.Callbacks;
import org.webrtc.VideoRenderer.I420Frame;

import java.nio.ByteBuffer;

/**
 * Created by zorin.a on 18.09.2017.
 */

public class ProxyRenderer implements Callbacks {

    private String TAG = "APP_RTC_Client.ProxyRenderer";
    private VideoRenderer.Callbacks target;

    public ProxyRenderer(String name) {
        TAG += " " + name;
    }

    @Override
    public void renderFrame(I420Frame frame) {
        if (target == null) {
            Logging.d(TAG, "Dropping frame in proxy because target is null.");
            VideoRenderer.renderFrameDone(frame);
            return;
        }
//        int rotW = frame.rotatedWidth();
//        int rotH = frame.rotatedHeight();
//
//
//        final int width = frame.width;
//        final int height = frame.height;
//        int deg = frame.rotationDegree;
//        final int[] yuvStrides = frame.yuvStrides;
//        ByteBuffer[] planes = frame.yuvPlanes;
//        deg += 90;
//
//        Frame1 newFrame1 = new Frame1(width, height, deg, yuvStrides, planes, 0);
//        Logging.d(TAG, "rotatedWidth: " + rotW + " rotatedHeight " + rotH);
//        target.renderFrame(newFrame1);
        target.renderFrame(frame);
    }

    synchronized public void setTarget(VideoRenderer.Callbacks target) {
        this.target = target;
    }


    class Frame1 extends I420Frame {

        public Frame1(int width, int height, int rotationDegree, int[] yuvStrides, ByteBuffer[] yuvPlanes, long nativeFramePointer) {
            super(width, height, rotationDegree, yuvStrides, yuvPlanes, nativeFramePointer);
        }

        public Frame1(int width, int height, int rotationDegree, int textureId, float[] samplingMatrix, long nativeFramePointer) {
            super(width, height, rotationDegree, textureId, samplingMatrix, nativeFramePointer);
        }
    }
}
