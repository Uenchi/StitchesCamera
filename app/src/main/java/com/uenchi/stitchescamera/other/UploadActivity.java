package com.uenchi.stitchescamera.other;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.PathUtils;
import com.cgfay.media.recorder.HWMediaRecorder;
import com.dueeeke.videoplayer.player.VideoView;
import com.uenchi.stitchescamera.Const;
import com.uenchi.stitchescamera.R;
import com.uenchi.stitchescamera.camera.StitchesCoverActivity;
import com.uenchi.stitchescamera.utils.EmptyUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * @author : uenchi
 * @date : 2021/4/12
 * @desc :
 */
public class UploadActivity extends AppCompatActivity {

    VideoView mVideoView;
    ImageView mImgCover;
    String videoPath;
    //封面选择的时长
    private float fSelTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        videoPath = getIntent().getStringExtra(Const.INTENT_VIDEO_PATH);

        initView();
        playVideo(videoPath);

    }

    private void initView() {
        mVideoView = findViewById(R.id.videoView);
        mImgCover = findViewById(R.id.imgCover);
        findViewById(R.id.btCover).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UploadActivity.this, StitchesCoverActivity.class);
                intent.putExtra(Const.INTENT_DATA, videoPath);
                startActivityForResult(intent, StitchesCoverActivity.COMR_FROM_SEL_COVER_TIME_ACTIVITY);
            }
        });
    }

    private void playVideo(String videoPath) {
        mVideoView.setLooping(true);
        mVideoView.setUrl(videoPath);
        mVideoView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (EmptyUtils.isNotEmpty(mVideoView)){
            mVideoView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (EmptyUtils.isNotEmpty(mVideoView)){
            mVideoView.resume();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == StitchesCoverActivity.COMR_FROM_SEL_COVER_TIME_ACTIVITY) {
            float time = data.getFloatExtra(StitchesCoverActivity.CUT_TIME, 0.5f);
            if (time > 0.5) {
                fSelTime = time;
                videoFrame();
            }
        }
    }


    private void videoFrame() {
        Bitmap mBitmap = getVideoFrame(videoPath, fSelTime); //获取视频第一帧
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        if (EmptyUtils.isNotEmpty(mBitmap))
            mImgCover.setImageBitmap(mBitmap);

    }

    private Bitmap getVideoFrame(String path, float time) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        Bitmap mBitmap = mmr.getFrameAtTime((int) (time * HWMediaRecorder.SECOND_IN_US));
        return mBitmap;
    }
}
