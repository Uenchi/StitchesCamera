package com.uenchi.editlibrary.filter;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;

import com.uenchi.editlibrary.R;

public class GlFlashFilter extends GlFilter {


    private int mExposeHandle;
    private int mFrames;

    private int mMaxFrames = 8;

    private int mHalfFrames = mMaxFrames / 2;

    public GlFlashFilter(Context context) {
        super(context, R.raw.def_vertext, R.raw.fragment_flash);
    }

    @Override
    public void initProgram() {
        super.initProgram();
        mExposeHandle = GLES30.glGetUniformLocation(program, "uAdditionalColor");
    }

    @Override
    public void onDraw() {
        float progress;
        if (mFrames <= mHalfFrames) {
            progress = mFrames * 1.0f / mHalfFrames;
        } else {
            progress = 2.0f - mFrames * 1.0f / mHalfFrames;
        }
        mFrames++;
        if (mFrames > mMaxFrames) {
            mFrames = 0;
        }
        GLES20.glUniform1f(mExposeHandle, progress);


    }
}

