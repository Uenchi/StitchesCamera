package com.uenchi.editlibrary.filter;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;

import com.uenchi.editlibrary.R;

public class GlShakeFilter extends GlFilter {

    float mScale = 0f;
    float mOffset = 0f;
    private int mScaleHandle;
    private boolean plus = false;

    public GlShakeFilter(Context context) {
        super(context, R.raw.def_vertext, R.raw.fragment_shake);
    }

    @Override
    public void initProgram() {
        super.initProgram();
        mScaleHandle = GLES30.glGetUniformLocation(program, "scale");
    }

    @Override
    public void onDraw() {
        mScale = 1.0f + 0.3f * getInterpolation(mOffset);
        mOffset += 0.06f;
        if (mOffset > 1.0f) {
            mOffset = 0.0f;
        }
        GLES20.glUniform1f(mScaleHandle, mScale);

    }

    private float getInterpolation(float input) {
        return (float) (Math.cos((input + 1) * Math.PI) / 2.0f) + 0.5f;
    }

}

