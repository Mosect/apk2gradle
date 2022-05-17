package com.mosect.a2g;

import com.mosect.a2g.dex.DexMaker;
import com.mosect.a2g.util.IOUtils;
import com.mosect.a2g.util.ProcessUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Make R.txt
 */
public class RtxtMaker {

    private File dex2jarFile;
    private File workDir;
    private File apktoolProjectDir;
    private File rtxtMakerJarFile;
    private File outputFile;

    public void setDex2jarFile(File dex2jarFile) {
        this.dex2jarFile = dex2jarFile;
    }

    public void setWorkDir(File workDir) {
        this.workDir = workDir;
    }

    public void setApktoolProjectDir(File apktoolProjectDir) {
        this.apktoolProjectDir = apktoolProjectDir;
    }

    public void setRtxtMakerJarFile(File rtxtMakerJarFile) {
        this.rtxtMakerJarFile = rtxtMakerJarFile;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void run() throws Exception {
        if (null == dex2jarFile) {
            throw new IllegalStateException("dex2jarFile not set");
        }
        if (null == workDir) {
            throw new IllegalStateException("workDir not set");
        }
        if (null == apktoolProjectDir) {
            throw new IllegalStateException("apktoolProjectDir not set");
        }
        if (null == rtxtMakerJarFile) {
            throw new IllegalStateException("rtxtMakerJarFile not set");
        }

        IOUtils.initDir(workDir);
        Map<String, File> smaliDirMap = IOUtils.listSmaliDirs(apktoolProjectDir);
        if (smaliDirMap.isEmpty()) {
            throw new IOException("Smali dirs not found");
        }
        DexMaker dexMaker = new DexMaker(1);
        for (File dir : smaliDirMap.values()) {
            addRClass(dir, "", dexMaker);
        }
        File dexFile = new File(workDir, "classes.dex");
        boolean ok = dexMaker.makeDex(dexFile);
        if (!ok) {
            throw new IOException("Create dex failed");
        }
        File jarFile = new File(workDir, "classes.jar");
        ProcessUtils.execWithException(new ProcessBuilder(
                dex2jarFile.getAbsolutePath(),
                "-o",
                jarFile.getAbsolutePath(),
                dexFile.getAbsolutePath()
        ));
        File rTxt = new File(workDir, "R.txt");
        ProcessUtils.execJava(
                rtxtMakerJarFile.getAbsolutePath(),
                jarFile.getAbsolutePath(),
                rTxt.getAbsolutePath()
        );
        outputFile = rTxt;
    }

    private void addRClass(File smaliDir, String prefix, DexMaker dexMaker) {
        File[] files = smaliDir.listFiles(file -> file.isFile() && file.getName().matches("^R(\\$\\w+)?\\.smali$"));
        if (null != files && files.length > 0) {
            for (File smaliFile : files) {
                String name = smaliFile.getName();
                String className = prefix + name.substring(0, name.length() - ".smali".length());
                dexMaker.addSmaliFile(className, smaliFile);
            }
        }
        File[] dirs = smaliDir.listFiles(file -> file.isDirectory() && !".".equals(file.getName()) && !"..".equals(file.getName()));
        if (null != dirs && dirs.length > 0) {
            for (File dir : dirs) {
                String nextPrefix = prefix + dir.getName() + ".";
                addRClass(dir, nextPrefix, dexMaker);
            }
        }
    }
}
