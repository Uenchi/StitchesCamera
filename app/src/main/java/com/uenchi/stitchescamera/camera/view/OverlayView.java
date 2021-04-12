package com.uenchi.stitchescamera.camera.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.uenchi.stitchescamera.R;

import org.jetbrains.annotations.NotNull;

public class OverlayView implements V2ThumbLineOverlay.ThumbLineOverlayView {
    private View mMiddleView;
    private View mHeadView;
    private View mTailView;
    private View mRootView;

    public OverlayView(Context context) {
        mRootView = LayoutInflater.from(context).inflate(R.layout.timeline_overlay_stitches, null);
        mMiddleView = mRootView.findViewById(R.id.middle_view);
        mHeadView = mRootView.findViewById(R.id.head_view);
        mTailView = mRootView.findViewById(R.id.tail_view);
    }

    @NotNull
    @Override
    public ViewGroup getContainer() {
        return (ViewGroup) mRootView;
    }

    @NotNull
    @Override
    public View getHeadView() {
        return mHeadView;
    }

    @NotNull
    @Override
    public View getTailView() {
        return mTailView;
    }

    @NotNull
    @Override
    public View getMiddleView() {
        return mMiddleView;
    }
}
