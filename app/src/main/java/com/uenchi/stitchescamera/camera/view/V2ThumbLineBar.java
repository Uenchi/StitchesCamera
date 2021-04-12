package com.uenchi.stitchescamera.camera.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.uenchi.stitchescamera.camera.adapter.ThumbRecyclerAdapter;
import com.uenchi.stitchescamera.camera.model.MediaItem;

import java.util.List;

public class V2ThumbLineBar extends FrameLayout {

    private static String TAG = V2ThumbLineBar.class.getName();
    private static final int WHAT_THUMBNAIL_VIEW_AUTO_MOVE = 1;
    private static final int WHAT_TIMELINE_ON_SEEK = 2;
    private static final int WHAT_TIMELINE_FINISH_SEEK = 3;
    private static final int WHAT_TIMELINE_START_SEEK = 4;

    private static final String KEY_RATE = "rate";
    private static final String KEY_DURATION = "duration";
    private static final String KEY_NEED_CALLBACK = "need_callback";

    protected RecyclerView mRecyclerView;
    protected View mIndicator;
    protected ThumbLineConfig mThumbLineConfig;
    protected long mCurrDuration = 0;
    private final Object mCurrDurationLock = new Object();
    private OnBarSeekListener mBarSeekListener;
    protected PlayerListener mLinePlayer;
    private PlayThread mPlayThread;
    private boolean mIsTouching = false;
    private float mErrorDis;
    protected float mCurrScroll;
    protected int mScrollState;
    protected long mDuration;

    private int mIndicatorMargin;
    private int mIndicatorWidth;
    private float downX = 0f;
    private boolean isIndicatorRange = false;
    private float indicatorStart = 0;
    private ThumbRecyclerAdapter mThumbRecyclerAdapter;
    private OnOperationEndListener mOnOperationEndListener;


    /**
     * 整个时间轴View的宽度（缩略图个数 * 单个缩略图的宽度）
     */
    protected float mTimelineBarViewWidth;

    private Handler mUIHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            long duration = msg.getData().getLong(KEY_DURATION);
            switch (msg.what) {
                case WHAT_THUMBNAIL_VIEW_AUTO_MOVE:
                    float rate = msg.getData().getFloat(KEY_RATE);
                    boolean needCallback = msg.getData().getBoolean(KEY_NEED_CALLBACK);
                    if (mBarSeekListener != null && needCallback && !mIsTouching) {
                        mBarSeekListener.onThumbLineBarSeek(duration);
                    }
                    scroll(rate);
                    break;
                case WHAT_TIMELINE_ON_SEEK:
                    if (null != mBarSeekListener)
                        mBarSeekListener.onThumbLineBarSeek(duration);
                    break;
                case WHAT_TIMELINE_FINISH_SEEK:
                    if (null != mBarSeekListener)
                        mBarSeekListener.onThumbLineBarSeekFinish(duration);
                    break;
                case WHAT_TIMELINE_START_SEEK:
                    if (null != mBarSeekListener)
                        mBarSeekListener.onThumbLineBarSeekStart();
                    break;
                default:
                    break;

            }
        }
    };

    public V2ThumbLineBar(@NonNull Context context) {
        this(context, null);
    }

    public V2ThumbLineBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public V2ThumbLineBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }


    /**
     * 初始化ui
     */
    private void initView() {
        mIndicatorMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f, getResources().getDisplayMetrics());
        mRecyclerView = new RecyclerView(getContext());
        mRecyclerView.setFocusable(false);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        params.setMargins(0, mIndicatorMargin, 0, mIndicatorMargin);
        mRecyclerView.setLayoutParams(params);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false) {
            @Override
            public boolean canScrollVertically() {
                return false;

            }
        });

        mIndicatorWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, getResources().getDisplayMetrics());
        indicatorPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, getResources().getDisplayMetrics());

        mIndicator = new View(getContext());
        setIndicatorViewLayoutParams(mIndicator, mIndicatorWidth, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.START, 0xFFFCC937);

        addView(mRecyclerView);
        addView(mIndicator);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setup(List<MediaItem> medias, ThumbLineConfig thumbLineConfig, OnBarSeekListener barSeekListener, PlayerListener linePlayer) {
        mThumbLineConfig = thumbLineConfig;
        initLayoutParams();
        mDuration = linePlayer.getDuration();
        if (mBarSeekListener == null) {
            setOnBarSeekListener(barSeekListener);
            setThumbLinePlayer(linePlayer);
            mRecyclerView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    indicatorScroll(event);
                    return true;
                }
            });
        }

        if (mThumbRecyclerAdapter == null) {
            mThumbRecyclerAdapter = new ThumbRecyclerAdapter(getContext(), medias, mThumbLineConfig.getThumbnailCount(),
                    (int) mLinePlayer.getDuration(),
                    mThumbLineConfig.getScreenWidth(),
                    mThumbLineConfig.getThumbnailPoint().x, mThumbLineConfig.getThumbnailPoint().y);
            mRecyclerView.setAdapter(mThumbRecyclerAdapter);
            mThumbRecyclerAdapter.cacheBitmaps();
        } else {
            mThumbRecyclerAdapter.setData(mThumbLineConfig.getThumbnailCount(),
                    (int) mLinePlayer.getDuration());
            mThumbRecyclerAdapter.notifyDataSetChanged();
        }

        //播放时间通过缩略条回调显示
        restart();
    }


    private void indicatorScroll(MotionEvent event) {
        int actionMasked = event.getActionMasked();
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                isIndicatorRange = isClickIndicator(downX);
                if (!isIndicatorRange) break;
                mIsTouching = true;
                indicatorStart = mIndicator.getTranslationX();
                Message msg = mUIHandler.obtainMessage(WHAT_TIMELINE_START_SEEK);
                mUIHandler.sendMessage(msg);
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isIndicatorRange) break;

                float xDistance = event.getX() - downX;
                if (xDistance != 0f) {
                    float newTransx = indicatorStart + xDistance;
                    adjustProgressBar(mIndicator, newTransx);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsTouching = false;
                if (isIndicatorRange) {
                    Message msgFinish = mUIHandler.obtainMessage(WHAT_TIMELINE_FINISH_SEEK);
                    Bundle data = new Bundle();
                    data.putLong(KEY_DURATION, mCurrDuration);
                    msgFinish.setData(data);
                    mUIHandler.sendMessage(msgFinish);
                }
                isIndicatorRange = false;
                break;
            default:
                break;
        }
    }

    private int indicatorPadding = 0;

    private boolean isClickIndicator(float downX) {
        float indicatorX = mIndicator.getTranslationX();
        return indicatorX > downX - indicatorPadding && indicatorX < downX + indicatorPadding;
    }

    //初始化布局参数 ->动态适配缩略图大小
    private void initLayoutParams() {

        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = mThumbLineConfig.getThumbnailPoint().y + 2 * mIndicatorMargin;

    }

    private void adjustProgressBar(View v, float transX_) {
        float transX = transX_;
        float width = getTimelineBarViewWidth();
        if (transX >= getTimelineBarViewWidth()) {
            transX = width - mIndicatorWidth;
        } else if (transX < 0) {
            transX = 0;
        }
        updateDuration(transX);
        v.setTranslationX(transX);
    }

    public void seekEnd() {
        mUIHandler.removeCallbacksAndMessages(null);
        seekTo(mDuration, false);
    }

    private void updateDuration(float transX) {
        if (transX < 0) {
            transX = 0;
        } else if (transX >= getTimelineBarViewWidth() - mIndicatorWidth) {
            transX = getTimelineBarViewWidth();
        }
        float rate = transX / getTimelineBarViewWidth();
        long duration = (long) (rate * mDuration);
        Message msg = mUIHandler.obtainMessage(WHAT_TIMELINE_ON_SEEK);
        Bundle data = new Bundle();
        data.putLong(KEY_DURATION, duration);
        msg.setData(data);
        mUIHandler.sendMessage(msg);
        mCurrDuration = duration;
    }

    public float getCurrDurationAsDis() {
        return mIndicator.getTranslationX();
    }

    /**
     * recyclerView滚动回调，子类可实现扩张功能
     */
    protected void onRecyclerViewScroll(int dx, int dy) {
        //empty realize
    }

    /**
     * recyclerView滚动状态改变，子类可实现扩张功能
     *
     * @param newState int
     */
    protected void onRecyclerViewScrollStateChanged(int newState) {
        //empty realize
    }

    private void scroll(float rate) {
        float scrollBy = rate * getTimelineBarViewWidth();
        float transX = scrollBy;
        float width = getTimelineBarViewWidth();
        if (transX >= getTimelineBarViewWidth()) {
            transX = width - mIndicatorWidth;
        } else if (transX < 0) {
            transX = 0;
        }
        mIndicator.setTranslationX(transX);
    }

    /**
     * 指示器线条和背景的layoutParams和background Color的设置
     *
     * @param view    目标View
     * @param width   宽
     * @param height  高
     * @param gravity 重心
     * @param color   颜色
     */
    private void setIndicatorViewLayoutParams(View view, int width, int height, int gravity, int color) {
        LayoutParams params = new LayoutParams(width, height, gravity);
        view.setLayoutParams(params);
        view.setBackgroundColor(color);

    }

    public void seekTo(long duration, boolean needCallback) {
        synchronized (mCurrDurationLock) {
            mCurrDuration = duration;
        }
        if (duration == 0) {
            Log.d(TAG, "duration  == 0");
        }
        float rate = duration * 1.0f / mDuration;
        Message msg = mUIHandler.obtainMessage(WHAT_THUMBNAIL_VIEW_AUTO_MOVE);
        Bundle data = new Bundle();
        data.putFloat(KEY_RATE, rate);
        data.putLong(KEY_DURATION, duration);
        data.putBoolean(KEY_NEED_CALLBACK, needCallback);
        msg.setData(data);
        mUIHandler.sendMessage(msg);
    }

    /**
     * 获取缩略图的总宽度
     *
     * @return int
     */
    public float getTimelineBarViewWidth() {
        if (mRecyclerView.getAdapter() == null) {
            return 0;
        }
        if (mTimelineBarViewWidth == 0) {
            this.mTimelineBarViewWidth = mThumbLineConfig.getThumbnailCount() * mThumbLineConfig.getThumbnailPoint().x;
        }
        return mTimelineBarViewWidth;
    }

    /**
     * 缩略条是否处于滚动状态
     *
     * @return boolean
     */
    public boolean isScrolling() {
        return mScrollState != RecyclerView.SCROLL_STATE_IDLE;
    }

    /**
     * 用户滑动thumbLineBar时的监听
     *
     * @param barSeekListener 监听器
     */
    private void setOnBarSeekListener(OnBarSeekListener barSeekListener) {
        mBarSeekListener = barSeekListener;
    }

    /**
     * 设置播放时间同步player
     *
     * @param linePlayer 同步接口
     */
    private void setThumbLinePlayer(PlayerListener linePlayer) {
        mLinePlayer = linePlayer;
    }

    /**
     * 是否正在操作缩略条
     *
     * @return boolean
     */
    public boolean isTouching() {
        return mIsTouching;
    }

    public void setOperationEndListener(OnOperationEndListener listener) {
        this.mOnOperationEndListener = listener;
    }

    /**
     * 隐藏
     */
    public void hide() {
        this.setVisibility(GONE);
    }

    /**
     * 开始显示
     */
    public void show() {
        if (mPlayThread != null && mPlayThread.mState != PlayThread.STATE_PLAYING) {
            //暂停时显示缩略条没有对齐
            seekTo(mLinePlayer.getCurrentDuration(), false);
        }
        setVisibility(VISIBLE);
    }

    public void start() {
        Log.d(TAG, "-------------- start --------------");

        mPlayThread = new PlayThread();
        mPlayThread.startPlaying();
    }

    public void resume() {
        Log.d(TAG, "-------------- resume --------------");
        if (mPlayThread != null) {
            mPlayThread.resumePlaying();
        }
    }

    public void pause() {
        Log.d(TAG, "-------------- pause --------------");
        if (mPlayThread != null) {
            mPlayThread.pause();
        }
    }

    public void stop() {
        Log.d(TAG, "-------------- stop --------------");
        if (mPlayThread != null) {
            mPlayThread.stopPlaying();
            mPlayThread = null;
        }
    }

    public void restart() {
        Log.d(TAG, "-------------- restart --------------");
        if (mPlayThread != null && mPlayThread.isAlive()) {
            mPlayThread.mLastDuration = -1;
            mPlayThread.resumePlaying();
        } else {
            start();
        }
    }


    private final Object mStateLock = new Object();

    protected class PlayThread extends Thread {
        private static final byte STATE_PLAYING = 1;
        private static final byte STATE_PAUSING = 2;
        private static final byte STATE_STOPPING = 3;

        private long mLastDuration = -1;
        private volatile byte mState = STATE_STOPPING;


        @Override
        public void run() {
            super.run();

            synchronized (mStateLock) {
                mState = STATE_PLAYING;
                mLastDuration = -1;
            }

            while (true) {
                synchronized (mStateLock) {
                    if (mState == STATE_PAUSING) {
                        try {
                            //Log.d(TAG, "TimelineBar pausing");
                            mStateLock.wait();
                            Log.d(TAG, "TimelineBar resuming");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else if (mState == STATE_STOPPING) {
                        mCurrDuration = 0;
                        break;
                    }
                }
                synchronized (mCurrDurationLock) {
                    mCurrDuration = mLinePlayer.getCurrentDuration();
                }
                if (mCurrDuration != mLastDuration) {
                    seekTo(mCurrDuration, false);
                    mLastDuration = mCurrDuration;
                }
                try {
                    sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void startPlaying() {
            this.start();
        }

        private void resumePlaying() {
            synchronized (mStateLock) {
                mState = STATE_PLAYING;
                mStateLock.notify();
            }
        }

        public void pause() {
            synchronized (mStateLock) {
                //避免pause和stop造成的anr
                if (mState == STATE_PLAYING) {
                    mState = STATE_PAUSING;
                }
            }
        }

        public int playerStatus() {
            synchronized (mStateLock) {
                return mState;
            }
        }

        private void stopPlaying() {
            synchronized (mStateLock) {
                mState = STATE_STOPPING;
                mStateLock.notify();
            }
            try {
                this.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mCurrDuration = 0;
        }

    }

    /**
     * 用户滑动thumbLineBar时的监听器
     */
    public interface OnBarSeekListener {

        void onThumbLineBarSeekStart();

        /**
         * 正在滑动
         *
         * @param duration 时间
         */
        void onThumbLineBarSeek(long duration);

        /**
         * 滑动完成 (用于快速滑动时惯性滑动发生时候)
         *
         * @param duration 时间
         */
        void onThumbLineBarSeekFinish(long duration);
    }

    /**
     * 缩略条的操作监听
     */
    public interface OnOperationEndListener {
        void onEnd();
    }
}

