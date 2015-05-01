package com.tokbox.android.demo.learningopentok;

import android.os.Handler;
import android.view.Surface;

import com.opentok.android.BaseVideoCapturer;

import java.util.Random;

public class NoiseVideoCapturer extends com.opentok.android.BaseVideoCapturer {
    private static final int FPS = 15;

    private boolean mCapturerHasStarted;
    private boolean mCapturerIsPaused;
    private CaptureSettings mCapturerSettings;
    private int mWidth;
    private int mHeight;

    private long mFrameProducerIntervalMillis = 1000 / FPS;
    private Handler mFrameProducerHandler;

    Runnable mFrameProducer = new Runnable() {
        @Override
        public void run() {
            Random random = new Random();
            byte[] buffer = new byte[mWidth * mHeight * 4];
            byte[] randoms = new byte[4];
            for (int i = 0; i < mWidth * mHeight * 4; i += 4) {
                random.nextBytes(randoms);
                buffer[i] = randoms[0];
                buffer[i + 1] = randoms[1];
                buffer[i + 2] = randoms[2];
                buffer[i + 3] = randoms[3];
            }

            provideByteArrayFrame(buffer, BaseVideoCapturer.ARGB,
                    mWidth, mHeight, Surface.ROTATION_0, false);

            if (mCapturerHasStarted && !mCapturerIsPaused) {
                mFrameProducerHandler.postDelayed(mFrameProducer, mFrameProducerIntervalMillis);
            }
        }
    };

    public NoiseVideoCapturer(int width, int height) {
        mWidth = width;
        mHeight = height;

        mFrameProducerHandler = new Handler();
    }

    @Override
    public void init() {
        mCapturerHasStarted = false;
        mCapturerIsPaused = false;

        mCapturerSettings = new CaptureSettings();
        mCapturerSettings.height = mHeight;
        mCapturerSettings.width = mWidth;
        mCapturerSettings.format = BaseVideoCapturer.ARGB;
        mCapturerSettings.fps = FPS;
        mCapturerSettings.expectedDelay = 0;
    }

    @Override
    public int startCapture() {
        mCapturerHasStarted = true;
        mFrameProducer.run();
        return 0;
    }

    @Override
    public int stopCapture() {
        mCapturerHasStarted = false;
        mFrameProducerHandler.removeCallbacks(mFrameProducer);
        return 0;
    }

    @Override
    public void destroy() {

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
    public void onPause() {
        mCapturerIsPaused = true;
    }

    @Override
    public void onResume() {
        mCapturerIsPaused = false;
        mFrameProducerHandler.postDelayed(mFrameProducer, mFrameProducerIntervalMillis);
    }
}
