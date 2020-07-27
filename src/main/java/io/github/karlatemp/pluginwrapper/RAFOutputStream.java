package io.github.karlatemp.pluginwrapper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class RAFOutputStream extends OutputStream {
    private final RandomAccessFile raf;

    public RAFOutputStream(RandomAccessFile randomAccessFile) {
        this.raf = randomAccessFile;
    }

    @Override
    public void write(int b) throws IOException {
        raf.write(b);
    }

    @Override
    public void close() throws IOException {
        raf.setLength(raf.getFilePointer());
        raf.close();
    }

    @Override
    public void write(byte[] b) throws IOException {
        raf.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        raf.write(b, off, len);
    }
}
