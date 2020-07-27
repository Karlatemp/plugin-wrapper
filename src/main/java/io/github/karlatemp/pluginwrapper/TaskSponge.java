package io.github.karlatemp.pluginwrapper;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;

public class TaskSponge extends TaskBukkit {
    public TaskSponge(String packageName, File source, String output, String outputName, String internalName) {
        super(packageName, source, output, outputName, internalName);
    }

    @Override
    protected byte[] genClass(String className) {
        // org.spongepowered.api.plugin.Plugin

        ClassWriter writer = new ClassWriter(0);
        writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC,
                className, null, "java/lang/Object", null);
        final MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
                "<init>", "()V", null, null);
        method.visitMaxs(2, 2);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        method.visitInsn(Opcodes.RETURN);

        final AnnotationVisitor annotation = writer.visitAnnotation("Lorg/spongepowered/api/plugin/Plugin;", true);
        annotation.visit("id", "plugin-wrapper-" + internalName);
        annotation.visit("version", "1.0.0");
        annotation.visit("name", internalName);
        annotation.visit("description", "The library of `" + source.getName() + '`');
        return writer.toByteArray();
    }

    @Override
    protected String configFileName() {
        return null;
    }
}
