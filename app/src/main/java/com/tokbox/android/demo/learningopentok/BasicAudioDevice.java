package com.tokbox.android.demo.learningopentok;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;

import com.opentok.android.BaseAudioDevice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private ByteBuffer mRendererBuffer;
    private File mRendererFile;

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

    private Handler mRendererHandler;
    private long mRendererIntervalMillis = 1000;
    private Runnable mRenderer = new Runnable() {
        @Override
        public void run() {
            mRendererBuffer.clear();
            getAudioBus().readRenderData(mRendererBuffer, SAMPLING_RATE);
            try {
                FileOutputStream stream = new FileOutputStream(mRendererFile);
                stream.write(mRendererBuffer.array());
                stream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (mRendererStarted && !mAudioDriverPaused) {
                mRendererHandler.postDelayed(mRenderer, mRendererIntervalMillis);
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
        mRendererHandler = new Handler();
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
        return true;
    }

    @Override
    public boolean initRenderer() {
        mRendererBuffer = ByteBuffer.allocateDirect(SAMPLING_RATE * 2); // Each sample has 2 bytes
        mRendererFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                   , "output.raw");
        if (!mRendererFile.exists()) {
            try {
                mRendererFile.getParentFile().mkdirs();
                mRendererFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public boolean startRenderer() {
        mRendererStarted = true;
        mRendererHandler.postDelayed(mRenderer, mRendererIntervalMillis);
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
        mRendererHandler.removeCallbacks(mRenderer);
    }

    @Override
    public void onResume() {
        mAudioDriverPaused = false;
        if (mCapturerStarted) {
            mCapturerHandler.postDelayed(mCapturer, mCapturerIntervalMillis);
        }
        if (mRendererStarted) {
            mRendererHandler.postDelayed(mRenderer, mRendererIntervalMillis);
        }
    }
}
