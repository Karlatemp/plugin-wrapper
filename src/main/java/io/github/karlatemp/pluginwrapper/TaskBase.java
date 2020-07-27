package io.github.karlatemp.pluginwrapper;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public abstract class TaskBase {

    public interface TaskSupplier {
        TaskBase newTask(String packageName, File source, String output, String outputName, String internalName);
    }

    protected final File source;
    protected String output;
    protected String packageName;
    protected String outputName;
    protected final ZipFile zipFile;
    protected final Map<String, PWEntry> entries = new LinkedHashMap<>();
    protected String internalName;

    public TaskBase(
            String packageName, File source, String output, String outputName, String internalName
    ) {
        this.packageName = packageName;
        this.output = output;
        this.outputName = outputName;
        this.internalName = internalName;
        if (!source.isFile()) {
            throw new PWException(1, "The source `" + source + "` is not a file.");
        }
        this.source = source;
        try {
            zipFile = new ZipFile(source);
        } catch (IOException ioException) {
            throw new PWException(2, "Source `" + source + "` invalid", ioException);
        }
        final Enumeration<? extends ZipEntry> entries = zipFile.entries();
        final Map<String, PWEntry> entryMap = this.entries;
        while (entries.hasMoreElements()) {
            entryMap.put(entries.nextElement().getName(), PWEntry.ZipCopied.INSTANCE);
        }
    }

    public void run() {
        try {
            process();
            doTransform();
        } catch (IOException ioException) {
            throw new PWException(3, "Exception in transform", ioException);
        }
    }

    protected File targetFile() {
        File file = new File(output, outputName);
        if (PluginWrapper.FORCE_MODE) {
            if (file.isDirectory()) {
                final File[] files = file.listFiles();
                if (files == null || files.length == 0) {
                    file.delete();
                    return file;
                }
            } else {
                return file;
            }
        }
        if (file.isFile()) {
            if (file.length() == 0) return file;
            else throw new PWException(7, "Output file `" + file + "` already exists.");
        } else if (file.isDirectory()) {
            throw new PWException(7, "Output file `" + file + "` is a directory.");
        }
        return file;
    }

    protected abstract void process() throws IOException;

    protected void doTransform() throws IOException {
        File target = targetFile();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(
                new RAFOutputStream(
                        new RandomAccessFile(target, "rw")
                )
        )) {
            for (Map.Entry<String, PWEntry> entry : entries.entrySet()) {
                String file = entry.getKey();
                setLine(file + " - Processing");
                entry.getValue().read(this, zipFile, file, zipOutputStream);
                setLine(file + " - Finished");
                setLine(null);
            }
        }
    }

    private String line;
    private final PrintStream out = System.out;
    private int linedLength;

    public void println(String line) {
        if (PluginWrapper.NO_LOG) return;
        if (this.line == null) {
            out.println(line);
            return;
        }
        out.print('\r');
        out.println(line);
        rerender();
    }

    protected void setLine(String line) {
        if (PluginWrapper.NO_LOG) return;
        if (line == null) {
            String tl = this.line;
            if (tl == null) return;
            this.line = null;
            out.println();
        } else {
            this.line = line;
            rerender();
        }
    }

    private void rerender() {
        if (PluginWrapper.NO_LOG) return;
        if (line != null) {
            out.print('\r');
            out.print(line);
            int ref = linedLength - (linedLength = line.length());
            while (ref-- > 0) {
                out.print(' ');
            }
        }
    }
}
