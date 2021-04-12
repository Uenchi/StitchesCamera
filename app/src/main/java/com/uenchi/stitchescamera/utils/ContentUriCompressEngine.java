package com.uenchi.stitchescamera.utils;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.PathUtils;
import com.huantansheng.easyphotos.callback.CompressCallback;
import com.huantansheng.easyphotos.engine.CompressEngine;
import com.huantansheng.easyphotos.models.album.entity.Photo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Source;

/**
 * @author : uenchi
 * @date : 2021/4/12
 * @desc :
 */
public class ContentUriCompressEngine implements CompressEngine {
    private boolean isVideo = false;
    //单例
    private static ContentUriCompressEngine instance = null;

    //单例模式，私有构造方法
    private ContentUriCompressEngine() {
    }

    //获取单例
    public static ContentUriCompressEngine getInstance() {
        if (null == instance) {
            synchronized (ContentUriCompressEngine.class) {
                if (null == instance) {
                    instance = new ContentUriCompressEngine();
                }
            }
        }
        return instance;
    }

    public void setVideo(boolean isVideo) {
        this.isVideo = isVideo;
    }

    public static String getPath() {
        File fileDir = new File(PathUtils.getCachePathExternalFirst() + "/tempFile");
        FileUtils.createOrExistsDir(fileDir);
        return fileDir.getPath();
    }

    private File getFile() {
        String mPath = getPath();
        File file = null;
        if (isVideo) {
            file = new File(mPath + "/" + System.currentTimeMillis() + ".mp4");
        } else {
            file = new File(mPath + "/" + System.currentTimeMillis() + ".png");
        }
        FileUtils.createOrExistsFile(file);
        return file;
    }

    @Override
    public void compress(Context context, ArrayList<Photo> photos, CompressCallback callback) {
        callback.onStart();
        new Thread(() -> {
            ArrayList<String> paths = new ArrayList<>();
            for (Photo photo : photos) {
                if (photo.selectedOriginal) continue;
                if (!TextUtils.isEmpty(photo.cropPath)) {
                    paths.add(photo.cropPath);
                } else {
                    paths.add(photo.path);
                }
            }
            if (paths.isEmpty()) {
                callback.onSuccess(photos);
                return;
            }
            List<String> files = new ArrayList<>();
            for (String path : paths) {
                String resolvePath =  MediaPathUtils.getPath(context, Uri.parse(path));
                if (EmptyUtils.isNotEmpty(resolvePath)) {
                    files.add(path);
                } else {
                    Uri uri = Uri.parse(path);
                    String realPath = SomeUtils.getRealPathFromUri(context, uri);
                    if (EmptyUtils.isNotEmpty(realPath)) {
                        files.add(realPath);
                    } else {
                        try {
                            InputStream inputStream = context.getContentResolver().openInputStream(Uri.parse(path));
                            if (EmptyUtils.isNotEmpty(inputStream)) {
                                Source source = Okio.source(inputStream);
                                BufferedSource bufferedSource = Okio.buffer(source);
                                File file = getFile();
                                BufferedSink bufferedSink = Okio.buffer(Okio.sink(file));
                                bufferedSink.writeAll(bufferedSource);
                                bufferedSink.flush();
                                bufferedSource.close();
                                files.add(file.getPath());
                            }
                        } catch (FileNotFoundException e) {
                            files.add(path);
                        } catch (IOException e) {
                            files.add(path);
                        }
                    }

                }

            }

            for (int i = 0, j = photos.size(); i < j; i++) {
                Photo photo = photos.get(i);
                photo.compressPath = files.get(i);
            }
            callback.onSuccess(photos);
        }).start();
    }
}