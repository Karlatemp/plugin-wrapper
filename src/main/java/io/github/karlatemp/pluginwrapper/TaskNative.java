package io.github.karlatemp.pluginwrapper;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.zip.ZipEntry;

//
public class TaskNative extends TaskBase {
    public TaskNative(String packageName, File source, String output, String outputName, String internalName) {
        super(packageName, source, output, outputName, internalName);
    }

    static class NativeModify extends PWEntry.Modify {
        static final NativeModify INSTANCE = new NativeModify();

        @Override
        protected boolean modify(TaskBase task, ZipEntry entry, AccessibleByteArrayOutputStream buf) throws IOException {
            ByteArrayInputStream view = new ByteArrayInputStream(
                    buf.getBuffer(),
                    0,
                    buf.getCount()
            );
            ClassReader reader;
            ClassNode node = new ClassNode();
            try {
                reader = new ClassReader(view);
                reader.accept(node, 0);
            } catch (Throwable ignored) {
                return false;
            }
            task.println(" - Editing " + node.name);
            final Iterator<MethodNode> iterator = node.methods.iterator();
            while (iterator.hasNext()) {
                MethodNode method = iterator.next();
                if ("<clinit>".equals(method.name)) {
                    iterator.remove();
                    task.println(" - Removed " + method.name + method.desc);
                } else {
                    method.instructions.clear();
                    task.println(" - Cleared " + method.name + method.desc);
                }
            }
            ClassWriter writer = new ClassWriter(0);
            node.accept(writer);
            final byte[] bytes = writer.toByteArray();
            buf.setBuffer(bytes);
            buf.setCount(bytes.length);
            return true;
        }
    }

    @Override
    protected void process() throws IOException {
        entries.replaceAll((k, v) -> NativeModify.INSTANCE);
    }
}
