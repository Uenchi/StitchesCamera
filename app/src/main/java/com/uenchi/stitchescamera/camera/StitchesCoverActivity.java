package com.uenchi.stitchescamera.camera;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.uenchi.stitchescamera.Const;
import com.uenchi.stitchescamera.R;
import com.uenchi.stitchescamera.camera.adapter.SelCoverAdapter;
import com.uenchi.stitchescamera.camera.view.ThumbnailSelTimeView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : uenchi
 * @date : 2021/4/12
 * @desc :
 */
public class StitchesCoverActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String CUT_TIME = "cut_time";
    public static final int COMR_FROM_SEL_COVER_TIME_ACTIVITY = 1;

    private static final int SEL_TIME = 0;
    private static final int SUBMIT = 1;
    private static final int SAVE_BITMAP = 2;

    private ImageView mBack;
    // 完成
    private TextView mNext;
    private VideoView mVideoView;
    private RecyclerView mRecyclerView;
    private ThumbnailSelTimeView mThumbnailSelTimeView;
    private FrameLayout mSelCoverFrameLayout;

    private String mVideoPath = "";
    private float mSelStartTime = 0.5f;
    private boolean mIsSelTime;//是否点了完成按钮
    public String mVideoRotation;
    private List<Bitmap> mBitmapList = new ArrayList<>();

    public SelCoverAdapter mSelCoverAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stitches_cover);
        findView();
        mVideoPath = getIntent().getStringExtra(Const.INTENT_DATA);
        initThumbs();
        initSetParam();
        initView();
        initListener();
    }

    private void findView() {
        mBack = findViewById(R.id.back);
        mBack.setOnClickListener(this);
        mNext = findViewById(R.id.next);
        mNext.setOnClickListener(this);
        mVideoView = findViewById(R.id.videoView);
        mRecyclerView = findViewById(R.id.recyclerView);
        mThumbnailSelTimeView = findViewById(R.id.thumbnailSelTimeView);
        mSelCoverFrameLayout = findViewById(R.id.sel_cover_frame_layout);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.next:
                mIsSelTime = true;
                onBackPressed();
                break;
            default:
                break;
        }
    }

    private void initListener() {
        mThumbnailSelTimeView.setOnScrollBorderListener(new ThumbnailSelTimeView.OnScrollBorderListener() {
            @Override
            public void OnScrollBorder(float start, float end) {
            }

            @Override
            public void onScrollStateChange() {
                myHandler.removeMessages(SEL_TIME);
                float rectLeft = mThumbnailSelTimeView.getRectLeft();
                mSelStartTime = (mVideoDuration * rectLeft) / 1000;
                Log.e("Atest", "onScrollStateChange: " + mSelStartTime);
                mVideoView.seekTo((int) mSelStartTime);
                myHandler.sendEmptyMessage(SEL_TIME);
            }
        });
    }

    private void initSetParam() {
        ViewGroup.LayoutParams layoutParams = mVideoView.getLayoutParams();
        if (mVideoRotation.equals("0") && mVideoWidth > mVideoHeight) {//本地视频横屏 0表示竖屏
            layoutParams.width = 1120;
            layoutParams.height = 630;
        } else {
            layoutParams.width = 630;
            layoutParams.height = 1120;
        }

        mVideoView.setLayoutParams(layoutParams);
        mVideoView.setVideoPath(mVideoPath);
        mVideoView.start();
        mVideoView.getDuration();
    }

    private void initView() {
        mSelCoverAdapter = new SelCoverAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false) {
            @Override
            public boolean canScrollHorizontally() {
                return true;
            }
        };
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mSelCoverAdapter);
    }

    public int mVideoHeight, mVideoWidth, mVideoDuration;

    private void initThumbs() {
        final MediaMetadataRetriever mediaMetadata = new MediaMetadataRetriever();
        mediaMetadata.setDataSource(this, Uri.parse(mVideoPath));
        mVideoRotation = mediaMetadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        mVideoWidth = Integer.parseInt(mediaMetadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        mVideoHeight = Integer.parseInt(mediaMetadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        mVideoDuration = Integer.parseInt(mediaMetadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        final int frame = 10;
        final int frameTime = mVideoDuration / frame * 1000;
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                for (int x = 0; x < frame; x++) {
                    Bitmap bitmap = mediaMetadata.getFrameAtTime(frameTime * x, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                    Message msg = myHandler.obtainMessage();
                    msg.what = SAVE_BITMAP;
                    msg.obj = bitmap;
                    msg.arg1 = x;
                    myHandler.sendMessage(msg);
                }
                mediaMetadata.release();
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                myHandler.sendEmptyMessage(SUBMIT);
            }
        }.execute();
    }

    private Handler myHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        private WeakReference<StitchesCoverActivity> mActivityWeakReference;

        public MyHandler(StitchesCoverActivity activityWeakReference) {
            mActivityWeakReference = new WeakReference<StitchesCoverActivity>(activityWeakReference);
        }

        @Override
        public void handleMessage(Message msg) {
            StitchesCoverActivity activity = mActivityWeakReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case SEL_TIME:
                        activity.mVideoView.seekTo((int) activity.mSelStartTime * 1000);
                        activity.mVideoView.start();
                        sendEmptyMessageDelayed(SEL_TIME, 1000);
                        break;
                    case SAVE_BITMAP:
                        activity.mBitmapList.add(msg.arg1, (Bitmap) msg.obj);
                        break;
                    case SUBMIT:
                        activity.mSelCoverAdapter.setList(activity.mBitmapList);
                        sendEmptyMessageDelayed(SEL_TIME, 1000);
                        break;
                }
            }
        }
    }


    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        if (mIsSelTime) {
            if (mSelStartTime < 0.5f) {
                mSelStartTime = 0.5f;
            }
            intent.putExtra(CUT_TIME, mSelStartTime);
        } else {
            intent.putExtra(CUT_TIME, 0.5f);
        }
        setResult(COMR_FROM_SEL_COVER_TIME_ACTIVITY, intent);
        super.onBackPressed();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new ContextWrapper(newBase) {
            @Override
            public Object getSystemService(String name) {
                if (Context.AUDIO_SERVICE.equals(name))
                    return getApplicationContext().getSystemService(name);
                return super.getSystemService(name);
            }
        });
    }
}