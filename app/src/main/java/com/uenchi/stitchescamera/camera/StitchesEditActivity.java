package com.uenchi.stitchescamera.camera;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cgfay.camera.utils.PathConstraints;
import com.cgfay.filter.glfilter.color.bean.DynamicColor;
import com.cgfay.media.CainCommandEditor;
import com.cgfay.media.recorder.HWMediaRecorder;
import com.cgfay.uitls.bean.MusicData;
import com.cgfay.uitls.utils.DensityUtils;
import com.cgfay.uitls.utils.FileUtils;
import com.noober.background.BackgroundLibrary;
import com.uenchi.editlibrary.composer.Mp4Composer;
import com.uenchi.editlibrary.effect.EffectFilterHelper;
import com.uenchi.editlibrary.effect.EffectFilterType;
import com.uenchi.editlibrary.filter.GlColorFilter;
import com.uenchi.editlibrary.filter.GlFilter;
import com.uenchi.editlibrary.model.GlFilterPeriod;
import com.uenchi.editlibrary.player.MPlayerView;
import com.uenchi.editlibrary.player.V2PlayerView;
import com.uenchi.stitchescamera.Const;
import com.uenchi.stitchescamera.R;
import com.uenchi.stitchescamera.camera.view.V2OverlayThumbLineBar;
import com.uenchi.stitchescamera.other.UploadActivity;
import com.uenchi.stitchescamera.camera.adapter.EffectAdapter;
import com.uenchi.stitchescamera.camera.model.EditorPage;
import com.uenchi.stitchescamera.camera.model.MediaItem;
import com.uenchi.stitchescamera.camera.view.OverlayView;
import com.uenchi.stitchescamera.camera.view.PlayerListener;
import com.uenchi.stitchescamera.camera.view.ThumbLineConfig;
import com.uenchi.stitchescamera.camera.view.V2ThumbLineBar;
import com.uenchi.stitchescamera.utils.DialogUtils;
import com.uenchi.stitchescamera.utils.EmptyUtils;
import com.uenchi.stitchescamera.utils.WeakHandler;
import com.uenchi.stitchescamera.camera.view.V2ThumbLineOverlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @author : uenchi
 * @date : 2021/4/12
 * @desc :
 */
public class StitchesEditActivity  extends AppCompatActivity implements View.OnClickListener {
    public static final String VIDEO_PATH = "VIDEO_PATH";
    public static final String MUSIC_DATA = "MUSIC_DATA";
    private static final String FRAGMENT_TAG = "FRAGMENT_TAG";
    private static final String MUSIC_TAG = "MUSIC_TAG";

    private static final int MESSAGE_ADD_OVERLAY = 0;
    private static final int MESSAGE_UPDATE_PROGRESS = 1;
    private static final int MESSAGE_REMOVE_OVERLAY = 2;
    private static final int MESSAGE_STOP_TO_UPDATE_OVERLAY = 3;

    private V2PlayerView mPlayerView;
    private FrameLayout mFragmentBottomContainer;
    private RelativeLayout mLayoutEffect;
    private ConstraintLayout mLayoutPlayer;
    private MediaPlayer mAudioPlayer;
    private ImageView mPause;
    private V2OverlayThumbLineBar mThumbLineBar;
    // 撤消
    private TextView mUndo;
    private ImageView mBack;
    // 下一步
    private TextView mNext;
    private RelativeLayout mActionBar;
    // 取消
    private TextView mCancel;
    // 保存
    private TextView mSave;
    private RelativeLayout mEffectActionBar;
    // 特效
    private TextView mTabEffect;
    // 滤镜
    private TextView mTabFilter;
    // 音乐
    private TextView mTabMusic;
    private LinearLayout mLayoutBottom;
    private RecyclerView mEffectRecyclerView;
    // 滤镜页面
    private PreviewEffectFragment mEffectFragment;

    private OverlayView mCurrOverlayView;

    //视频地址
    private String strVideoPath;
    private boolean mFragmentAnimating;
    private boolean mEffectShowing;
    private long mediaDuration;
    private long curPosition;
    private long addOverlayTime;
    private int mPlayViewWidth;
    private int mPlayViewHeight;
    private GlFilterPeriod mEffectFilterPeriod;
    private MusicData mMusicData;

    private V2ThumbLineOverlay mCurrOverlay;
    private int mOverlayColor = 0;
    private Stack<V2ThumbLineOverlay> mAddedOverlayTemp = null;

    // 命令行编辑器
    private CainCommandEditor mCommandEditor;

    //视频宽高
    private String strWidth, strHeight;

    private WeakHandler mMainHandler = new WeakHandler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MESSAGE_ADD_OVERLAY:
                    addOverlay();
                    break;
                case MESSAGE_UPDATE_PROGRESS:
                    updateOverlayProgress();
                    break;
                case MESSAGE_REMOVE_OVERLAY:
                    removeOverlay();
                    break;
                case MESSAGE_STOP_TO_UPDATE_OVERLAY:
                    mMainHandler.removeMessages(MESSAGE_UPDATE_PROGRESS);
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        BackgroundLibrary.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_edit);

        // 命令行编辑器
        mCommandEditor = new CainCommandEditor();

        strVideoPath = getIntent().getStringExtra(VIDEO_PATH);
        mMusicData = getIntent().getParcelableExtra(MUSIC_DATA);

        mediaDuration = CainCommandEditor.getDuration(strVideoPath) / 1000;

        findView();
        getWidthAndHeight();
        initPlayerView();
        initAudioPlayer();
        initThumbLineBar();
        initEffectList();
    }

    private void findView() {
        mPlayerView = findViewById(R.id.player_view);
        mPlayerView.setOnClickListener(this);
        mFragmentBottomContainer = findViewById(R.id.fragment_bottom_container);
        mLayoutEffect = findViewById(R.id.layout_effect);
        mPause = findViewById(R.id.pause);
        mPause.setOnClickListener(this);
        mLayoutPlayer = findViewById(R.id.layout_player);
        mThumbLineBar = findViewById(R.id.thumb_line_bar);
        mUndo = findViewById(R.id.undo);
        mUndo.setOnClickListener(this);
        mLayoutEffect = findViewById(R.id.layout_effect);
        mBack = findViewById(R.id.back);
        mBack.setOnClickListener(this);
        mNext = findViewById(R.id.next);
        mNext.setOnClickListener(this);
        mActionBar = findViewById(R.id.action_bar);
        mCancel = findViewById(R.id.cancel);
        mCancel.setOnClickListener(this);
        mSave = findViewById(R.id.save);
        mSave.setOnClickListener(this);
        mEffectActionBar = findViewById(R.id.effect_action_bar);
        mTabEffect = findViewById(R.id.tab_effect);
        mTabEffect.setOnClickListener(this);
        mTabFilter = findViewById(R.id.tab_filter);
        mTabFilter.setOnClickListener(this);
        mTabMusic = findViewById(R.id.tab_music);
        mTabMusic.setOnClickListener(this);
        mLayoutBottom = findViewById(R.id.layout_bottom);
        mEffectRecyclerView = findViewById(R.id.effect_recycler_view);
    }

    private void initPlayerView() {
        mPlayerView.setPositionChangeListener(new V2PlayerView.PositionChangeListener() {
            @Override
            public void onPositionChange(long curPosition) {
                StitchesEditActivity.this.curPosition = curPosition;
            }
        });
        mPlayerView.setOnCompletionListener(new MPlayerView.OnCompletionListener() {
            @Override
            public void onCompletion() {
                if (null != mMusicData) {
                    mAudioPlayer.seekTo(0);
                }
            }
        });
        mPlayerView.setDataSource(strVideoPath);
        //传入视频宽高
        mPlayerView.setWithAndHeight(strWidth, strHeight);
        mPlayerView.start();
    }

    private void getWidthAndHeight() {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(strVideoPath);
        Bitmap mBitmap = retriever.getFrameAtTime((int) (0.5 * HWMediaRecorder.SECOND_IN_US));
        //避免MediaMetadataRetriever 在部分手机获取宽高不准确
        if (EmptyUtils.isNotEmpty(mBitmap)) {
            int iWith = mBitmap.getWidth();
            int iHeight = mBitmap.getHeight();
            strWidth = String.valueOf(iWith);
            strHeight = String.valueOf(iHeight);
        } else {
            strWidth = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH); //宽
            strHeight = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);//高
        }

    }

    private void initAudioPlayer() {
        mAudioPlayer = new MediaPlayer();
        mAudioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        if (null != mMusicData) {
            setMusicDataSource(mMusicData.getPath());
        }
    }

    private void initThumbLineBar() {
        List<MediaItem> medias = new ArrayList<>();
        MediaItem mediaItem = new MediaItem(strVideoPath, "video", 0, 0);
        mediaItem.setDuration((int) mediaDuration);
        medias.add(mediaItem);

        int thumbnailSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32f, getResources().getDisplayMetrics());
        Point thumbnailPoint = new Point(thumbnailSize, thumbnailSize);
        ThumbLineConfig config = new ThumbLineConfig.Builder()
                .screenWidth(getWindowManager().getDefaultDisplay().getWidth())
                .thumbPoint(thumbnailPoint)
                .thumbnailCount(10)
                .build();

        mThumbLineBar.setup(medias, config, mOnBarSeekListener, mPlayerListener);
    }

    private void initEffectList() {
        List<EffectFilterType> effectFilterList = EffectFilterHelper.getResourceList();
        mEffectRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        EffectAdapter effectAdapter = new EffectAdapter(this, effectFilterList);
        mEffectRecyclerView.setAdapter(effectAdapter);

        effectAdapter.setOnEffectChangeListener(new EffectAdapter.OnEffectChangeListener() {
            @Override
            public void onBeginEffectChanged(EffectFilterType effectType) {
                GlFilter filter = EffectFilterHelper.getEffectFilterByName(StitchesEditActivity.this, effectType.getName());
                addOverlayTime = curPosition;
                mEffectFilterPeriod = mPlayerView.addGlEffect(addOverlayTime, mediaDuration, filter, effectType.getName());
                mMainHandler.sendEmptyMessage(MESSAGE_ADD_OVERLAY);
                resumePlayer();
            }

            @Override
            public void onEndEffectChanged(EffectFilterType effectType) {
                mMainHandler.sendEmptyMessage(MESSAGE_STOP_TO_UPDATE_OVERLAY);
                pausePlayer();
                if (null != mEffectFilterPeriod) {
                    mEffectFilterPeriod.endTimeMs = curPosition;
                    long effectDuration = mEffectFilterPeriod.endTimeMs - mEffectFilterPeriod.startTimeMs;
                    if (effectDuration < 200) {
                        remoteGlEffect();
                    } else {
                        mUndo.setSelected(true);
                    }
                }
            }
        });
    }

    private void remoteGlEffect() {
        mPlayerView.remoteGlEffect();
        mThumbLineBar.remoteLastOverlay();
        if (null != mEffectFilterPeriod) {
            seekTo(mEffectFilterPeriod.startTimeMs);
            if (null != mThumbLineBar) {
                mThumbLineBar.pause();
                mThumbLineBar.seekTo(mEffectFilterPeriod.startTimeMs, false);
            }
        }
        mEffectFilterPeriod = null;
        if (!mPlayerView.hasGlEffect()) {
            mUndo.setSelected(false);
        }
    }

    private void setMusicDataSource(String path) {
        if (null == mAudioPlayer) return;
        if (mAudioPlayer.isPlaying()) {
            mAudioPlayer.stop();
        }
        mAudioPlayer.reset();
        mAudioPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        try {
            mAudioPlayer.setDataSource(path);
        } catch (IOException e) {
        }
        mAudioPlayer.prepareAsync();
        resetPlayer();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.player_view:
            case R.id.pause:
                if (mPause.getVisibility() != View.VISIBLE) {
                    pausePlayer();
                } else {
                    resumePlayer();
                }
                break;
            case R.id.undo:
                if (mUndo.isSelected()) {
                    remoteGlEffect();
                }
                break;
            case R.id.back:
                showBackTipsDialog();
                break;
            case R.id.next:
                mNext.setClickable(false);
                DialogUtils.getInstance().showLoading(this);
                pausePlayer();
                mp4Composer();
                break;
            case R.id.cancel:
                showChangeEffectLayout(false);
                int size = mPlayerView.cleanGlEffect();
                mThumbLineBar.remoteMultipleOverlay(size);
                break;
            case R.id.save:
                showChangeEffectLayout(false);
                mPlayerView.cleanGlEffectCache();
                break;
            case R.id.tab_effect:
                showChangeEffectLayout(true);
                break;
            case R.id.tab_filter:
                showEffectFragment();
                break;
            case R.id.tab_music:
                openMusicPicker();
                break;
            default:
                break;
        }
    }

    private void showBackTipsDialog() {
        Dialog dialog = DialogUtils.getInstance().createDialogCenter(this, R.layout.dialog_simple_msg);
        ((TextView) dialog.findViewById(R.id.txMsg)).setText("返回拍摄界面将会清空所有效果");
        dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();

            }
        });
        dialog.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pausePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mPlayerView) {
            mPlayerView.release();
        }

        if (null != mAudioPlayer) {
            mAudioPlayer.release();
        }

        if (mCommandEditor != null) {
            mCommandEditor.release();
            mCommandEditor = null;
        }
    }

    private void resetPlayer() {
        resumePlayer();
        mPlayerView.seekTo(0);
    }

    private void resumePlayer() {
        if (mPlayerView != null) {
            mPlayerView.resumePlay();
        }
        if (mAudioPlayer != null && mMusicData != null) {
            mAudioPlayer.start();
        }
        if (mThumbLineBar != null) {
            mThumbLineBar.resume();
        }
        mPause.setVisibility(View.GONE);
    }

    private void seekTo(long timeMs) {
        if (mPlayerView != null) {
            mPlayerView.resumePlay();
            mPlayerView.seekTo(timeMs);
            mPlayerView.pausePlay();
        }
        if (mAudioPlayer != null && mMusicData != null) {
            mAudioPlayer.start();
            mAudioPlayer.seekTo((int) timeMs);
            mAudioPlayer.pause();
        }
        mPause.setVisibility(View.VISIBLE);
    }

    private void pausePlayer() {
        if (mPlayerView != null) {
            mPlayerView.pausePlay();
        }
        if (mAudioPlayer != null && mMusicData != null) {
            mAudioPlayer.pause();
        }
        if (mThumbLineBar != null) {
            mThumbLineBar.pause();
        }
        mPause.setVisibility(View.VISIBLE);
    }

    /**
     * 显示特效页面
     *
     * @param showSubView
     */
    private void showChangeEffectLayout(boolean showSubView) {
        mEffectShowing = showSubView;
        if (showSubView) {
            AnimatorSet animatorSet = new AnimatorSet();
            // 特效页面显示动画
            ValueAnimator effectShowAnimator = ValueAnimator.ofFloat(1f, 0f);
            effectShowAnimator.setDuration(400);
            final LinearLayout.LayoutParams effectParams = (LinearLayout.LayoutParams) mLayoutEffect.getLayoutParams();
            final LinearLayout.LayoutParams playerParams = (LinearLayout.LayoutParams) mLayoutPlayer.getLayoutParams();
            mPlayViewWidth = mLayoutPlayer.getWidth();
            mPlayViewHeight = mLayoutPlayer.getHeight();
            final int minPlayViewHeight = mPlayViewHeight - DensityUtils.dp2px(StitchesEditActivity.this, 200);
            final float playerViewScale = mPlayViewWidth / (float) mPlayViewHeight;
            effectShowAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    effectParams.bottomMargin = (int) (-DensityUtils.dp2px(StitchesEditActivity.this, 200) * (float) animation.getAnimatedValue());
                    mLayoutEffect.setLayoutParams(effectParams);

                    playerParams.width = (int) ((minPlayViewHeight + ((mPlayViewHeight - minPlayViewHeight) * (float) animation.getAnimatedValue())) * playerViewScale);
                    playerParams.topMargin = (int) (DensityUtils.dp2px(StitchesEditActivity.this, 50) * (1f - (float) animation.getAnimatedValue()));
                    playerParams.bottomMargin = (int) (DensityUtils.dp2px(StitchesEditActivity.this, 50) * (float) animation.getAnimatedValue() +
                            DensityUtils.dp2px(StitchesEditActivity.this, 10));
                    mLayoutPlayer.setLayoutParams(playerParams);
                }
            });
            animatorSet.playSequentially(effectShowAnimator);
            animatorSet.start();
            mLayoutBottom.setVisibility(View.GONE);
            mActionBar.setVisibility(View.GONE);
            mEffectActionBar.setVisibility(View.VISIBLE);

            pausePlayer();
        } else {
            AnimatorSet animatorSet = new AnimatorSet();
            // 特效页面退出动画
            ValueAnimator effectExitAnimator = ValueAnimator.ofFloat(0f, 1f);
            effectExitAnimator.setDuration(400);
            final LinearLayout.LayoutParams effectParams = (LinearLayout.LayoutParams) mLayoutEffect.getLayoutParams();
            final LinearLayout.LayoutParams playerParams = (LinearLayout.LayoutParams) mLayoutPlayer.getLayoutParams();
            final int minPlayViewHeight = mPlayViewHeight - DensityUtils.dp2px(StitchesEditActivity.this, 200);
            final float playerViewScale = mPlayViewWidth / (float) mPlayViewHeight;
            effectExitAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    effectParams.bottomMargin = (int) (-DensityUtils.dp2px(StitchesEditActivity.this, 200) * (float) animation.getAnimatedValue());
                    mLayoutEffect.setLayoutParams(effectParams);

                    playerParams.width = (int) ((minPlayViewHeight + ((mPlayViewHeight - minPlayViewHeight) * (float) animation.getAnimatedValue())) * playerViewScale);
                    playerParams.topMargin = (int) (DensityUtils.dp2px(StitchesEditActivity.this, 50) * (1f - (float) animation.getAnimatedValue()));
                    playerParams.bottomMargin = (int) (DensityUtils.dp2px(StitchesEditActivity.this, 50) * (float) animation.getAnimatedValue() +
                            DensityUtils.dp2px(StitchesEditActivity.this, 10));
                    mLayoutPlayer.setLayoutParams(playerParams);
                }
            });
            animatorSet.playSequentially(effectExitAnimator);
            animatorSet.start();

            mLayoutBottom.setVisibility(View.VISIBLE);
            mActionBar.setVisibility(View.VISIBLE);
            mEffectActionBar.setVisibility(View.GONE);

            resumePlayer();
        }
    }

    /**
     * 显示滤镜页面
     */
    private void showEffectFragment() {
        if (mFragmentAnimating) {
            return;
        }
        if (mEffectFragment == null) {
            mEffectFragment = new PreviewEffectFragment();
        }
        mEffectFragment.setCallback(new PreviewEffectFragment.Callback() {
            @Override
            public void onFilterSelect(DynamicColor color) {
                GlColorFilter colorFilter = new GlColorFilter(color);
                mPlayerView.setGlFiler(0, mediaDuration, colorFilter, color);
            }

            @Override
            public void onDeleteFilter() {
                mPlayerView.remoteGlFiler();
            }

            @Override
            public void onClose() {
                hideFragmentAnimating();
            }
        });

        if (!mEffectFragment.isAdded()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_bottom_container, mEffectFragment, FRAGMENT_TAG)
                    .commitAllowingStateLoss();
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .show(mEffectFragment)
                    .commitAllowingStateLoss();
        }
        showFragmentAnimating();
    }

    private void addOverlay() {
        mCurrOverlayView = new OverlayView(this);
        long duration = curPosition - addOverlayTime;
        mCurrOverlay = mThumbLineBar.addOverlay(addOverlayTime, duration, mCurrOverlayView, 0,
                false, EditorPage.FILTER_EFFECT, null);
        mCurrOverlay.updateMiddleViewColor(mOverlayColor);
        mMainHandler.sendEmptyMessage(MESSAGE_UPDATE_PROGRESS);
        if (mAddedOverlayTemp == null) {
            mAddedOverlayTemp = new Stack();
        }
        mAddedOverlayTemp.push(mCurrOverlay);
    }

    private void updateOverlayProgress() {
        if (curPosition + 200 >= mediaDuration) {
            seekTo(mediaDuration);
            mMainHandler.sendEmptyMessage(MESSAGE_STOP_TO_UPDATE_OVERLAY);
            mThumbLineBar.seekEnd();
            mCurrOverlay.updateDuration(mediaDuration - addOverlayTime);
            return;
        }
        long duration = curPosition - addOverlayTime;
        mCurrOverlay.updateDuration(duration);
        mMainHandler.sendEmptyMessage(MESSAGE_UPDATE_PROGRESS);
    }

    private void removeOverlay() {
        if (mAddedOverlayTemp == null) {
            return;
        }
        V2ThumbLineOverlay overlay = mAddedOverlayTemp.pop();
        mThumbLineBar.removeOverlay(overlay);
        mThumbLineBar.seekTo(overlay.getStartTime(), true);
        mCurrOverlay = null;
        mCurrOverlayView = null;
        if (!mAddedOverlayTemp.empty()) {
            mCurrOverlay = mAddedOverlayTemp.peek();
            mCurrOverlayView = (OverlayView) mCurrOverlay.getThumbLineOverlayView();
        }
    }

    /**
     * 打开音乐选择页面
     */
    public void openMusicPicker() {
        PickerMusicFragment fragment = new PickerMusicFragment();
        fragment.addOnMusicSelectedListener(
                new PickerMusicFragment.OnMusicSelectedListener() {
                    @Override
                    public MusicData onInitMusicData() {
                        return mMusicData;
                    }

                    @Override
                    public void onMusicSelectClose() {
                        removeFragment(MUSIC_TAG);
                    }

                    @Override
                    public void onMusicSelected(MusicData musicData) {
                        mMusicData = musicData;
                        setMusicDataSource(musicData.getPath());
                        removeFragment(MUSIC_TAG);
                    }

                    @Override
                    public void onMusicDelete() {
                        mMusicData = null;
                        mAudioPlayer.stop();
                        removeFragment(MUSIC_TAG);
                    }
                });
        getSupportFragmentManager()
                .beginTransaction()
                .add(fragment, MUSIC_TAG)
                .commitAllowingStateLoss();

        pausePlayer();
    }

    /**
     * 显示Fragment动画
     */
    private void showFragmentAnimating() {
        if (mFragmentAnimating) {
            return;
        }
        mFragmentAnimating = true;
        mFragmentBottomContainer.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(this, com.cgfay.cameralibrary.R.anim.preview_slide_up);
        mFragmentBottomContainer.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mFragmentAnimating = false;
                mLayoutBottom.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    /**
     * 隐藏Fragment动画
     */
    private void hideFragmentAnimating() {
        if (mFragmentAnimating) {
            return;
        }
        mFragmentAnimating = true;
        Animation animation = AnimationUtils.loadAnimation(this, com.cgfay.cameralibrary.R.anim.preivew_slide_down);
        mFragmentBottomContainer.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mLayoutBottom.setVisibility(View.VISIBLE);
                removeFragment(FRAGMENT_TAG);
                mFragmentAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 移除Fragment
     */
    private void removeFragment(String tag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(fragment)
                    .commitAllowingStateLoss();
        }
    }

    private void startUploadActivity(String videoPath) {
        DialogUtils.getInstance().dismissLoading();
        Intent intent = new Intent(StitchesEditActivity.this, UploadActivity.class);
        intent.putExtra(Const.INTENT_VIDEO_PATH, videoPath);
        startActivity(intent);
    }

    private void audioCut(String videoPath) {
        String dstFile = PathConstraints.getAudioTempPath(this, "audioCut");
        FileUtils.createFile(dstFile);
        mCommandEditor.execCommand(CainCommandEditor.audioCut(mMusicData.getPath(), dstFile, 0, (int) mediaDuration),
                (result) -> {
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (result == 0) {
                                mergeAudioVideo(videoPath, dstFile);
                            } else {
                                DialogUtils.getInstance().dismissLoading();
                                Toast.makeText(StitchesEditActivity.this, "合成失败", Toast.LENGTH_SHORT).show();
                                FileUtils.deleteFile(dstFile);
                                FileUtils.deleteFile(videoPath);
                            }
                        }
                    });

                });
    }

    private void mergeAudioVideo(String videoPath, String audioPath) {
        final String currentFile = PathConstraints.getVideoCachePath(this);
        mCommandEditor.execCommand(CainCommandEditor.mergeAudioVideo(videoPath, audioPath, currentFile),
                (result) -> {
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (result == 0) {
                                startUploadActivity(currentFile);
                            } else {
                                FileUtils.deleteFile(currentFile);
                                DialogUtils.getInstance().dismissLoading();
                                Toast.makeText(StitchesEditActivity.this, "合成失败", Toast.LENGTH_SHORT).show();
                            }
                            // 删除旧的文件
                            FileUtils.deleteFile(videoPath);
                            FileUtils.deleteFile(audioPath);
                        }
                    });
                });
    }

    private void mp4Composer() {
        final String currentFile = PathConstraints.getVideoCachePath(this);
        new Mp4Composer(strVideoPath, currentFile)
                .size(544, 960)
                .filterList(mPlayerView.getFilterListData(this))
                .listener(new Mp4Composer.Listener() {
                    @Override
                    public void onProgress(double progress) {

                    }

                    @Override
                    public void onCompleted() {
                        mMainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mNext.setClickable(true);
                                if (null != mMusicData) {
                                    audioCut(currentFile);
                                } else {
                                    DialogUtils.getInstance().dismissLoading();
                                    startUploadActivity(currentFile);
                                }
                            }
                        });

                    }

                    @Override
                    public void onCanceled() {
                        mNext.setClickable(true);
                    }

                    @Override
                    public void onFailed(Exception exception) {
                        mMainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                FileUtils.deleteFile(currentFile);
                                DialogUtils.getInstance().dismissLoading();
                                Toast.makeText(StitchesEditActivity.this, "合成失败", Toast.LENGTH_SHORT).show();
                                mNext.setClickable(true);
                            }
                        });
                    }
                }).start();
    }


    V2ThumbLineBar.OnBarSeekListener mOnBarSeekListener = new V2ThumbLineBar.OnBarSeekListener() {
        @Override
        public void onThumbLineBarSeekStart() {
            if (mPause.getVisibility() != View.VISIBLE)
                pausePlayer();
        }

        @Override
        public void onThumbLineBarSeek(long duration) {
            if (null != mPlayerView) {
                seekTo(duration);
            }
        }

        @Override
        public void onThumbLineBarSeekFinish(long duration) {
            if (null != mPlayerView) {
                curPosition = duration;
                seekTo(duration);
            }
            if (mPause.getVisibility() != View.VISIBLE)
                pausePlayer();
        }
    };

    PlayerListener mPlayerListener = new PlayerListener() {
        @Override
        public long getCurrentDuration() {
            return curPosition;
        }

        @Override
        public long getDuration() {
            return mediaDuration;
        }

        @Override
        public void updateDuration(long duration) {
        }
    };
}