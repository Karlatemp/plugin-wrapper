package io.github.karlatemp.pluginwrapper;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class TaskBukkit extends TaskBase {
    public TaskBukkit(String packageName, File source, String output, String outputName, String internalName) {
        super(packageName, source, output, outputName, internalName);
    }

    protected String configFileName() {
        return "plugin.yml";
    }

    @Override
    protected void process() throws IOException {
        if (internalName == null) {
            internalName = StringUtils.getFileName(outputName);
        }
        if (entries.get("config.yml") != null) {
            throw new PWException(15, "Source jar contains `config.yml`");
        }
        Map<String, String> baseConfig = new LinkedHashMap<>();
        baseConfig.put("author", "Karlatemp");
        baseConfig.put("name", internalName);
        baseConfig.put("version", "1.0-Release");
        baseConfig.put("api-version", "1.13");
        packageName = packageName.replace('/', '.');
        if (!packageName.endsWith(".")) packageName += '.';
        String className = packageName + "PWGen$" + UUID.randomUUID().toString().replace('-', '_');
        baseConfig.put("main", className);
        entries.put(className.replace('.', '/') + ".class",
                new PWEntry.NewFile(genClass(className.replace('.', '/')))
        );
        processConfig(baseConfig);
        final String configFileName = configFileName();
        if (configFileName != null)
            entries.put(configFileName, new PWEntry.NewFile(genPlugin(baseConfig)));
    }

    protected void processConfig(Map<String, String> config) {
    }

    protected byte[] genPlugin(Map<String, String> baseConfig) {
        return new Yaml(new DumperOptions() {{
            setAllowUnicode(false);
            setDefaultFlowStyle(FlowStyle.BLOCK);
        }}).dump(baseConfig).getBytes(StandardCharsets.UTF_8);
    }

    protected byte[] genClass(String className) {
        ClassWriter writer = new ClassWriter(0);
        writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
                className, null, "org/bukkit/plugin/java/JavaPlugin", null);
        final MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
                "<init>", "()V", null, null);
        method.visitMaxs(2, 2);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitMethodInsn(Opcodes.INVOKESPECIAL, "org/bukkit/plugin/java/JavaPlugin", "<init>", "()V", false);
        method.visitInsn(Opcodes.RETURN);
        return writer.toByteArray();
    }
}
