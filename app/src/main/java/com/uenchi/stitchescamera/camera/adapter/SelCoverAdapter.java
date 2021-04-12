package com.uenchi.stitchescamera.camera.adapter;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.uenchi.stitchescamera.R;

import org.jetbrains.annotations.NotNull;

/**
 * @author : uenchi
 * @date : 2021/4/12
 * @desc :
 */
public class SelCoverAdapter extends BaseQuickAdapter<Bitmap, BaseViewHolder> {
    public SelCoverAdapter() {
        super(R.layout.adapter_stitches_cover);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, Bitmap bitmap) {
        ImageView imageView = baseViewHolder.getView(R.id.cover);
        imageView.setImageBitmap(bitmap);
    }
}