package com.mosect.a2g.dex;

import org.jf.baksmali.Baksmali;
import org.jf.baksmali.BaksmaliOptions;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.DexFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DexDecoder {

    private final List<File> dexFiles = new ArrayList<>();
    private int apiLevel = 15;

    public void setApiLevel(int apiLevel) {
        this.apiLevel = apiLevel;
    }

    public void addDexFile(File dexFile) {
        dexFiles.add(dexFile);
    }

    public List<File> decode(File outDir) throws IOException {
        List<File> result = new ArrayList<>();

        Opcodes opcodes = Opcodes.forApi(apiLevel);
        int classesIndex = 1;
        for (File file : dexFiles) {
            File dir;
            if (classesIndex == 1) {
                dir = new File(outDir, "classes");
            } else {
                dir = new File(outDir, "classes" + classesIndex);
            }
            DexFile dexFile = DexFileFactory.loadDexFile(file, opcodes);
            BaksmaliOptions options = new BaksmaliOptions();
            options.apiLevel = apiLevel;
            boolean ok = Baksmali.disassembleDexFile(dexFile, dir, 10, options);
            if (!ok) {
                throw new IOException("BakSmaliFailed: " + file.getAbsolutePath());
            }
            result.add(dir);
            ++classesIndex;
        }
        return result;
    }
}
