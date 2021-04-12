package com.uenchi.stitchescamera.camera;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.noober.background.BackgroundLibrary;
import com.uenchi.stitchescamera.R;
import com.uenchi.stitchescamera.utils.EmptyUtils;

/**
 * @author : uenchi
 * @date : 2021/4/9
 * @desc :
 */
public class StitchesCameraActivity extends AppCompatActivity {
    private static final String FRAGMENT_CAMERA = "fragment_camera";

    private StitchesCameraFragment mPreviewFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BackgroundLibrary.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stitches_camera);
        if (null == savedInstanceState && mPreviewFragment == null) {
            mPreviewFragment = new StitchesCameraFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, mPreviewFragment, FRAGMENT_CAMERA)
                    .commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (EmptyUtils.isNotEmpty(mPreviewFragment) && !mPreviewFragment.isFinish()) {
            return;
        }
        super.onBackPressed();
    }
}
