package com.uenchi.stitchescamera.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;

/**
 * @author : uenchi
 * @date : 2021/4/12
 * @desc :
 */
public class SomeUtils {

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            if (cursor != null && cursor.getColumnCount() > 0) {
                cursor.moveToFirst();
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                String path = cursor.getString(column_index);
                return path;
            } else {
            }
        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return "";
    }

    /**
     * 获取完整文件名(包含扩展名)
     *
     * @param filePath
     * @return
     */
    public static String getFilenameWithExtension(String filePath) {
        if (filePath == null || filePath.length() == 0) {
            return "";
        }
        int lastIndex = filePath.lastIndexOf(File.separator);
        String filename = filePath.substring(lastIndex + 1);
        return filename;
    }

    /**
     * 判断文件路径的文件名是否存在文件扩展名 eg: /external/images/media/2283
     *
     * @param filePath
     * @return
     */
    public static boolean isFilePathWithExtension(String filePath) {
        String filename = getFilenameWithExtension(filePath);
        return filename.contains(".");
    }

}
