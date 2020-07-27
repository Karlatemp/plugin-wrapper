package io.github.karlatemp.pluginwrapper;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.util.EnumConverter;

import java.io.File;
import java.util.Arrays;
import java.util.UUID;

public class PluginWrapper {
    public static boolean FORCE_MODE;
    public static boolean NO_LOG;

    public enum WrappedType {
        BUKKIT(TaskBukkit::new),
        NUKKIT(TaskNukkit::new),
        SPONGE(TaskSponge::new),
        NATIVE(TaskNative::new),
        BUNGEE(TaskBungee::new);
        public final TaskBase.TaskSupplier taskSupplier;

        WrappedType(TaskBase.TaskSupplier taskSupplier) {
            this.taskSupplier = taskSupplier;
        }
    }

    public static void main(String[] args) throws Throwable {
        try {
            invoke(args);
        } catch (PWException exception) {
            exception.printStackTrace(System.err);
            System.exit(exception.code);
        }
    }

    public static void invoke(String[] args) throws Throwable {
        OptionParser parser = new OptionParser();

        OptionSpec<String> input = parser.acceptsAll(
                Arrays.asList("i", "input"),
                "The source of library"
        ).withRequiredArg();
        OptionSpec<Void> help = parser.acceptsAll(Arrays.asList("h", "help"), "Show the help");
        OptionSpec<WrappedType> type = parser.acceptsAll(Arrays.asList("t", "type"), "The type of wrapped jar")
                .withRequiredArg().ofType(WrappedType.class)
                .withValuesConvertedBy(new EnumConverter<WrappedType>(WrappedType.class) {
                });
        OptionSpec<String> output = parser.acceptsAll(Arrays.asList("o", "output"), "The output location.")
                .withOptionalArg();
        OptionSpec<String> name = parser.acceptsAll(Arrays.asList("on", "output-name"), "The name of output jar.")
                .withOptionalArg();
        OptionSpec<String> internal_name = parser.acceptsAll(Arrays.asList("in", "internal-name"), "The name of output jar.")
                .withOptionalArg();
        OptionSpec<String> package_name = parser.acceptsAll(Arrays.asList("p", "package-name"), "The package name")
                .withOptionalArg();
        OptionSpec<Void> force = parser.accepts("force");
        OptionSpec<Void> noLog = parser.accepts("no-log");
        final OptionSet options = parser.parse(args);

        if (options.has(help) || !options.has(input) || !options.has(type)) {
            parser.printHelpOn(System.out);
            return;
        }

        FORCE_MODE = options.has(force);
        NO_LOG = options.has(noLog);

        final WrappedType wrappedType = options.valueOf(type);
        String sourceJar = options.valueOf(input),
                packageName = options.valueOf(package_name),
                internalName = options.valueOf(internal_name),
                outputDir = options.valueOf(output),
                outputName = options.valueOf(name);
        if (packageName == null) {
            packageName = "io.github.karlatemp.pluginwrapper.RK" + UUID.randomUUID().toString().replace('-', '_');
        }
        File sourceJarFile = new File(sourceJar);
        if (internalName == null) {
            internalName = StringUtils.getFileName(sourceJarFile.getName());
        }
        if (outputDir == null) {
            if ((outputDir = sourceJarFile.getParent()) == null) {
                outputDir = ".";
            }
        }
        if (outputName == null) {
            String jarName = sourceJarFile.getName();
            int split = jarName.lastIndexOf('.');
            if (split == -1) {
                outputName = jarName + "-" + wrappedType + ".jar";
            } else {
                outputName = jarName.substring(0, split) + "-" + wrappedType + jarName.substring(split);
            }
        }
        wrappedType.taskSupplier.newTask(packageName, sourceJarFile, outputDir, outputName, internalName)
                .run();
    }
}
