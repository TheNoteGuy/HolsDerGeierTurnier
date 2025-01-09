package org.example;

import javax.tools.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class ClassMover {
    private final Class<?> botClass;
    private final String targetRound;
    private static final String BOTS_BASE_PATH = "src/main/java/org/example/bots/";
    private static final String TEMP_COMPILE_DIR = "temp_compile";
    private static final String PROJECT_ROOT = System.getProperty("user.dir");

    public ClassMover(Class<?> botClass, String targetRound) {
        this.botClass = botClass;
        this.targetRound = targetRound;
    }

    public void move() throws IOException {
        String className = botClass.getSimpleName();
        String currentPackage = botClass.getPackage().getName();
        String currentRound = currentPackage.substring(currentPackage.lastIndexOf('.') + 1);

        Path sourcePath = Paths.get(PROJECT_ROOT, BOTS_BASE_PATH, currentRound, className + ".java");
        Path targetPath = Paths.get(PROJECT_ROOT, BOTS_BASE_PATH, targetRound, className + ".java");
        Path compileDir = Paths.get(PROJECT_ROOT, TEMP_COMPILE_DIR);

        try {
            Files.createDirectories(compileDir);

            String content = new String(Files.readAllBytes(sourcePath), StandardCharsets.UTF_8);

            content = content.replaceFirst(
                    "package org.example.bots." + currentRound,
                    "package org.example.bots." + targetRound
            );

            Files.createDirectories(targetPath.getParent());

            Files.write(targetPath, content.getBytes(StandardCharsets.UTF_8));

            Files.delete(sourcePath);

            compileFile(targetPath.toString(), compileDir.toString());

            System.out.println("Moved and compiled " + className + " from " + currentRound + " to " + targetRound);

        } catch (IOException e) {
            System.err.println("Failed to move " + className + ": " + e.getMessage());
            throw e;
        }
    }

    private void compileFile(String sourceFile, String outputDir) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new RuntimeException("No Java compiler found!");
        }

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        String classpath = System.getProperty("java.class.path");

        List<String> options = Arrays.asList(
                "-d", outputDir,
                "-classpath", classpath
        );

        Iterable<? extends JavaFileObject> compilationUnits =
                fileManager.getJavaFileObjectsFromStrings(Arrays.asList(sourceFile));

        JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                fileManager,
                diagnostics,
                options,
                null,
                compilationUnits
        );

        boolean success = task.call();

        if (!success) {
            StringBuilder sb = new StringBuilder();
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                sb.append(diagnostic.getMessage(null));
                sb.append("\n");
            }
            throw new RuntimeException("Compilation failed: " + sb.toString());
        }

        fileManager.close();
    }

    public static void moveToNextRound(Class<?> botClass) {
        try {
            String currentPackage = botClass.getPackage().getName();
            String currentRound = currentPackage.substring(currentPackage.lastIndexOf('.') + 1);
            if (currentRound.startsWith("round")) {
                int roundNumber = Integer.parseInt(currentRound.replace("round", ""));
                new ClassMover(botClass, "round" + (roundNumber + 1)).move();
            }
        } catch (Exception e) {
            System.err.println("Failed to move to next round: " + e.getMessage());
        }
    }

    public static void moveToLosers(Class<?> botClass) {
        try {
            new ClassMover(botClass, "losers").move();
        } catch (Exception e) {
            System.err.println("Failed to move to losers: " + e.getMessage());
        }
    }
}