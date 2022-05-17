package com.mosect.a2g.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProcessUtils {

    private static final String TAG = "A2G:exec";

    public static void execJava(String jarPath, String... args) throws Exception {
        String osName = System.getProperty("os.name").toLowerCase();
        File javaHome = new File(System.getProperty("java.home"));
        File java;
        if (osName.startsWith("windows")) {
            java = new File(javaHome, "bin/java.exe");
        } else {
            java = new File(javaHome, "bin/java");
        }
        List<String> cmd = new ArrayList<>();
        cmd.add(java.getAbsolutePath());
        cmd.add("-Dfile.encoding=utf-8");
        cmd.add("-jar");
        cmd.add(jarPath);
        if (null != args) {
            Collections.addAll(cmd, args);
        }
        execWithException(new ProcessBuilder(cmd));
    }

    public static void execWithException(ProcessBuilder builder) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        for (String str : builder.command()) {
            stringBuilder.append(str).append(' ');
        }
        LogUtils.i(TAG, stringBuilder.toString());
        builder.directory(new File(".").getAbsoluteFile());
        File javaHome = new File(System.getProperty("java.home"));
        builder.environment().put("JAVA_HOME", javaHome.getAbsolutePath());
        Process process = builder.start();
        output(process.getErrorStream(), true);
        output(process.getInputStream(), false);
        int code = process.waitFor();
        if (code != 0) throw new RuntimeException("Exec failed: " + code);
    }

    private static void output(InputStream ins, boolean error) {
        Thread thread = new Thread(() -> {
            InputStreamReader isr = new InputStreamReader(ins);
            BufferedReader reader = new BufferedReader(isr);
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    if (error) {
                        LogUtils.e(TAG, line);
                    }
                }
            } catch (IOException ignored) {
            }
            IOUtils.close(reader);
            IOUtils.close(isr);
            IOUtils.close(ins);
        });
        thread.start();
    }
}
