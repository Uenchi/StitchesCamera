package com.uenchi.stitchescamera.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.huantansheng.easyphotos.engine.ImageEngine;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * @author : uenchi
 * @date : 2021/4/12
 * @desc :
 */
public class GlideEngine  implements ImageEngine {
    //单例
    private static GlideEngine instance = null;

    //单例模式，私有构造方法
    private GlideEngine() {
    }

    //获取单例
    public static GlideEngine getInstance() {
        if (null == instance) {
            synchronized (GlideEngine.class) {
                if (null == instance) {
                    instance = new GlideEngine();
                }
            }
        }
        return instance;
    }

    @Override
    public void loadPhoto(Context context, String photoPath, ImageView imageView) {
        Glide.with(context).load(photoPath).transition(withCrossFade()).into(imageView);
    }

    @Override
    public void loadGifAsBitmap(Context context, String gifPath, ImageView imageView) {
        Glide.with(context).asBitmap().load(gifPath).into(imageView);
    }

    @Override
    public void loadGif(Context context, String gifPath, ImageView imageView) {
        Glide.with(context).asGif().load(gifPath).transition(withCrossFade()).into(imageView);
    }

    @Override
    public Bitmap getCacheBitmap(Context context, String path, int width, int height) throws Exception {
        return Glide.with(context).asBitmap().load(path).submit(width, height).get();
    }
}
