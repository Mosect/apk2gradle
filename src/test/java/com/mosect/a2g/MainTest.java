package com.mosect.a2g;

import com.mosect.a2g.util.IOUtils;

import org.junit.Test;

import java.io.File;

public class MainTest {

    @Test
    public void testRtxtMaker() throws Exception {
        File workDir = new File("build/Rtxt");
        IOUtils.delete(workDir);

        RtxtMaker rtxtMaker = new RtxtMaker();
        rtxtMaker.setWorkDir(workDir);
        rtxtMaker.setRtxtMakerJarFile(new File("data/RtxtMaker.jar"));
        // Apk dump dir with apktool
        rtxtMaker.setApktoolProjectDir(new File("D:\\Temp\\test"));
        rtxtMaker.setDex2jarFile(new File("data/dex-tools/d2j-dex2jar.bat"));
        rtxtMaker.run();
        System.out.println(rtxtMaker.getOutputFile());
    }
}
