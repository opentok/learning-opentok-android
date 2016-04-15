package com.tokbox.android.demo.learningopentok;

import android.content.Context;
import android.os.Handler;

import com.opentok.android.BaseAudioDevice;

import java.nio.ByteBuffer;
import java.util.Random;

public class BasicAudioDevice extends BaseAudioDevice {
    private final static int SAMPLING_RATE = 44100;
    private final static int NUM_CHANNELS_CAPTURING = 1;
    private final static int NUM_CHANNELS_RENDERING = 1;

    private Context mContext;

    private AudioSettings mCaptureSettings;
    private AudioSettings mRendererSettings;

    private boolean mCapturerStarted;
    private boolean mRendererStarted;

    private boolean mAudioDriverPaused;

    private ByteBuffer mCapturerBuffer;

    private Handler mCapturerHandler;
    private long mCapturerIntervalMillis = 1000;
    private Runnable mCapturer = new Runnable() {
        @Override
        public void run() {
            mCapturerBuffer.rewind();

            Random rand = new Random();
            rand.nextBytes(mCapturerBuffer.array());

            getAudioBus().writeCaptureData(mCapturerBuffer, SAMPLING_RATE);

            if(mCapturerStarted && !mAudioDriverPaused) {
                mCapturerHandler.postDelayed(mCapturer, mCapturerIntervalMillis);
            }
        }
    };

    public BasicAudioDevice(Context context) {
        mContext = context;

        mCaptureSettings = new AudioSettings(SAMPLING_RATE, NUM_CHANNELS_CAPTURING);
        mRendererSettings = new AudioSettings(SAMPLING_RATE, NUM_CHANNELS_RENDERING);

        mCapturerStarted = false;
        mRendererStarted = false;

        mAudioDriverPaused = false;

        mCapturerHandler = new Handler();
    }

    @Override
    public boolean initCapturer() {
        mCapturerBuffer = ByteBuffer.allocateDirect(SAMPLING_RATE * 2); // Each sample has 2 bytes
        return true;
    }

    @Override
    public boolean startCapturer() {
        mCapturerStarted = true;
        mCapturerHandler.postDelayed(mCapturer, mCapturerIntervalMillis);
        return true;
    }

    @Override
    public boolean stopCapturer() {
        mCapturerStarted = false;
        mCapturerHandler.removeCallbacks(mCapturer);
        return true;
    }

    @Override
    public boolean destroyCapturer() {
        mCapturerBuffer = null;
        return true;
    }

    @Override
    public boolean initRenderer() {
        return true;
    }

    @Override
    public boolean startRenderer() {
        mRendererStarted = true;
        return true;
    }

    @Override
    public boolean stopRenderer() {
        mRendererStarted = false;
        return true;
    }

    @Override
    public boolean destroyRenderer() {
        return true;
    }

    @Override
    public int getEstimatedCaptureDelay() {
        return 0;
    }

    @Override
    public int getEstimatedRenderDelay() {
        return 0;
    }

    @Override
    public AudioSettings getCaptureSettings() {
        return mCaptureSettings;
    }

    @Override
    public AudioSettings getRenderSettings() {
        return mRendererSettings;
    }

    @Override
    public void onPause() {
        mAudioDriverPaused = true;
        mCapturerHandler.removeCallbacks(mCapturer);
    }

    @Override
    public void onResume() {
        mAudioDriverPaused = false;
        if (mCapturerStarted) {
            mCapturerHandler.postDelayed(mCapturer, mCapturerIntervalMillis);
        }
    }
}
