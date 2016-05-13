package com.tokbox.android.demo.learningopentok;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.view.View;

import com.opentok.android.BaseVideoRenderer;

import java.nio.ByteBuffer;

public class BlackWhiteVideoRender extends BaseVideoRenderer {

    GLSurfaceView mRenderView;
    GLRendererHelper mRenderer;

    public BlackWhiteVideoRender(Context context) {
        mRenderView = new GLSurfaceView(context);
        mRenderView.setEGLContextClientVersion(2);

        mRenderer = new GLRendererHelper();
        mRenderView.setRenderer(mRenderer);

        mRenderView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void onFrame(Frame frame) {
        ByteBuffer imageBuffer = frame.getBuffer();

        // Image buffer is represented using three planes, Y, U and V.
        // Data is laid out in a linear way in the imageBuffer variable
        // Y plane is first, and its size is the same of the image (width * height)
        // U and V planes are next, in order to produce a B&W image, we set both
        // planes with the same value.

        int startU = frame.getWidth() * frame.getHeight();
        for (int i = startU; i < imageBuffer.capacity(); i++) {
            imageBuffer.put(i, (byte)-127);
        }

        mRenderer.displayFrame(frame);
        mRenderView.requestRender();
    }

    @Override
    public void setStyle(String key, String value) {
        if (BaseVideoRenderer.STYLE_VIDEO_SCALE.equals(key)) {
            if (BaseVideoRenderer.STYLE_VIDEO_FIT.equals(value)) {
                mRenderer.enableVideoFit(true);
            } else if (BaseVideoRenderer.STYLE_VIDEO_FILL.equals(value)) {
                mRenderer.enableVideoFit(false);
            }
        }
    }

    @Override
    public void onVideoPropertiesChanged(boolean videoEnabled) {
        mRenderer.disableVideo(!videoEnabled);
    }

    @Override
    public View getView() {
        return mRenderView;
    }

    @Override
    public void onPause() {
        mRenderView.onPause();
    }

    @Override
    public void onResume() {
        mRenderView.onResume();
    }
}
