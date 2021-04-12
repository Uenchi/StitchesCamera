package com.uenchi.editlibrary.filter;

import android.opengl.GLES20;

import com.uenchi.editlibrary.composer.EFramebufferObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;

/**
 * Created by sudamasayuki on 2017/05/16.
 */

public class GlFilterGroup extends GlFilter {

    private Collection<GlFilter> filters;

    private final ArrayList<GlFilterEntry> list = new ArrayList<GlFilterEntry>();

    static class GlFilterEntry {
        GlFilter filter;
        EFramebufferObject fbo;
        String name;

        public GlFilterEntry(GlFilter filter, EFramebufferObject fbo, String name) {
            this.filter = filter;
            this.fbo = fbo;
            this.name = name;
        }
    }

    public GlFilterGroup() {

    }

    public GlFilterGroup(final GlFilter... glFilters) {
        this(Arrays.asList(glFilters));
    }

    public GlFilterGroup(final Collection<GlFilter> glFilters) {
        filters = glFilters;
    }

    public void startDraw(final int texName, final EFramebufferObject fbo, final Collection<GlFilter> glFilters) {
        release();
        filters = glFilters;
        setup();
        setFrameSize(fbo.getWidth(), fbo.getHeight());
        draw(texName, fbo);
    }

    @Override
    public void setup() {
        super.setup();

        if (filters != null) {
            final int max = filters.size();
            int count = 0;

            for (final GlFilter filter : filters) {
                filter.setup();
                final EFramebufferObject fbo;
                if ((count + 1) < max) {
                    fbo = new EFramebufferObject();
                } else {
                    fbo = null;
                }
                list.add(new GlFilterEntry(filter, fbo, filter.getName()));
                count++;
            }
        }
    }

    @Override
    public void release() {
        for (final GlFilterEntry entry : list) {
            if (entry.filter != null) {
                entry.filter.release();
            }
            if (entry.fbo != null) {
                entry.fbo.release();
            }
        }
        list.clear();
        super.release();
    }

    @Override
    public void setFrameSize(final int width, final int height) {
        super.setFrameSize(width, height);

        for (final GlFilterEntry entry : list) {
            if (entry.filter != null) {
                entry.filter.setFrameSize(width, height);
            }
            if (entry.fbo != null) {
                entry.fbo.setup(width, height);
            }
        }
    }

    private int prevTexName;

    @Override
    public int draw(final int texName, final EFramebufferObject fbo) {
        prevTexName = texName;
        int textName = -1;

        for (final GlFilterEntry entry : list) {
            if (entry.fbo != null) {
                if (entry.filter != null) {
                    entry.fbo.enable();
                    GLES20.glClear(GL_COLOR_BUFFER_BIT);

                    textName = entry.filter.draw(prevTexName, entry.fbo);
                }
                prevTexName = entry.fbo.getTexName();
            } else {
                if (fbo != null) {
                    fbo.enable();
                } else {
                    GLES20.glBindFramebuffer(GL_FRAMEBUFFER, 0);
                }

                if (entry.filter != null) {
                    textName = entry.filter.draw(prevTexName, fbo);
                }
            }
        }

        return textName;
    }

}
