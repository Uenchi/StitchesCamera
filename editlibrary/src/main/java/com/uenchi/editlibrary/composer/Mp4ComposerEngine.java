package com.uenchi.editlibrary.composer;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.os.Build;
import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;

import com.uenchi.editlibrary.filter.GlFilter;
import com.uenchi.editlibrary.model.FillMode;
import com.uenchi.editlibrary.model.FillModeCustomItem;
import com.uenchi.editlibrary.model.GlFilterList;
import com.uenchi.editlibrary.model.Resolution;
import com.uenchi.editlibrary.model.Rotation;
import com.uenchi.editlibrary.model.VideoFormatMimeType;

import java.io.FileDescriptor;
import java.io.IOException;

// Refer: https://github.com/ypresto/android-transcoder/blob/master/lib/src/main/java/net/ypresto/androidtranscoder/engine/MediaTranscoderEngine.java

/**
 * Internal engine, do not use this directly.
 */
class Mp4ComposerEngine {
    private static final String TAG = "Mp4ComposerEngine";
    private static final double PROGRESS_UNKNOWN = -1.0;
    private static final long SLEEP_TO_WAIT_TRACK_TRANSCODERS = 10;
    private static final long PROGRESS_INTERVAL_STEPS = 10;
    private FileDescriptor inputFileDescriptor;
    private VideoComposer videoComposer;
    private IAudioComposer audioComposer;
    private MediaExtractor mediaExtractor;
    private MediaMuxer mediaMuxer;
    private ProgressCallback progressCallback;
    private long durationUs;


    void setDataSource(FileDescriptor fileDescriptor) {
        inputFileDescriptor = fileDescriptor;
    }

    void setProgressCallback(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }


    void compose(
            final String destPath,
            final Resolution outputResolution,
            final GlFilterList filterList,
            final int bitrate,
            final int frameRate,
            final boolean mute,
            final Rotation rotation,
            final Resolution inputResolution,
            final FillMode fillMode,
            final FillModeCustomItem fillModeCustomItem,
            final VideoFormatMimeType videoFormatMimeType,
            final int timeScale,
            final boolean flipVertical,
            final boolean flipHorizontal,
            final long startTimeMs,
            final long endTimeMs
    ) throws IOException {


        try {
            mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(inputFileDescriptor);
            mediaMuxer = new MediaMuxer(destPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(inputFileDescriptor);
            if (startTimeMs >= 0 && endTimeMs > startTimeMs) {
                durationUs = endTimeMs - startTimeMs;
            } else {
                try {
                    durationUs = Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) * 1000;
                } catch (NumberFormatException e) {
                    durationUs = -1;
                }
            }

            Log.d(TAG, "Duration (us): " + durationUs);

            MediaFormat videoOutputFormat = createVideoOutputFormatWithAvailableEncoders(videoFormatMimeType, bitrate, outputResolution);

            MuxRender muxRender = new MuxRender(mediaMuxer);

            // identify track indices
            MediaFormat format = mediaExtractor.getTrackFormat(0);
            String mime = format.getString(MediaFormat.KEY_MIME);

            final int videoTrackIndex;
            final int audioTrackIndex;

            if (mime.startsWith("video/")) {
                videoTrackIndex = 0;
                audioTrackIndex = 1;
            } else {
                videoTrackIndex = 1;
                audioTrackIndex = 0;
            }

            // setup video composer
            videoComposer = new VideoComposer(mediaExtractor, videoTrackIndex, videoOutputFormat, muxRender, timeScale);
            videoComposer.setUp(filterList, rotation, outputResolution, inputResolution, fillMode, fillModeCustomItem, flipVertical, flipHorizontal);
            mediaExtractor.selectTrack(videoTrackIndex);

            if (startTimeMs >= 0 && endTimeMs > startTimeMs) {
                videoComposer.setClipRange(startTimeMs, endTimeMs);
            }


            // setup audio if present and not muted
            if (mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO) != null && !mute) {
                // has Audio video

                if (timeScale < 2) {
                    audioComposer = new AudioComposer(mediaExtractor, audioTrackIndex, muxRender);
                    if (startTimeMs >= 0 && endTimeMs > startTimeMs) {
                        ((AudioComposer) audioComposer).setClipRange(startTimeMs, endTimeMs);
                    }
                } else {
                    audioComposer = new RemixAudioComposer(mediaExtractor, audioTrackIndex, mediaExtractor.getTrackFormat(audioTrackIndex), muxRender, timeScale);
                }


                audioComposer.setup();

                mediaExtractor.selectTrack(audioTrackIndex);

                runPipelines();
            } else {
                // no audio video
                runPipelinesNoAudio();
            }


            mediaMuxer.stop();
        } finally {
            try {
                if (videoComposer != null) {
                    videoComposer.release();
                    videoComposer = null;
                }
                if (audioComposer != null) {
                    audioComposer.release();
                    audioComposer = null;
                }
                if (mediaExtractor != null) {
                    mediaExtractor.release();
                    mediaExtractor = null;
                }
            } catch (RuntimeException e) {
                // Too fatal to make alive the app, because it may leak native resources.
                //noinspection ThrowFromFinallyBlock
                throw new Error("Could not shutdown mediaExtractor, codecs and mediaMuxer pipeline.", e);
            }
            try {
                if (mediaMuxer != null) {
                    mediaMuxer.release();
                    mediaMuxer = null;
                }
            } catch (RuntimeException e) {
                Log.e(TAG, "Failed to release mediaMuxer.", e);
            }
        }


    }

    @NonNull
    private static MediaFormat createVideoOutputFormatWithAvailableEncoders(@NonNull final VideoFormatMimeType mimeType,
                                                                            final int bitrate,
                                                                            @NonNull final Resolution outputResolution) {
        final MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);

        if (mimeType != VideoFormatMimeType.AUTO) {
            final MediaFormat mediaFormat = createVideoFormat(mimeType.getFormat(), bitrate, outputResolution);
            if (mediaCodecList.findEncoderForFormat(mediaFormat) != null) {
                return mediaFormat;
            }
        }

        final MediaFormat hevcMediaFormat = createVideoFormat(VideoFormatMimeType.HEVC.getFormat(), bitrate, outputResolution);
        if (mediaCodecList.findEncoderForFormat(hevcMediaFormat) != null) {
            return hevcMediaFormat;
        }

        final MediaFormat avcMediaFormat = createVideoFormat(VideoFormatMimeType.AVC.getFormat(), bitrate, outputResolution);
        if (mediaCodecList.findEncoderForFormat(avcMediaFormat) != null) {
            return avcMediaFormat;
        }

        final MediaFormat mp4vesMediaFormat = createVideoFormat(VideoFormatMimeType.MPEG4.getFormat(), bitrate, outputResolution);
        if (mediaCodecList.findEncoderForFormat(mp4vesMediaFormat) != null) {
            return mp4vesMediaFormat;
        }

        return createVideoFormat(VideoFormatMimeType.H263.getFormat(), bitrate, outputResolution);
    }

    @NonNull
    private static MediaFormat createVideoFormat(@NonNull final String mimeType,
                                                 final int bitrate,
                                                 @NonNull final Resolution outputResolution) {
        final MediaFormat outputFormat =
                MediaFormat.createVideoFormat(mimeType,
                        outputResolution.width(),
                        outputResolution.height());

        outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        // On Build.VERSION_CODES.LOLLIPOP, format must not contain a MediaFormat#KEY_FRAME_RATE.
        // https://developer.android.com/reference/android/media/MediaCodecInfo.CodecCapabilities.html#isFormatSupported(android.media.MediaFormat)
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.LOLLIPOP) {
            // Required but ignored by the encoder
            outputFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        }
        outputFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        outputFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);

        return outputFormat;
    }

    private void runPipelines() {
        long loopCount = 0;
        if (durationUs <= 0) {
            if (progressCallback != null) {
                progressCallback.onProgress(PROGRESS_UNKNOWN);
            }// unknown
        }
        while (!(videoComposer.isFinished() && audioComposer.isFinished())) {
            boolean stepped = videoComposer.stepPipeline()
                    || audioComposer.stepPipeline();
            loopCount++;
            if (durationUs > 0 && loopCount % PROGRESS_INTERVAL_STEPS == 0) {
                double videoProgress = videoComposer.isFinished() ? 1.0 : Math.min(1.0, (double) videoComposer.getWrittenPresentationTimeUs() / durationUs);
                double audioProgress = audioComposer.isFinished() ? 1.0 : Math.min(1.0, (double) audioComposer.getWrittenPresentationTimeUs() / durationUs);
                double progress = (videoProgress + audioProgress) / 2.0;
                if (progressCallback != null) {
                    progressCallback.onProgress(progress);
                }
            }
            if (!stepped) {
                try {
                    Thread.sleep(SLEEP_TO_WAIT_TRACK_TRANSCODERS);
                } catch (InterruptedException e) {
                    // nothing to do
                }
            }
        }
    }

    private void runPipelinesNoAudio() {
        long loopCount = 0;
        if (durationUs <= 0) {
            if (progressCallback != null) {
                progressCallback.onProgress(PROGRESS_UNKNOWN);
            } // unknown
        }
        while (!videoComposer.isFinished()) {
            boolean stepped = videoComposer.stepPipeline();
            loopCount++;
            if (durationUs > 0 && loopCount % PROGRESS_INTERVAL_STEPS == 0) {
                double videoProgress = videoComposer.isFinished() ? 1.0 : Math.min(1.0, (double) videoComposer.getWrittenPresentationTimeUs() / durationUs);
                if (progressCallback != null) {
                    progressCallback.onProgress(videoProgress);
                }
            }
            if (!stepped) {
                try {
                    Thread.sleep(SLEEP_TO_WAIT_TRACK_TRANSCODERS);
                } catch (InterruptedException e) {
                    // nothing to do
                }
            }
        }


    }


    interface ProgressCallback {
        /**
         * Called to notify progress. Same thread which initiated transcode is used.
         *
         * @param progress Progress in [0.0, 1.0] range, or negative value if progress is unknown.
         */
        void onProgress(double progress);
    }
}
