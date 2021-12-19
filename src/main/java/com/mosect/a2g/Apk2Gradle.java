package com.mosect.a2g;

import com.google.gson.Gson;

import org.w3c.dom.Document;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Manifest;

public class Apk2Gradle {

    private static final String TAG = "A2G";

    private static Manifest readManifest() {
        try {
            Enumeration<URL> resources = Apk2Gradle.class.getClassLoader()
                    .getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                try {
                    return new Manifest(resources.nextElement().openStream());
                } catch (IOException ignored) {
                }
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    private static void printHelp() {
        String version = "1.0.0";
        Manifest manifest = readManifest();
        if (null != manifest) {
            String v = manifest.getMainAttributes().getValue("Implementation-Version");
            if (null != v) version = v;
        }
        System.out.println("apk2gradle");
        System.out.println("version: " + version);
        System.out.println("author: mosect.com");
        System.out.println("web: http://www.msoect.com");
        System.out.println("github: https://github.com/Mosect");
        System.out.println();
        System.out.println("Usage: ");
        System.out.println("java -jar apk2gradle.jar <export|e|--e|e|/e> <apk_file_path> [output_dir_path]");
        System.out.println("Sample: ");
        System.out.println("java -jar apk2gradle.jar export my.apk out_dir");
        System.out.println("java -jar apk2gradle.jar e my.apk out_dir");
        System.out.println("java -jar apk2gradle.jar -e my.apk out_dir");
        System.out.println("java -jar apk2gradle.jar /e my.apk");
    }

    public static void main(String[] args) {
        if (null == args || args.length == 0) {
            printHelp();
            return;
        }
        String action = args[0].toLowerCase();
        switch (action) {
            case "-e":
            case "--e":
            case "e":
            case "export":
            case "/e":
                if (args.length == 1) {
                    System.err.println("Missing apk file path arg.");
                    printHelp();
                    System.exit(1);
                    return;
                }
                break;
            case "help":
            case "-h":
            case "--h":
            case "/?":
            case "?":
            case "--help":
                printHelp();
                return;
            default:
                System.err.println("Unsupported action: " + action);
                printHelp();
                System.exit(1);
                return;
        }

        Config config = readConfig();

        File cacheDir = new File(config.cache);
        File templateDir = new File(config.template);
        File apktoolFile = new File(config.apktool);

        File apkFile = new File(args[1]);
        String outName = apkFile.getName().replace(".", "_") + "_a2g";
        File outDir = new File(args.length > 2 ? args[2] : outName);

        // dump apk
        LogUtils.i(TAG, "dump apk");
        File apkDir = new File(cacheDir, "apk-" + System.currentTimeMillis());
        boolean dumpOk = exec(new ProcessBuilder(
                config.java, "-jar", apktoolFile.getAbsolutePath(),
                "d", "-s", "-o", apkDir.getAbsolutePath(), apkFile.getAbsolutePath()
        ));
        if (dumpOk) {
            if (outDir.exists()) IOUtils.delete(outDir);
            outDir.mkdirs();
            // copy template files
            LogUtils.i(TAG, "copy template files");
            IOUtils.copy(templateDir, outDir);

            // load project env
            Map<String, Object> apktoolYml = new Yaml().loadAs(
                    IOUtils.loadFile(new File(apkDir, "apktool.yml")),
                    Map.class
            );
            String minSdk = get(apktoolYml, "sdkInfo", "minSdkVersion");
            String targetSdk = get(apktoolYml, "sdkInfo", "targetSdkVersion");
            if (TextUtils.empty(targetSdk)) targetSdk = minSdk;
            String versionName = get(apktoolYml, "versionInfo", "versionName");
            String versionCode = get(apktoolYml, "versionInfo", "versionCode");
            Document manifest = IOUtils.readDocument(new File(apkDir, "AndroidManifest.xml"));
            String packageName = manifest.getDocumentElement().getAttribute("package");
            String compileSdk = manifest.getDocumentElement().getAttribute("platformBuildVersionCode");
            if (TextUtils.empty(compileSdk)) compileSdk = targetSdk;
            manifest.getDocumentElement().removeAttribute("platformBuildVersionCode");
            manifest.getDocumentElement().removeAttribute("platformBuildVersionName");

            // modify build.gradle
            LogUtils.i(TAG, "modify build.gradle");
            Map<String, String> buildEnv = new HashMap<>();
            buildEnv.put("__COMPILE_SDK__", compileSdk);
            buildEnv.put("__APPLICATION_ID__", string(packageName));
            buildEnv.put("__MIN_SDK__", minSdk);
            buildEnv.put("__TARGET__SDK__", targetSdk);
            buildEnv.put("__VERSION_CODE__", versionCode);
            buildEnv.put("__VERSION_NAME__", string(versionName));
            modifyBuildGradle(new File(outDir, "app/build.gradle"), buildEnv);

            File mainDir = new File(outDir, "app/src/main");
            mainDir.mkdirs();
            // create AndroidManifest.xml
            LogUtils.i(TAG, "create AndroidManifest.xml");
            IOUtils.saveDocument(new File(mainDir, "AndroidManifest.xml"), manifest);
            // copy res
            LogUtils.i(TAG, "copy res");
            IOUtils.copy(new File(apkDir, "res"), new File(mainDir, "res"));
            // copy assets
            LogUtils.i(TAG, "copy assets");
            IOUtils.copy(new File(apkDir, "assets"), new File(mainDir, "assets"));

            File originalDir = new File(outDir, "app/original");
            originalDir.mkdirs();
            // create classes.jar
            LogUtils.i(TAG, "create classes.jar");
            File classesJar = new File(originalDir, "classes.jar");
            exec(new ProcessBuilder(config.dex2jar,
                    "-o", classesJar.getAbsolutePath(), apkFile.getAbsolutePath()));
            // create unknown.jar
            LogUtils.i(TAG, "create unknown.jar");
            IOUtils.zipDir(new File(apkDir, "unknown"), new File(originalDir, "unknown.jar"));
            // copy dex files
            LogUtils.i(TAG, "copy dex files");
            copyDexFiles(apkDir, originalDir);
        }

        LogUtils.i(TAG, "result: " + dumpOk);
        // 清理缓存
        IOUtils.delete(apkDir);
        if (!dumpOk) {
            System.exit(1);
        }
    }

    private static void copyDexFiles(File apkDir, File outDir) {
        File[] files = apkDir.listFiles();
        if (null != files && files.length > 0) {
            for (File f : files) {
                if (f.getName().matches("^classes[0-9]*.dex$")) {
                    File dst = new File(outDir, f.getName());
                    IOUtils.copy(f, dst);
                }
            }
        }
    }

    private static String string(String str) {
        return String.format("'%s'", str);
    }

    private static void modifyBuildGradle(File file, Map<String, String> env) {
        List<String> lines = IOUtils.readFileLines(file);
        List<String> text = new ArrayList<>(lines.size());
        for (String line : lines) {
            for (Map.Entry<String, String> entry : env.entrySet()) {
                line = line.replace(entry.getKey(), entry.getValue());
            }
            text.add(line);
        }
        IOUtils.saveLines(file, text);
    }

    @SuppressWarnings("unchecked")
    private static <T> T get(Object data, String... keys) {
        if (data instanceof Map) {
            Map<String, Object> cur = (Map<String, Object>) data;
            Object result = null;
            for (String key : keys) {
                result = cur.get(key);
                if (result instanceof Map) {
                    cur = (Map<String, Object>) result;
                }
            }
            return (T) result;
        }
        return null;
    }

    private static Config readConfig() {
        Config config = null;
        File file = new File("config.json");
        if (file.exists()) {
            String data = IOUtils.loadFile(file);
            if (TextUtils.notEmpty(data)) {
                config = new Gson().fromJson(data, Config.class);
            }
        }
        if (null == config) config = new Config();

        if (TextUtils.empty(config.java)) config.java = "java";
        if (TextUtils.empty(config.apktool)) config.apktool = "apktool.jar";
        if (TextUtils.empty(config.cache)) config.cache = "cache";
        if (TextUtils.empty(config.template)) config.template = "template";
        if (TextUtils.empty(config.dex2jar)) config.dex2jar = "dex-tools/d2j-dex2jar.bat";
        return config;
    }

    private static boolean exec(ProcessBuilder builder) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String str : builder.command()) {
            stringBuilder.append(str).append(' ');
        }
        LogUtils.i(TAG, "exec: " + stringBuilder);
        try {
            Process process = builder.start();
            InputStream error = process.getErrorStream();
            Thread thread = new Thread(() -> {
                InputStreamReader isr = new InputStreamReader(error);
                BufferedReader reader = new BufferedReader(isr);
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        LogUtils.e(TAG, line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                IOUtils.close(reader);
                IOUtils.close(isr);
                IOUtils.close(error);
            });
            thread.start();
            int code = process.waitFor();
            if (code == 0) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
