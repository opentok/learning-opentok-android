package com.tokbox.android.demo.learningopentok;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.view.View;

import com.opentok.android.BaseVideoCapturer;

public class ScreensharingCapturer extends BaseVideoCapturer {
    private static final int FPS = 15;

    private boolean mCapturerHasStarted;
    private boolean mCapturerIsPaused;
    private CaptureSettings mCapturerSettings;
    private View mContentView;
    private int mWidth;
    private int mHeight;
    private Handler mFrameProducerHandler;
    private long mFrameProducerIntervalMillis = 1000 / FPS;

    private int[] frameBuffer;

    private Runnable mFrameProducer = new Runnable() {
        @Override
        public void run() {
            int width = mContentView.getWidth();
            int height = mContentView.getHeight();

            if (frameBuffer == null || mWidth != width || mHeight != height) {
                mWidth = width;
                mHeight = height;
                frameBuffer = new int[mWidth * mHeight];
            }

            mContentView.setDrawingCacheEnabled(true);
            mContentView.buildDrawingCache();
            Bitmap bmp = mContentView.getDrawingCache();
            if (bmp != null) {
                bmp.getPixels(frameBuffer, 0, width, 0, 0, width, height);
                mContentView.setDrawingCacheEnabled(false);
                provideIntArrayFrame(frameBuffer, ARGB, width, height, 0, false);
            }

            if (mCapturerHasStarted && !mCapturerIsPaused) {
                mFrameProducerHandler.postDelayed(mFrameProducer, mFrameProducerIntervalMillis);
            }
        }
    };

    public ScreensharingCapturer(View view) {
        mContentView = view;

        mFrameProducerHandler = new Handler();
    }

    @Override
    public void init() {
        mCapturerHasStarted = false;
        mCapturerIsPaused = false;

        mCapturerSettings = new CaptureSettings();
        mCapturerSettings.fps = FPS;
        mCapturerSettings.width = mWidth;
        mCapturerSettings.height = mHeight;
        mCapturerSettings.format = BaseVideoCapturer.ARGB;
    }

    @Override
    public int startCapture() {
        mCapturerHasStarted = true;
        mFrameProducerHandler.postDelayed(mFrameProducer, mFrameProducerIntervalMillis);
        return 0;
    }

    @Override
    public int stopCapture() {
        mCapturerHasStarted = false;
        mFrameProducerHandler.removeCallbacks(mFrameProducer);
        return 0;
    }

    @Override
    public boolean isCaptureStarted() {
        return mCapturerHasStarted;
    }

    @Override
    public CaptureSettings getCaptureSettings() {
        return mCapturerSettings;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void onPause() {
        mCapturerIsPaused = true;

    }

    @Override
    public void onResume() {
        mCapturerIsPaused = false;
        mFrameProducerHandler.postDelayed(mFrameProducer, mFrameProducerIntervalMillis);
    }

}