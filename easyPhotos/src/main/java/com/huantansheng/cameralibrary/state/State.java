package com.huantansheng.cameralibrary.state;

import android.view.Surface;
import android.view.SurfaceHolder;

import com.huantansheng.cameralibrary.CameraInterface;

/**
 * =====================================
 * 作    者: 陈嘉桐
 * 版    本：1.1.4
 * 创建日期：2017/9/8
 * 描    述：
 * =====================================
 */
public interface State {

    void start(SurfaceHolder holder, float screenProp);

    void stop();

    void foucs(float x, float y, CameraInterface.FocusCallback callback);

    void swtich(SurfaceHolder holder, float screenProp);

    void restart();

    void capture();

    void record(Surface surface, float screenProp);

    void stopRecord(boolean isShort, long time);

    void cancel(SurfaceHolder holder, float screenProp);

    void confirm();

    void zoom(float zoom, int type);

    void flash(String mode);
}
