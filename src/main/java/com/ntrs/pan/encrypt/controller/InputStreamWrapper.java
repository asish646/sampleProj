package com.ntrs.pan.encrypt.controller;

import com.microsoft.informationprotection.IStream;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamWrapper implements IStream {

    private final InputStream inputStream;
    private long position = 0;
    private long size = 0;

    public InputStreamWrapper(InputStream inputStream, long size) {

        this.inputStream = inputStream;
        this.size = size;
    }

    @Override
    public boolean canRead() {
        return inputStream != null;
    }

    @Override
    public boolean canWrite() {
        return false;
    }

    @Override
    public boolean flush() {
        throw new UnsupportedOperationException("Flush not supported");
    }

    @Override
    public long position() {
        return position;
    }

    @Override
    public long read(byte[] buffer, long arg1) {
        try {
            inputStream.skip(arg1);
            int bytesRead = inputStream.read(buffer);
            if (bytesRead != -1) {
                position += bytesRead;
            }
            return bytesRead;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public void seek(long arg0) {
        try {
            inputStream.reset();
            inputStream.skip(arg0);
            position = arg0;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public void size(long arg0) {
        this.size = arg0;
    }

    @Override
    public long write(byte[] arg0) {
        throw new UnsupportedOperationException("Flush not supported");
    }
}