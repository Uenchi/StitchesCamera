package com.uenchi.stitchescamera.camera;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.ToastUtils;
import com.cgfay.camera.PreviewEngine;
import com.cgfay.camera.camera.CameraParam;
import com.cgfay.camera.model.AspectRatio;
import com.cgfay.camera.widget.CainTextureView;
import com.cgfay.camera.widget.CameraMeasureFrameLayout;
import com.cgfay.camera.widget.CameraTabView;
import com.cgfay.camera.widget.PreviewMeasureListener;
import com.cgfay.camera.widget.RecordButton;
import com.cgfay.camera.widget.RecordProgressView;
import com.cgfay.camera.widget.RecordSpeedLevelBar;
import com.cgfay.filter.glfilter.color.bean.DynamicColor;
import com.cgfay.media.recorder.HWMediaRecorder;
import com.cgfay.media.recorder.SpeedMode;
import com.cgfay.uitls.bean.MusicData;
import com.cgfay.uitls.utils.BrightnessUtils;
import com.cgfay.uitls.utils.PermissionUtils;
import com.cgfay.uitls.widget.RoundOutlineProvider;
import com.huantansheng.easyphotos.EasyPhotos;
import com.huantansheng.easyphotos.callback.SelectCallback;
import com.huantansheng.easyphotos.constant.Type;
import com.huantansheng.easyphotos.models.album.entity.Photo;
import com.uenchi.stitchescamera.R;
import com.uenchi.stitchescamera.utils.BigDecimalUtils;
import com.uenchi.stitchescamera.utils.ContentUriCompressEngine;
import com.uenchi.stitchescamera.utils.DialogUtils;
import com.uenchi.stitchescamera.utils.EmptyUtils;
import com.uenchi.stitchescamera.utils.GlideEngine;
import com.uenchi.stitchescamera.utils.WeakHandler;

import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @author : uenchi
 * @date : 2021/4/9
 * @desc :
 */
public class StitchesCameraFragment extends Fragment implements View.OnClickListener {

    private static final boolean VERBOSE = true;
    private static final String FRAGMENT_TAG = "FRAGMENT_TAG";
    private static final String MUSIC_TAG = "MUSIC_TAG";

    private View mBack;

    // Fragment主页面
    private View mContentView;

    private CameraMeasureFrameLayout mLayoutCameraPreview;
    private CainTextureView mCameraTextureView;
    private RecordProgressView mRecordProgress;
    // 选择音乐
    private TextView mMusic;
    private LinearLayout mLlvMusic;
    // 翻转
    private TextView mSwitchCamera;
    // 滤镜
    private TextView mFilter;
    // 速度关
    private TextView mSpeed;
    // 闪光灯
    private TextView mFlash;
    private RecordSpeedLevelBar mRecordSpeedBar;
    private TextView mRecordTime;
    private RecordButton mRecordButton;
    private ImageView mDone;
    private ImageView mDelete;
    private ImageView mPhoto;
    private View mVBottom;
    private CameraTabView mTabView;
    private CircleImageView mIndicator;
    private FrameLayout mFragmentBottomContainer;

    private WeakHandler mMainHandler;
    private StitchesCameraPreviewPresenter mPreviewPresenter;
    private Activity mActivity;
    private MediaPlayer mAudioPlayer;
    private MusicData mMusicData;

    // 滤镜页面
    private PreviewEffectFragment mEffectFragment;

    // 预览参数
    private CameraParam mCameraParam;
    private boolean mFragmentAnimating;

    private long recordStartDelay = 300L;

    public StitchesCameraFragment() {
        mCameraParam = CameraParam.getInstance();
        mMainHandler = new WeakHandler(Looper.getMainLooper());
        mPreviewPresenter = new StitchesCameraPreviewPresenter(this);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        } else {
            mActivity = getActivity();
        }
        mPreviewPresenter.onAttach(mActivity);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreviewPresenter.onCreate();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_stitches_camera, container, false);
        return mContentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isCameraEnable()) {
            findView(mContentView);
            initPreviewSurface();
            initCameraTabView();
            initAudioPlayer();
        } else {
            PermissionUtils.requestCameraPermission(this);
        }

    }

    private void findView(View view) {
        mBack = view.findViewById(R.id.back);
        mBack.setOnClickListener(this);
        mLayoutCameraPreview = view.findViewById(R.id.layout_camera_preview);
        mRecordProgress = view.findViewById(R.id.record_progress);
        mMusic = view.findViewById(R.id.music);
        mLlvMusic = view.findViewById(R.id.llvMusic);
        mLlvMusic.setOnClickListener(this);
        mSwitchCamera = view.findViewById(R.id.switch_camera);
        mSwitchCamera.setOnClickListener(this);
        mFilter = view.findViewById(R.id.filter);
        mFilter.setOnClickListener(this);
        mSpeed = view.findViewById(R.id.speed);
        mSpeed.setOnClickListener(this);
        mFlash = view.findViewById(R.id.flash);
        mFlash.setOnClickListener(this);
        mRecordSpeedBar = view.findViewById(R.id.record_speed_bar);
        mRecordTime = view.findViewById(R.id.record_time);
        mRecordButton = view.findViewById(R.id.record_button);
        mDone = view.findViewById(R.id.done);
        mDone.setOnClickListener(this);
        mDelete = view.findViewById(R.id.delete);
        mDelete.setOnClickListener(this);
        mPhoto = view.findViewById(R.id.photo);
        mPhoto.setOnClickListener(this);
        mVBottom = view.findViewById(R.id.vBottom);
        mTabView = view.findViewById(R.id.tabView);
        mIndicator = view.findViewById(R.id.indicator);
        mFragmentBottomContainer = view.findViewById(R.id.fragment_bottom_container);

        mRecordSpeedBar.setOnSpeedChangedListener((speed) -> {
            mPreviewPresenter.setSpeedMode(SpeedMode.valueOf(speed.getSpeed()));
        });
        mRecordButton.addRecordStateListener(mRecordStateListener);
    }

    private void initPreviewSurface() {
        PreviewEngine.from(this)
                .setCameraRatio(AspectRatio.Ratio_16_9)
                .showFacePoints(false)
                .showFps(false)
                .backCamera(true)
                .setPreviewCaptureListener((path, type) -> {
                    Intent intent = new Intent(getActivity(), StitchesEditActivity.class);
                    intent.putExtra(StitchesEditActivity.VIDEO_PATH, path);
                    if (null != mMusicData) {
                        intent.putExtra(StitchesEditActivity.MUSIC_DATA, mMusicData);
                    }
                    startActivity(intent);
                });


        mCameraTextureView = new CainTextureView(mActivity);
        mCameraTextureView.addOnTouchScroller(mTouchScroller);
        mCameraTextureView.addMultiClickListener(mMultiClickListener);
        mCameraTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        mLayoutCameraPreview.addView(mCameraTextureView);

        // 添加圆角显示
        if (Build.VERSION.SDK_INT >= 21) {
            mCameraTextureView.setOutlineProvider(new RoundOutlineProvider(getResources().getDimension(com.cgfay.cameralibrary.R.dimen.dp7)));
            mCameraTextureView.setClipToOutline(true);
        }
        mLayoutCameraPreview.setOnMeasureListener(new PreviewMeasureListener(mLayoutCameraPreview));
    }


    private void initCameraTabView() {
        mTabView.addTab(mTabView.newTab().setText("拍60秒"));
        mTabView.addTab(mTabView.newTab().setText("拍15秒"), true);

        mTabView.setIndicateCenter(true);
        mTabView.setScrollAutoSelected(true);
        mTabView.addOnTabSelectedListener(new CameraTabView.OnTabSelectedListener() {
            @Override
            public void onTabSelected(CameraTabView.Tab tab) {
                int position = tab.getPosition();
                if (position == 1) {
                    mPreviewPresenter.setRecordSeconds(15);
                } else {
                    mPreviewPresenter.setRecordSeconds(60);
                }
            }

            @Override
            public void onTabUnselected(CameraTabView.Tab tab) {

            }

            @Override
            public void onTabReselected(CameraTabView.Tab tab) {

            }
        });
        mPreviewPresenter.setRecordSeconds(15);
    }

    private void initAudioPlayer() {
        mAudioPlayer = new MediaPlayer();
        mAudioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    private void setMusicDataSource(String path) {
        if (null == mAudioPlayer) return;
        if (mAudioPlayer.isPlaying()) {
            mAudioPlayer.stop();
        }
        mAudioPlayer.reset();
        try {
            mAudioPlayer.setDataSource(path);
        } catch (IOException e) {
        }
        mAudioPlayer.prepareAsync();
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
                mPreviewPresenter.changeDynamicFilter(color);
            }

            @Override
            public void onDeleteFilter() {
                mPreviewPresenter.changeDynamicFilter(null);
            }

            @Override
            public void onClose() {
                hideFragmentAnimating();
            }
        });

        if (!mEffectFragment.isAdded()) {
            getChildFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_bottom_container, mEffectFragment, FRAGMENT_TAG)
                    .commitAllowingStateLoss();
        } else {
            getChildFragmentManager()
                    .beginTransaction()
                    .show(mEffectFragment)
                    .commitAllowingStateLoss();
        }
        showFragmentAnimating();
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
                        mMusic.setText(musicData.getName());
                        mMusic.setSelected(true);
                        removeFragment(MUSIC_TAG);
                    }

                    @Override
                    public void onMusicDelete() {
                        mMusicData = null;
                        mAudioPlayer.stop();
                        resetMusic();
                        removeFragment(MUSIC_TAG);
                    }
                });
        getChildFragmentManager()
                .beginTransaction()
                .add(fragment, MUSIC_TAG)
                .commitAllowingStateLoss();
    }

    @Override
    public void onStart() {
        super.onStart();
        mPreviewPresenter.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPreviewPresenter.onResume();
    }

    /**
     * 增强光照
     */
    private void enhancementBrightness() {
        BrightnessUtils.setWindowBrightness(mActivity, mCameraParam.luminousEnhancement
                ? BrightnessUtils.MAX_BRIGHTNESS : mCameraParam.brightness);
    }

    @Override
    public void onPause() {
        super.onPause();
        DialogUtils.getInstance().dismissLoading();
        mPreviewPresenter.onPause();
        audioPause();
        if (mPreviewPresenter.isRecording()) {
            mMainHandler.post(runRecordStop);
            mRecordButton.reset();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mPreviewPresenter.onStop();

    }

    @Override
    public void onDestroyView() {
        mContentView = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mPreviewPresenter.onDestroy();
        mMainHandler.removeCallbacksAndMessages(null);
        audioRelease();
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        mPreviewPresenter.onDetach();
        mPreviewPresenter = null;
        mActivity = null;
        mAudioPlayer = null;
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                if (isFinish()) {
                    getActivity().finish();
                }
                break;
            case R.id.llvMusic:
                if (mPreviewPresenter.getRecordedVideoSize() <= 0) {
                    openMusicPicker();
                }
                break;
            case R.id.switch_camera:
                switchCamera();
                break;
            case R.id.filter:
                showEffectFragment();
                break;
            case R.id.speed:
                setSpeed();
                break;
            case R.id.flash:
                setFlash();
                break;
            case R.id.done:
                if (mDone.isSelected()) {
                    DialogUtils.getInstance().showLoading(getContext());
                    if (mPreviewPresenter.isRecording()) {
                        mMainHandler.post(runRecordStop);
                        mRecordButton.reset();
                        mMainHandler.postDelayed(runMergeAndEdit, 3000);
                    } else {
                        mPreviewPresenter.mergeAndEdit();
                    }
                }
                break;
            case R.id.delete:
                mPreviewPresenter.deleteLastVideo();
                break;
            case R.id.photo:
                showMedia();
                break;
            default:
                break;
        }
    }

    public boolean isFinish() {
        boolean isFinish = mPreviewPresenter.getDuration2US() > 0 ? false : true;
        if (!isFinish) {
            showBackDialog();
        }
        return isFinish;
    }

    private void showMedia() {
        ContentUriCompressEngine engine = ContentUriCompressEngine.getInstance();
        engine.setVideo(true);
        EasyPhotos.createAlbum(this, true, GlideEngine.getInstance())
                .filter(Type.video())
                .setVideoMaxSecond(2 * 60)
                .setVideoMinSecond(1)
                .isCompress(true)
                .setCompressEngine(engine)
                .start(callback);
    }

    private SelectCallback callback = new SelectCallback() {
        @Override
        public void onResult(ArrayList<Photo> photos, ArrayList<String> paths, boolean isOriginal) {
            if (photos.size() > 0) {
                if (EmptyUtils.isNotEmpty(photos.get(0).compressPath)) {
                    startEditActivity(photos.get(0).compressPath);
                } else {
                    ToastUtils.showShort("请重新选择视频!");
                }
            }
        }
    };

    private void startEditActivity(String path) {
        Intent intent = new Intent(getActivity(), StitchesEditActivity.class);
        intent.putExtra(StitchesEditActivity.VIDEO_PATH, path);
        startActivity(intent);
    }

    private void showBackDialog() {
        Dialog dialog = DialogUtils.getInstance().createDialogCenter(getContext(), R.layout.dialog_record_back_msg);
        ((TextView) dialog.findViewById(R.id.txMsg)).setText("是否移除当前音乐和拍摄进度?");
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
                getActivity().finish();
            }
        });
        dialog.findViewById(R.id.record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecordProgress.clear();
                mPreviewPresenter.deleteAllVideo();
                mRecordTime.setVisibility(View.INVISIBLE);
                mDone.setSelected(false);
                mDelete.setVisibility(View.GONE);
                audioSeek(0);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void audioStart() {
        if (null != mMusicData) {
            mAudioPlayer.start();
        }
    }

    private void audioPause() {
        if (null != mMusicData) {
            mAudioPlayer.pause();
        }
    }

    private void audioSeek(long time) {
        if (null != mMusicData) {
            mAudioPlayer.seekTo((int) time);
        }
    }

    private void audioRelease() {
        if (null != mAudioPlayer) {
            mAudioPlayer.release();
        }
    }

    /**
     * 设置速度
     */
    private void setSpeed() {
        if (mSpeed.isSelected()) {
            mSpeed.setSelected(false);
            mRecordSpeedBar.setVisibility(View.GONE);
        } else {
            mSpeed.setSelected(true);
            mRecordSpeedBar.setVisibility(View.VISIBLE);
        }
        updateSpeedUI();
    }

    /**
     * 设置闪光灯
     */
    private void setFlash() {
        if (mFlash.isSelected()) {
            mFlash.setSelected(false);
            mPreviewPresenter.setFlashLight(false);
        } else {
            mFlash.setSelected(true);
            mPreviewPresenter.setFlashLight(true);
        }
        updateFlashUI();
    }

    /**
     * 是否允许拍摄
     *
     * @return
     */
    private boolean isCameraEnable() {
        return PermissionUtils.permissionChecking(mActivity, Manifest.permission.CAMERA);
    }

    /**
     * 切换相机
     */
    private void switchCamera() {
        if (!isCameraEnable()) {
            PermissionUtils.requestCameraPermission(this);
            return;
        }
        mPreviewPresenter.switchCamera();
        resetFlashButton();
    }

    /**
     * 录制状态隐藏
     */
    public void hideOnRecording() {
        mMainHandler.post(() -> {
            mBack.setVisibility(View.GONE);

            mLlvMusic.setVisibility(View.GONE);
            mSwitchCamera.setVisibility(View.GONE);
            mFilter.setVisibility(View.GONE);
            mSpeed.setVisibility(View.GONE);
            mFlash.setVisibility(View.GONE);

            mRecordSpeedBar.setVisibility(View.GONE);
            mDone.setVisibility(View.GONE);
            mDelete.setVisibility(View.GONE);
            mPhoto.setVisibility(View.GONE);

            mTabView.setVisibility(View.GONE);
            mIndicator.setVisibility(View.GONE);

            mRecordTime.setVisibility(View.VISIBLE);
        });
    }

    /**
     * 滤镜状态隐藏
     */
    public void hideAllLayout() {
        mMainHandler.post(() -> {
            mBack.setVisibility(View.GONE);

            mRecordProgress.setVisibility(View.GONE);
            mLlvMusic.setVisibility(View.GONE);
            mSwitchCamera.setVisibility(View.GONE);
            mFilter.setVisibility(View.GONE);
            mSpeed.setVisibility(View.GONE);
            mFlash.setVisibility(View.GONE);

            mRecordButton.setVisibility(View.GONE);
            mRecordTime.setVisibility(View.INVISIBLE);
            mRecordSpeedBar.setVisibility(View.GONE);
            mDone.setVisibility(View.GONE);
            mDelete.setVisibility(View.GONE);
            mPhoto.setVisibility(View.GONE);

            mTabView.setVisibility(View.GONE);
            mIndicator.setVisibility(View.GONE);
        });
    }


    /**
     * 恢复所有布局
     */
    public void resetAllLayout() {
        mMainHandler.post(() -> {
            mBack.setVisibility(View.VISIBLE);

            mRecordProgress.setVisibility(View.VISIBLE);
            mLlvMusic.setVisibility(View.VISIBLE);
            mSwitchCamera.setVisibility(View.VISIBLE);
            mFilter.setVisibility(View.VISIBLE);
            mSpeed.setVisibility(View.VISIBLE);
            resetFlashButton();
            resetMusic();

            mRecordSpeedBar.setVisibility(mSpeed.isSelected() ? View.VISIBLE : View.GONE);
            mDone.setVisibility(View.VISIBLE);
            mPhoto.setVisibility(View.VISIBLE);
            resetDoneButton();
            resetDeleteButton();
            resetRecordTime();

            resetBottomTabView();

            mRecordButton.setVisibility(View.VISIBLE);
            mRecordButton.reset();
        });
    }


    /**
     * 更新录制时间
     *
     * @param duration
     */
    public void updateRecordProgress(final long duration, final float progress) {
        mMainHandler.post(() -> {
            mRecordProgress.setProgress(progress);
            if (mRecordTime.getVisibility() != View.VISIBLE)
                mRecordTime.setVisibility(View.VISIBLE);
            long totalDuration = mPreviewPresenter.getDuration2US() + duration;
            mRecordTime.setText(getRecordTime(totalDuration) + "");
            if (totalDuration >= 5 * HWMediaRecorder.SECOND_IN_US) {
                mDone.setVisibility(View.VISIBLE);
                mDone.setSelected(true);
            }
        });
    }

    /**
     * 添加一段进度
     *
     * @param progress
     */
    public void addProgressSegment(float progress) {
        mMainHandler.post(() -> {
            mRecordProgress.addProgressSegment(progress);
        });
    }

    /**
     * 删除一段进度
     */
    public void deleteProgressSegment() {
        mMainHandler.post(() -> {
            mRecordProgress.deleteProgressSegment();
            audioSeek(mPreviewPresenter.getVideoDuration());
            resetDoneButton();
            resetDeleteButton();
            resetRecordTime();
            resetMusic();
            resetBottomTabView();
        });
    }

    /**
     * 复位闪光灯按钮
     */
    private void resetFlashButton() {
        boolean isSupportFlashLight = mPreviewPresenter.isSupportFlashLight();
        mFlash.setVisibility(isSupportFlashLight ? View.VISIBLE : View.GONE);
        if (!isSupportFlashLight) {
            mFlash.setSelected(false);
            updateFlashUI();
        }
    }

    /**
     * 更新设置闪光灯值
     */
    private void updateFlashUI() {
        if (mFlash.isSelected()) {
            setDrawable(mFlash, R.mipmap.ic_flash_on);
        } else {
            setDrawable(mFlash, R.mipmap.ic_flash_off);
        }
    }

    /**
     * 更新设置速度值
     */
    private void updateSpeedUI() {
        if (mSpeed.isSelected()) {
            setDrawable(mSpeed, R.mipmap.ic_speed_on);
            mSpeed.setText("速度开");
        } else {
            setDrawable(mSpeed, R.mipmap.ic_speed_off);
            mSpeed.setText("速度关");
        }
    }


    /**
     * 设置drawableTop
     *
     * @param view
     * @param drawableRes
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setDrawable(TextView view, int drawableRes) {
        Drawable drawable = getResources().getDrawable(drawableRes, null);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        view.setCompoundDrawables(null, drawable, null, null);
    }

    /**
     * 复位删除按钮
     */
    private void resetDeleteButton() {
        boolean hasRecordVideo = (mPreviewPresenter.getRecordedVideoSize() > 0);
        mDelete.setVisibility(hasRecordVideo ? View.VISIBLE : View.GONE);
    }

    /**
     * 复位音乐
     */
    private void resetMusic() {
        boolean hasRecordVideo = (mPreviewPresenter.getRecordedVideoSize() > 0);
        if (!hasRecordVideo) {
            mMusic.setText(null == mMusicData ? "选择音乐" : mMusicData.getName());
            mMusic.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            mMusic.setSelected(false);
        } else {
            mMusic.setTextColor(ContextCompat.getColor(getContext(), R.color.color_c7d1cb));
            mMusic.setSelected(true);
        }
    }

    /**
     * 复位完成按钮
     */
    private void resetDoneButton() {
        mDone.setSelected(mPreviewPresenter.isVideoDurationEnough() ? true : false);
    }

    /**
     * 复位时间
     */
    private void resetRecordTime() {
        double time = getRecordTime();
        mRecordTime.setVisibility(time > 0 ? View.VISIBLE : View.INVISIBLE);
        mRecordTime.setText(time + "");
    }

    /**
     * 获取录制时间(秒包含1位小数点)
     *
     * @return
     */
    private double getRecordTime() {
        return BigDecimalUtils.decimalOne(BigDecimalUtils.division(mPreviewPresenter.getDuration2US(), HWMediaRecorder.SECOND_IN_US));
    }

    private double getRecordTime(long duration) {
        return BigDecimalUtils.decimalOne(BigDecimalUtils.division(duration, HWMediaRecorder.SECOND_IN_US));
    }

    /**
     * 复位底部时间选择
     */
    private void resetBottomTabView() {
        mTabView.setVisibility(mPreviewPresenter.getDuration2US() > 0 ? View.GONE : View.VISIBLE);
        mIndicator.setVisibility(mPreviewPresenter.getDuration2US() > 0 ? View.GONE : View.VISIBLE);
    }

    /**
     * 处理返回按钮事件
     *
     * @return 是否拦截返回按键事件
     */
    public boolean onBackPressed() {
        Fragment fragment = getChildFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment != null) {
            hideFragmentAnimating();
            return true;
        }
        return false;
    }

    /**
     * 移除Fragment
     */
    private void removeFragment(String tag) {
        Fragment fragment = getChildFragmentManager().findFragmentByTag(tag);
        if (fragment != null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .remove(fragment)
                    .commitAllowingStateLoss();
        }
    }

    /**
     * 显示合成进度
     */
    public void showConcatProgressDialog() {
        mMainHandler.post(() -> {
            DialogUtils.getInstance().showLoading(getContext());
        });
    }

    /**
     * 隐藏合成进度
     */
    public void hideConcatProgressDialog() {
        mMainHandler.post(() -> {
            DialogUtils.getInstance().dismissLoading();
        });
    }

    /**
     * 显示Toast提示
     *
     * @param msg
     */
    public void showToast(String msg) {
        mMainHandler.post(() -> {
            ToastUtils.showShort(msg);
        });
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
        Animation animation = AnimationUtils.loadAnimation(mActivity, com.cgfay.cameralibrary.R.anim.preview_slide_up);
        mFragmentBottomContainer.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mFragmentAnimating = false;
                hideAllLayout();
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
        Animation animation = AnimationUtils.loadAnimation(mActivity, com.cgfay.cameralibrary.R.anim.preivew_slide_down);
        mFragmentBottomContainer.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                resetAllLayout();
                removeFragment(FRAGMENT_TAG);
                mFragmentAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    Runnable runRecordStart = new Runnable() {
        @Override
        public void run() {
            mPreviewPresenter.startRecord();
            audioStart();
        }
    };

    Runnable runRecordStop = new Runnable() {
        @Override
        public void run() {
            mPreviewPresenter.stopRecord();
            audioPause();
        }
    };

    Runnable runMergeAndEdit = new Runnable() {
        @Override
        public void run() {
            mPreviewPresenter.mergeAndEdit();
        }
    };

    /**
     * 录制监听器回调
     */
    private RecordButton.RecordStateListener mRecordStateListener = new RecordButton.RecordStateListener() {
        @Override
        public boolean onRecordEnable() {
            return mPreviewPresenter.isRecordEnable();
        }

        @Override
        public void onRecordStart() {
            mMainHandler.removeCallbacks(runRecordStart);
            mMainHandler.post(runRecordStart);
        }

        @Override
        public void onRecordStop() {
            mMainHandler.removeCallbacks(runRecordStop);
            mMainHandler.post(runRecordStop);
        }

        @Override
        public void onZoom(float percent) {

        }
    };

    // ------------------------------- TextureView 滑动、点击回调 ----------------------------------
    private CainTextureView.OnTouchScroller mTouchScroller = new CainTextureView.OnTouchScroller() {

        @Override
        public void swipeBack() {
//            mPreviewPresenter.nextFilter();
        }

        @Override
        public void swipeFrontal() {
//            mPreviewPresenter.previewFilter();
        }

        @Override
        public void swipeUpper(boolean startInLeft, float distance) {
            if (VERBOSE) {

            }
        }

        @Override
        public void swipeDown(boolean startInLeft, float distance) {
            if (VERBOSE) {

            }
        }

    };

    /**
     * 单双击回调监听
     */
    private CainTextureView.OnMultiClickListener mMultiClickListener = new CainTextureView.OnMultiClickListener() {

        @Override
        public void onSurfaceSingleClick(final float x, final float y) {
            // 处理浮窗Fragment
            if (onBackPressed()) {
                return;
            }

            // 如果处于触屏拍照状态，则直接拍照，不做对焦处理
            if (mCameraParam.touchTake) {
//                takePicture();
                return;
            }

            // todo 判断是否支持对焦模式

        }

        @Override
        public void onSurfaceDoubleClick(float x, float y) {
//            if (ViewClickUtils.onDoubleClick()) return;
//            switchCamera();
        }

    };

    // ---------------------------- TextureView SurfaceTexture监听 ---------------------------------
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mPreviewPresenter.onSurfaceCreated(surface);
            mPreviewPresenter.onSurfaceChanged(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            mPreviewPresenter.onSurfaceChanged(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            mPreviewPresenter.onSurfaceDestroyed();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

}