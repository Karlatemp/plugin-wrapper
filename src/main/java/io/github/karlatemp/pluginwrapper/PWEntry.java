package io.github.karlatemp.pluginwrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public abstract class PWEntry {

    public abstract void read(TaskBase task, ZipFile zip, String path, ZipOutputStream output) throws IOException;

    public static class ZipCopied extends PWEntry {
        private ZipCopied() {
        }

        public static final ZipCopied INSTANCE = new ZipCopied();

        @Override
        public void read(TaskBase task, ZipFile zip, String path, ZipOutputStream output) throws IOException {
            final ZipEntry entry = zip.getEntry(path);
            output.putNextEntry(entry);
            if (!entry.isDirectory()) {
                byte[] buffer = new byte[10240];
                try (InputStream stream = zip.getInputStream(entry)) {
                    while (true) {
                        int length = stream.read(buffer);
                        if (length == -1) break;
                        output.write(buffer, 0, length);
                    }
                }
            }
        }
    }

    public static class NewFile extends PWEntry {
        private final byte[] data;
        private final int offset;
        private final int length;

        public NewFile(byte[] data, int offset, int length) {
            this.data = data;
            this.offset = offset;
            this.length = length;
        }

        public NewFile(byte[] data) {
            this(data, 0, data.length);
        }

        @Override
        public void read(TaskBase task, ZipFile zip, String path, ZipOutputStream output) throws IOException {
            ZipEntry entry = new ZipEntry(path);
            output.putNextEntry(entry);
            output.write(data, offset, length);
        }
    }

    public abstract static class Modify extends PWEntry {
        public static class AccessibleByteArrayOutputStream extends ByteArrayOutputStream {
            public byte[] getBuffer() {
                return buf;
            }

            public AccessibleByteArrayOutputStream() {
            }

            public int getCount() {
                return count;
            }

            public AccessibleByteArrayOutputStream(int length) {
                super(length);
            }

            public void setBuffer(byte[] buffer) {
                this.buf = buffer;
            }

            public void setCount(int count) {
                this.count = count;
            }
        }

        protected abstract boolean modify(TaskBase task, ZipEntry entry, AccessibleByteArrayOutputStream buf) throws IOException;

        @Override
        public void read(TaskBase task, ZipFile zip, String path, ZipOutputStream output) throws IOException {
            final ZipEntry entry = zip.getEntry(path);
            if (!entry.isDirectory()) {
                byte[] buffer = new byte[2048];
                AccessibleByteArrayOutputStream bos = new AccessibleByteArrayOutputStream(10240);
                try (InputStream stream = zip.getInputStream(entry)) {
                    while (true) {
                        int length = stream.read(buffer);
                        if (length == -1) break;
                        bos.write(buffer, 0, length);
                    }
                }
                if (modify(task, entry, bos)) {
                    ZipEntry newEntry = new ZipEntry(entry.getName());
                    newEntry.setComment(entry.getComment());
                    newEntry.setTime(entry.getTime());
                    Optional.ofNullable(entry.getLastAccessTime()).ifPresent(newEntry::setLastAccessTime);
                    Optional.ofNullable(entry.getLastModifiedTime()).ifPresent(newEntry::setLastModifiedTime);
                    output.putNextEntry(newEntry);
                } else {
                    output.putNextEntry(entry);
                }
                bos.writeTo(output);
            } else {
                output.putNextEntry(entry);
            }
        }
    }
}
