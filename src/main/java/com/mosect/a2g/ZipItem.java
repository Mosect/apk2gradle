package com.mosect.a2g;

import java.io.File;
import java.io.InputStream;

public class ZipItem {

    private final File file;
    private final String path;
    private final InputStream data;

    public ZipItem(String path) {
        this.path = path;
        this.file = null;
        this.data = null;
    }

    public ZipItem(File file, String path) {
        this.file = file;
        this.path = path;
        this.data = null;
    }

    public ZipItem(InputStream data, String path) {
        this.file = null;
        this.path = path;
        this.data = data;
    }

    public InputStream getData() {
        return data;
    }

    public File getFile() {
        return file;
    }

    public String getPath() {
        return path;
    }
}
