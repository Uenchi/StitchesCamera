package com.uenchi.editlibrary.composer;

import android.media.MediaMetadataRetriever;
import android.util.Log;

import androidx.annotation.NonNull;

import com.uenchi.editlibrary.filter.GlFilter;
import com.uenchi.editlibrary.model.FillMode;
import com.uenchi.editlibrary.model.FillModeCustomItem;
import com.uenchi.editlibrary.model.GlFilterList;
import com.uenchi.editlibrary.model.Resolution;
import com.uenchi.editlibrary.model.Rotation;
import com.uenchi.editlibrary.model.VideoFormatMimeType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by sudamasayuki on 2017/11/15.
 */

public class Mp4Composer {

    private final static String TAG = Mp4Composer.class.getSimpleName();

    private final String srcPath;
    private final String destPath;
    private GlFilterList filterList;
    private Resolution outputResolution;
    private int bitrate = -1;
    private int frameRate = 30;
    private boolean mute = false;
    private Rotation rotation = Rotation.NORMAL;
    private Listener listener;
    private FillMode fillMode = FillMode.PRESERVE_ASPECT_FIT;
    private FillModeCustomItem fillModeCustomItem;
    private VideoFormatMimeType videoFormatMimeType = VideoFormatMimeType.AUTO;
    private int timeScale = 1;
    private float resolutionScale = 1f;
    private long clipStartMs, clipEndMs;
    private boolean flipVertical = false;
    private boolean flipHorizontal = false;

    private ExecutorService executorService;


    public Mp4Composer(@NonNull final String srcPath, @NonNull final String destPath) {
        this.srcPath = srcPath;
        this.destPath = destPath;
    }

    public Mp4Composer filterList(@NonNull GlFilterList filterList) {
        this.filterList = filterList;
        Log.d(TAG, "set filterList = " + this.filterList);
        return this;
    }

    public Mp4Composer size(int width, int height) {
        this.outputResolution = new Resolution(width, height);
        return this;
    }

    public Mp4Composer clip(long start, long end) {
        this.clipStartMs = start;
        this.clipEndMs = end;
        return this;
    }

    public Mp4Composer videoBitrate(int bitrate) {
        this.bitrate = bitrate;
        return this;
    }

    public Mp4Composer mute(boolean mute) {
        this.mute = mute;
        return this;
    }

//    public Mp4Composer frameRate(int value) {
//        this.frameRate = value;
//        return this;
//    }

    public Mp4Composer flipVertical(boolean flipVertical) {
        this.flipVertical = flipVertical;
        return this;
    }

    public Mp4Composer flipHorizontal(boolean flipHorizontal) {
        this.flipHorizontal = flipHorizontal;
        return this;
    }

    public Mp4Composer rotation(@NonNull Rotation rotation) {
        this.rotation = rotation;
        return this;
    }

    public Mp4Composer fillMode(@NonNull FillMode fillMode) {
        this.fillMode = fillMode;
        return this;
    }

    public Mp4Composer customFillMode(@NonNull FillModeCustomItem fillModeCustomItem) {
        this.fillModeCustomItem = fillModeCustomItem;
        this.fillMode = FillMode.CUSTOM;
        return this;
    }


    public Mp4Composer listener(@NonNull Listener listener) {
        this.listener = listener;
        return this;
    }

    public Mp4Composer timeScale(final int timeScale) {
        this.timeScale = timeScale;
        return this;
    }

    public void videoFormatMimeType(VideoFormatMimeType videoFormatMimeType) {
        this.videoFormatMimeType = videoFormatMimeType;
    }

    private ExecutorService getExecutorService() {
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }
        return executorService;
    }


    public Mp4Composer start() {
        getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                Mp4ComposerEngine engine = new Mp4ComposerEngine();

                engine.setProgressCallback(new Mp4ComposerEngine.ProgressCallback() {
                    @Override
                    public void onProgress(final double progress) {
                        if (listener != null) {
                            listener.onProgress(progress);
                        }
                    }
                });

                File outFile = new File(destPath);
                if (outFile.exists()) {
                    outFile.delete();
                }

                final File srcFile = new File(srcPath);
                final FileInputStream fileInputStream;
                try {
                    fileInputStream = new FileInputStream(srcFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    if (listener != null) {
                        listener.onFailed(e);
                    }
                    return;
                }

                try {
                    engine.setDataSource(fileInputStream.getFD());
                } catch (IOException e) {
                    e.printStackTrace();
                    if (listener != null) {
                        listener.onFailed(e);
                    }
                    return;
                }

                final int videoRotate = getVideoRotation(srcPath);
                final Resolution srcVideoResolution = getVideoResolution(srcPath, videoRotate);

                if (fillMode == null) {
                    fillMode = FillMode.PRESERVE_ASPECT_FIT;
                }

                if (fillModeCustomItem != null) {
                    fillMode = FillMode.CUSTOM;
                }

                if (outputResolution == null) {
                    if (fillMode == FillMode.CUSTOM) {
                        outputResolution = srcVideoResolution;
                    } else {
                        Rotation rotate = Rotation.fromInt(rotation.getRotation() + videoRotate);
                        if (rotate == Rotation.ROTATION_90 || rotate == Rotation.ROTATION_270) {
                            outputResolution = new Resolution(srcVideoResolution.height(), srcVideoResolution.width());
                        } else {
                            outputResolution = srcVideoResolution;
                        }
                    }
                }

                if (timeScale < 2) {
                    timeScale = 1;
                }

                Log.d(TAG, "filterList = " + filterList);
                Log.d(TAG, "rotation = " + (rotation.getRotation() + videoRotate));
                Log.d(TAG, "inputResolution width = " + srcVideoResolution.width() + " height = " + srcVideoResolution.height());
                outputResolution = new Resolution((int) (outputResolution.width() * resolutionScale), (int) (outputResolution.height() * resolutionScale));
                Log.d(TAG, "outputResolution width = " + outputResolution.width() + " height = " + outputResolution.height());
                Log.d(TAG, "fillMode = " + fillMode);

                try {
                    if (bitrate < 0) {
                        bitrate = calcBitRate(outputResolution.width(), outputResolution.height());
                    }
                    engine.compose(
                            destPath,
                            outputResolution,
                            filterList,
                            bitrate,
                            frameRate,
                            mute,
                            Rotation.fromInt(rotation.getRotation() + videoRotate),
                            srcVideoResolution,
                            fillMode,
                            fillModeCustomItem,
                            videoFormatMimeType,
                            timeScale,
                            flipVertical,
                            flipHorizontal,
                            clipStartMs,
                            clipEndMs
                    );

                } catch (Exception e) {
                    e.printStackTrace();
                    if (listener != null) {
                        listener.onFailed(e);
                    }
                    executorService.shutdown();
                    return;
                }

                if (listener != null) {
                    listener.onCompleted();
                }
                executorService.shutdown();
            }
        });

        return this;
    }

    public void cancel() {
        getExecutorService().shutdownNow();
    }


    public interface Listener {
        /**
         * Called to notify progress.
         *
         * @param progress Progress in [0.0, 1.0] range, or negative value if progress is unknown.
         */
        void onProgress(double progress);

        /**
         * Called when transcode completed.
         */
        void onCompleted();

        /**
         * Called when transcode canceled.
         */
        void onCanceled();


        void onFailed(Exception exception);
    }

    private int getVideoRotation(String videoFilePath) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            mediaMetadataRetriever.setDataSource(videoFilePath);
        } catch (Exception e) {
            Log.e("MediaMetadataRetriever", "getVideoRotation error");
            return 0;
        }
        String orientation = mediaMetadataRetriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        return Integer.valueOf(orientation);
    }

    private int calcBitRate(int width, int height) {
        final int bitrate = (int) (0.25 * 30 * width * height);
        Log.i(TAG, "bitrate=" + bitrate);
        return bitrate;
    }

    private Resolution getVideoResolution(final String path, final int rotation) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        int width = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        int height = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        retriever.release();

        return new Resolution(width, height);
    }

}
