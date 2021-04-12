package com.uenchi.editlibrary.filter;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;

import com.uenchi.editlibrary.R;


public class GlScaleFilter extends GlFilter {

    float mScale = 0f;
    float mOffset = 0f;
    private int mScaleHandle;
    private boolean plus = false;

    public GlScaleFilter(Context context) {
        super(context, R.raw.def_vertext, R.raw.fragment_scale);
    }

    @Override
    public void initProgram() {
        super.initProgram();
        mScaleHandle = GLES30.glGetUniformLocation(program, "scale");
    }

    @Override
    public void onDraw() {
        mOffset += plus ? +0.06f : -0.06f;
        if (mOffset >= 1.0f) {
            plus = false;
        } else if (mOffset <= 0.0f) {
            plus = true;
        }
        mScale = 1.0f + 0.5f * getInterpolation(mOffset);
        GLES20.glUniform1f(mScaleHandle, mScale);


    }

    private float getInterpolation(float input) {
        return (float) (Math.cos((input + 1) * Math.PI) / 2.0f) + 0.5f;
    }

}

