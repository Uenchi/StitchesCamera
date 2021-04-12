package com.uenchi.editlibrary.source;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FilePathDataSource implements DataSource {

    private final static String TAG = FilePathDataSource.class.getSimpleName();

    private FileDescriptor fileDescriptor;

    public FilePathDataSource(@NonNull String filePath, @NonNull Listener listener) {

        final File srcFile = new File(filePath);
        final FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(srcFile);
        } catch (FileNotFoundException e) {
            listener.onError(e);
            return;
        }

        try {
            fileDescriptor = fileInputStream.getFD();
        } catch (IOException e) {
            listener.onError(e);
        }
    }

    @NonNull
    @Override
    public FileDescriptor getFileDescriptor() {
        return fileDescriptor;
    }
}
