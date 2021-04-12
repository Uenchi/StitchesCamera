package com.uenchi.editlibrary.model;

import android.content.Context;
import android.util.Log;


import com.uenchi.editlibrary.composer.EFramebufferObject;
import com.uenchi.editlibrary.filter.GlFilter;
import com.uenchi.editlibrary.filter.GlFilterGroup;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by shaopx on 2018/11/05.
 * glfiter list , time aware
 * its name is like a list , But, one time one filter.  different with  GlFilterGroup.
 */
public class GlFilterList {

    private static final String TAG = "GlFilterList";
    private final LinkedList<GlFilterPeriod> glFilerPeriod = new LinkedList<>();
    private List<GlFilterPeriod> glFilerList = new ArrayList<>();
    private List<GlFilterPeriod> glEffectList = new ArrayList<>();
    private List<GlFilterPeriod> glEffectCacheList = new ArrayList<>();
    private GlFilterGroup drawFilter;
    private boolean needLastFrame = false;
    private boolean onlyOneEffect = false;

    public GlFilterList() {
        drawFilter = new GlFilterGroup();
        glFilerPeriod.add(0, new GlFilterPeriod(0, 600 * 1000, new GlFilter(), GlFilterType.DEFAULT));
    }

    private void putGlFilter(GlFilterPeriod period) {
        glFilerPeriod.add(period);
    }

    public void setGlFilter(GlFilterPeriod period) {
        if (glFilerList.size() > 0) {
            remoteGlFilter();
        }
        period.setType(GlFilterType.FILTER);
        glFilerList.add(period);
        glFilerPeriod.add(0, period);
    }

    public void remoteGlFilter() {
        int size = glFilerList.size();
        if (size > 0) {
            GlFilterPeriod period = glFilerList.get(0);
            glFilerList.clear();
            int index = glFilerPeriod.indexOf(period);
            if (index >= 0) {
                glFilerPeriod.remove(index);
            }
        }
    }

    public void addGlEffect(GlFilterPeriod period) {
        period.setType(GlFilterType.EFFECT);
        glEffectCacheList.add(period);
        glEffectList.add(period);
        glFilerPeriod.add(0, period);
    }

    public void remoteGlEffect() {
        int size = glEffectList.size();
        if (size > 0) {
            GlFilterPeriod period = glEffectList.get(size - 1);
            glEffectList.remove(size - 1);
            int index = glFilerPeriod.indexOf(period);
            if (index >= 0) {
                glFilerPeriod.remove(index);
            }
            remoteGlEffectCache();
        }
    }

    private void remoteGlEffectCache(){
        int size = glEffectCacheList.size();
        if (size > 0) {
            glEffectCacheList.remove(size - 1);
        }
    }

    public int cleanGlEffect(){
        int size = glEffectCacheList.size();
        for (GlFilterPeriod period : glEffectCacheList){
            int index = glFilerPeriod.indexOf(period);
            if (index >= 0) {
                glFilerPeriod.remove(index);
            }

            int index2 = glEffectList.indexOf(period);
            if (index2 >= 0) {
                glEffectList.remove(index2);
            }
        }
        glEffectCacheList.clear();
        return size;
    }

    public void cleanGlEffectCache(){
        glEffectCacheList.clear();
    }

    public GlFilter getGlFilter(long time) {
        for (GlFilterPeriod glFilterPeriod : glFilerPeriod) {
            if (glFilterPeriod.contains(time)) {
                return glFilterPeriod.filter;
            }
        }
        return null;
    }


    public void draw(int texName, EFramebufferObject fbo, long presentationTimeUs) {
        List<GlFilter> glFilters = new ArrayList<>();
        for (GlFilterPeriod glFilterPeriod : glFilerPeriod) {
            if (glFilterPeriod.contains(presentationTimeUs / (1000 * 1000))) {
                if (glFilterPeriod.type != GlFilterType.EFFECT){
                    glFilters.add(glFilterPeriod.filter);
                }else if (!onlyOneEffect){
                    onlyOneEffect = true;
                    glFilters.add(glFilterPeriod.filter);
                }
            }
        }
        drawFilter.startDraw(texName, fbo, glFilters);
        glFilters.clear();
        onlyOneEffect = false;
    }

    public void release() {
        drawFilter.release();
        for (GlFilterPeriod glFilterPeriod : glFilerPeriod) {
            glFilterPeriod.filter.release();
        }
    }

//    public void setFrameSize(int width, int height) {
//        for (GlFilterPeriod glFilterPeriod : glFilerPeriod) {
//            glFilterPeriod.filter.setFrameSize(width, height);
//        }
//    }

//    public void setup() {
//        for (GlFilterPeriod glFilterPeriod : glFilerPeriod) {
//            glFilterPeriod.filter.setup();
//        }
//    }

    public boolean needLastFrame() {
        return needLastFrame;
    }

    public boolean hasGlEffect() {
        return glEffectList.size() > 0 ? true : false;
    }

    public boolean hasGlFiler() {
        return glFilerList.size() > 0 ? true : false;
    }

    public void remoteLast(){
        glFilerPeriod.removeLast();
    }

    @NotNull
    public GlFilterList copy(Context context) {
        GlFilterList filterList = new GlFilterList();
        filterList.remoteLast();
        for (GlFilterPeriod glFilterPeriod : glFilerPeriod) {
            filterList.putGlFilter(glFilterPeriod.copy(context));
        }
        return filterList;
    }

}
