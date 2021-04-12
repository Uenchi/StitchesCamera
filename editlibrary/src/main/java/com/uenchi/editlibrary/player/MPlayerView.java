package com.uenchi.editlibrary.player;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.os.Build;
import android.print.PrintAttributes;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.cgfay.filter.glfilter.color.bean.DynamicColor;
import com.uenchi.editlibrary.composer.DecoderOutputSurface;
import com.uenchi.editlibrary.composer.EncoderSurface;
import com.uenchi.editlibrary.filter.GlFilter;
import com.uenchi.editlibrary.model.FillMode;
import com.uenchi.editlibrary.model.FillModeCustomItem;
import com.uenchi.editlibrary.model.GlFilterList;
import com.uenchi.editlibrary.model.GlFilterPeriod;
import com.uenchi.editlibrary.model.Resolution;


import java.io.IOException;

import static android.media.MediaPlayer.SEEK_CLOSEST;

/**
 * by shaopx 2018.11.5
 * textureview and egl entry
 */
public class MPlayerView extends FrameLayout implements
        TextureView.SurfaceTextureListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener {

    private static final String TAG = "MPlayerView";
    private Context mContext;
    private FrameLayout mContainer;
    private TextureView mTextureView;
    protected MediaPlayer mMediaPlayer;
    private String mUrl;
    private int surfaceWidth, surfaceHeight;
    private EncoderSurface encoderSurface;
    private DecoderOutputSurface decoderSurface;
    private GlFilterList filterList = null;

    protected long currentPostion = 0L;
    float w, h;

    public MPlayerView(Context context) {
        this(context, null);
    }

    public MPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        mContainer = new FrameLayout(mContext);
        mContainer.setBackgroundColor(Color.BLACK);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(mContainer, params);

        filterList = new GlFilterList();
    }

    public GlFilterList getFilterListData(Context context) {
        return filterList.copy(context);
    }

    public GlFilterPeriod setGlFiler(long startTimeMs, long endTimeMs, GlFilter glFilter, DynamicColor color) {
        GlFilterPeriod period = new GlFilterPeriod(startTimeMs, endTimeMs, glFilter);
        period.color = color;
        filterList.setGlFilter(period);
        return period;
    }

    public void remoteGlFiler() {
        filterList.remoteGlFilter();
    }

    public GlFilterPeriod addGlEffect(long startTimeMs, long endTimeMs, GlFilter glFilter, String name) {
        GlFilterPeriod period = new GlFilterPeriod(startTimeMs, endTimeMs, glFilter);
        period.name = name;
        filterList.addGlEffect(period);
        return period;
    }

    public void remoteGlEffect() {
        filterList.remoteGlEffect();
    }

    public boolean hasGlEffect() {
        return filterList.hasGlEffect();
    }

    public int cleanGlEffect() {
        return filterList.cleanGlEffect();
    }

    public void cleanGlEffectCache() {
        filterList.cleanGlEffectCache();
    }

    public boolean hasGlFiler() {
        return filterList.hasGlFiler();
    }

    public void setDataSource(String url) {
        mUrl = url;
    }

    public void setWithAndHeight(String width, String height) {
        if (null != width && null != height) {
            w = Float.parseFloat(width);
            h = Float.parseFloat(height);
        }
    }

    public void start() {
        initTextureView();
        addTextureView();
        running = true;
    }


    private void initTextureView() {
        if (mTextureView == null) {
            mTextureView = new TextureView(mContext);
            mTextureView.setSurfaceTextureListener(this);
        }
    }

    private void addTextureView() {
        mContainer.removeView(mTextureView);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mContainer.addView(mTextureView, 0, params);
    }

    private void updateTextureViewSizeCenter() {
        Matrix matrix = new Matrix();
        matrix.preTranslate((mTextureView.getWidth() - ww) / 2, -(mTextureView.getHeight() - hh) / 2);
        mTextureView.setTransform(matrix);
        mTextureView.postInvalidate();
    }

    public void updateTextureViewSize(float w, float h) {
        decoderSurface.setFillMode(FillMode.TEST);
        decoderSurface.setFillModeCustomItem(new FillModeCustomItem(0, w, h, 0, 0, 0, 0, 0));
    }

    private void initMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);

            while (decoderSurface.getSurface() == null) {
                try {
                    Thread.sleep(30);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            Surface surface = decoderSurface.getSurface();
            try {
                mMediaPlayer.setDataSource(mUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMediaPlayer.setSurface(surface);

//            surface.release();
            mMediaPlayer.prepareAsync();

            mMediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i1) {
                    if (w > 0 && h > 0 && w > h) {
                        updateTextureViewSizeCenter();
                    }
                }
            });
        }
    }

    public Surface getSurface() {
        return decoderSurface.getSurface();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//        videoRenderer.onSurfaceChanged(width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private int ww;
    private int hh;

    @Override
    public void onSurfaceTextureAvailable(final SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureAvailable: ...");
        surfaceWidth = width;
        surfaceHeight = height;
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {

                encoderSurface = new EncoderSurface(new Surface(surface));
                encoderSurface.makeCurrent();

                decoderSurface = new DecoderOutputSurface(filterList);
                if (w > 0 && h > 0 && w > h) {
                    ww = mTextureView.getWidth();
                    hh = (int) (ww * (h / w));
                    decoderSurface.setOutputResolution(new Resolution(ww, hh));
                    decoderSurface.setInputResolution(new Resolution(ww, hh));
                } else {
                    decoderSurface.setOutputResolution(new Resolution(surfaceWidth, surfaceHeight));
                    decoderSurface.setInputResolution(new Resolution(720, 1280));
                }

                decoderSurface.setupAll();
                post(new Runnable() {
                    @Override
                    public void run() {
                        initMediaPlayer();
                    }
                });
                poll();
            }
        });
        th.start();
    }

    private volatile boolean running = false;
    protected volatile boolean notDestroyed = true;

    private void poll() {
        while (notDestroyed) {
            if (running) {
                try {
                    decoderSurface.awaitNewImage();
                } catch (Exception ex) {
                    ex.printStackTrace();
//                    throw ex;
                    return;
                }
                decoderSurface.drawImage(currentPostion * 1000 * 1000);
                encoderSurface.setPresentationTime(System.currentTimeMillis());
                encoderSurface.swapBuffers();
            } else {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        notDestroyed = false;
        running = false;
        decoderSurface.stopRun();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "onPrepared: ...");
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.start();
        if (null != mOnCompletionListener) {
            mOnCompletionListener.onCompletion();
        }
    }

    public void resumePlay() {
        running = true;
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    public void pausePlay() {
        running = false;
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.pause();
            } catch (Exception ex) {
            }
        }
    }

    public void release() {
        notDestroyed = false;
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        decoderSurface.stopRun();
    }

    public void seekTo(long timems) {
        if (mMediaPlayer != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mMediaPlayer.seekTo(timems, SEEK_CLOSEST);
            } else {
                mMediaPlayer.seekTo((int) timems);
            }
        }
    }

    public void setOnCompletionListener(OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    private OnCompletionListener mOnCompletionListener;

    public interface OnCompletionListener {
        void onCompletion();
    }

}
