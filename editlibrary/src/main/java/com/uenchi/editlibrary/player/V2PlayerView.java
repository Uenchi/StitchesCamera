package com.uenchi.editlibrary.player;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;


public class V2PlayerView extends MPlayerView {
    private static Handler uiHandler = new Handler(Looper.getMainLooper());

    private PositionChangeListener mPositionChangeListener;

    public V2PlayerView(Context context) {
        super(context);
    }

    public V2PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setPositionChangeListener(PositionChangeListener positionChangeListener) {
        mPositionChangeListener = positionChangeListener;
        uiHandler.post(mRunnable);
    }

    public interface PositionChangeListener{
        void onPositionChange(long curPosition);
    }

    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            checkPlayProgress();
        }
    };

    private void checkPlayProgress(){
        if (!notDestroyed) {
            if (null != uiHandler){
                uiHandler.removeCallbacks(mRunnable);
            }
            return;
        }

        if (mPositionChangeListener != null && mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            currentPostion = mMediaPlayer.getCurrentPosition();
            mPositionChangeListener.onPositionChange(currentPostion);
            uiHandler.postDelayed(mRunnable, 10);
        }else {
            uiHandler.postDelayed(mRunnable, 200);
        }
    }
}
