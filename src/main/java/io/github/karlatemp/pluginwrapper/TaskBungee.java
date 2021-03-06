package io.github.karlatemp.pluginwrapper;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;

public class TaskBungee extends TaskBukkit {
    public TaskBungee(String packageName, File source, String output, String outputName, String internalName) {
        super(packageName, source, output, outputName, internalName);
    }

    @Override
    protected byte[] genClass(String className) {
        ClassWriter writer = new ClassWriter(0);
        writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
                className, null, "net/md_5/bungee/api/plugin/Plugin", null);
        final MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
                "<init>", "()V", null, null);
        method.visitMaxs(2, 2);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitMethodInsn(Opcodes.INVOKESPECIAL, "net/md_5/bungee/api/plugin/Plugin", "<init>", "()V", false);
        method.visitInsn(Opcodes.RETURN);
        return writer.toByteArray();
    }
}
