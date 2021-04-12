package com.uenchi.editlibrary.model;

import android.content.Context;

import com.cgfay.filter.glfilter.color.bean.DynamicColor;
import com.uenchi.editlibrary.effect.EffectFilterHelper;
import com.uenchi.editlibrary.filter.GlColorFilter;
import com.uenchi.editlibrary.filter.GlFilter;

import java.io.Serializable;

public class GlFilterPeriod implements Serializable {
    public long startTimeMs;
    public long endTimeMs;
    public GlFilter filter;
    public String name;
    public DynamicColor color;
    public GlFilterType type;

    public GlFilterPeriod(long startTimeMs, long endTimeMs, GlFilter filter) {
        this.startTimeMs = startTimeMs;
        this.endTimeMs = endTimeMs;
        this.filter = filter;
    }

    public GlFilterPeriod(long startTimeMs, long endTimeMs, GlFilter filter, GlFilterType type) {
        this.startTimeMs = startTimeMs;
        this.endTimeMs = endTimeMs;
        this.filter = filter;
        this.type = type;
    }

    public void setType(GlFilterType type) {
        this.type = type;
    }

    public boolean contains(long time) {
        return time >= startTimeMs && time <= endTimeMs;
    }

    @Override
    public String toString() {
        return "[" + startTimeMs + "," + endTimeMs + "]";
    }

    public GlFilterPeriod copy(Context context) {
        if (null != color) {
            return new GlFilterPeriod(startTimeMs, endTimeMs, new GlColorFilter(color), GlFilterType.FILTER);
        } else if (null != name) {
            return new GlFilterPeriod(startTimeMs, endTimeMs, EffectFilterHelper.getEffectFilterByName(context, name),  GlFilterType.EFFECT);
        } else {
            return new GlFilterPeriod(startTimeMs, endTimeMs, new GlFilter(), GlFilterType.DEFAULT);
        }
    }
}
