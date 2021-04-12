package com.uenchi.editlibrary.effect;

import android.content.Context;


import com.uenchi.editlibrary.filter.GLImageComplexionBeautyFilter;
import com.uenchi.editlibrary.filter.Gl4SplitFilter;
import com.uenchi.editlibrary.filter.GlFilter;
import com.uenchi.editlibrary.filter.GlFlashFilter;
import com.uenchi.editlibrary.filter.GlItchFilter;
import com.uenchi.editlibrary.filter.GlScaleFilter;
import com.uenchi.editlibrary.filter.GlShakeFilter;
import com.uenchi.editlibrary.filter.GlSoulOutFilter;

import java.util.ArrayList;
import java.util.List;

public class EffectFilterHelper {

    // 资源列表
    private static final List<EffectFilterType> mResourceList = new ArrayList<>();


    public static List<EffectFilterType> getResourceList() {
        return mResourceList;
    }

    public static void initAssetsResource() {
        if (null != mResourceList && mResourceList.size() > 0) return;
        // 清空之前的数据
        mResourceList.clear();

        // 滤镜特效
        mResourceList.add(new EffectFilterType(EffectFilterType.FILTER, "灵魂出窍", 0x000, "assets://thumbs/effect/icon_effect_soul_stuff.png"));
        mResourceList.add(new EffectFilterType(EffectFilterType.FILTER, "抖动", 0x001, "assets://thumbs/effect/icon_effect_shake.png"));
        mResourceList.add(new EffectFilterType(EffectFilterType.FILTER, "缩放", 0x003, "assets://thumbs/effect/icon_effect_scale.png"));
        mResourceList.add(new EffectFilterType(EffectFilterType.FILTER, "毛刺", 0x003, "assets://thumbs/effect/icon_frame_blur.png"));
        mResourceList.add(new EffectFilterType(EffectFilterType.FILTER, "闪电", 0x004, "assets://thumbs/effect/icon_effect_glitter_white.png"));

        // 分屏特效
        mResourceList.add(new EffectFilterType(EffectFilterType.MULTIFRAME, "四分镜", 0x200, "assets://thumbs/effect/icon_frame_four.png"));
    }

    public static GlFilter getEffectFilterByName(Context context, String name){
        switch (name){
            case "灵魂出窍":
                return new GlSoulOutFilter(context);
            case "抖动":
                return new GlShakeFilter(context);
            case "缩放":
                return new GlScaleFilter(context);
            case "毛刺":
                return new GlItchFilter(context);
            case "闪电":
                return new GlFlashFilter(context);
            case "四分镜":
                return new Gl4SplitFilter(context);
            default:
                return new GLImageComplexionBeautyFilter(context);
        }
    }
}
