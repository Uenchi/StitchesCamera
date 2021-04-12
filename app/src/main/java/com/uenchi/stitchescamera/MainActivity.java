package com.uenchi.stitchescamera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.blankj.utilcode.util.PermissionUtils;
import com.cgfay.filter.glfilter.resource.FilterHelper;
import com.uenchi.editlibrary.effect.EffectFilterHelper;
import com.uenchi.stitchescamera.camera.StitchesCameraActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestAllPermission();
            }
        });
    }


    /**
     * 请求全部权限（点击拍摄按钮）
     */
    @SuppressLint("WrongConstant")
    private void requestAllPermission() {
        PermissionUtils.permission(Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE).callback(new PermissionUtils.SingleCallback() {
            @Override
            public void callback(boolean isAllGranted, @NonNull List<String> granted, @NonNull List<String> deniedForever, @NonNull List<String> denied) {
                if (isAllGranted) {
                    initResource();
                    startActivity(new Intent(MainActivity.this, StitchesCameraActivity.class));
                }
            }
        }).request();
    }

    /**
     * 初始化拍摄所需资源
     */
    private void initResource() {
        FilterHelper.initAssetsFilter(this);
        EffectFilterHelper.initAssetsResource();
    }
}