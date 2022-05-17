package com.mosect.a2g.util;

import com.mosect.a2g.Apk2Gradle;
import com.mosect.a2g.ZipItem;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public final class IOUtils {

    private IOUtils() {
    }

    public static void initParent(File file) throws IOException {
        File parent = file.getParentFile();
        initDir(parent);
    }

    public static void initDir(File dir) throws IOException {
        if (null != dir && !dir.exists() && !dir.mkdirs()) {
            throw new IOException("Can't create directory {" + dir.getAbsolutePath() + "}");
        }
    }

    public static Map<String, File> listSmaliDirs(File dir) {
        File[] dirs = dir.listFiles(file -> {
            String name = file.getName();
            return file.isDirectory() && name.matches("^smali(_classes[0-9]+)?$");
        });
        if (null != dirs && dirs.length > 0) {
            Map<String, File> result = new HashMap<>();
            for (File d : dirs) {
                int index = d.getName().lastIndexOf('_');
                if (index < 0) {
                    result.put("classes", d);
                } else {
                    String name = d.getName().substring(index + 1);
                    result.put(name, d);
                }
            }
            return result;
        }
        return Collections.emptyMap();
    }

    public static String load(InputStream is) {
        try (ByteArrayOutputStream temp = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) > 0) {
                temp.write(buffer, 0, len);
            }
            return temp.toString("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String loadFile(File file) {
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file);) {
                return load(fis);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String loadRes(String name) {
        InputStream is = IOUtils.class.getResourceAsStream(name);
        if (null != is) {
            String result = load(is);
            close(is);
            return result;
        }
        return null;
    }

    public static boolean save(File file, String data) {
        File dir = file.getParentFile();
        if (null != dir && !dir.exists()) {
            if (!dir.mkdirs()) {
                return false;
            }
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data.getBytes(StandardCharsets.UTF_8));
            fos.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void close(Closeable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static boolean delete(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (null != files && files.length > 0) {
                for (File f : files) {
                    String name = f.getName();
                    if (".".equals(name) || "..".equals(name)) continue;
                    delete(f);
                }
            }
        }
        return file.delete();
    }

    public static List<String> readLines(InputStream is) {
        List<String> lines = new ArrayList<>();
        try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lines;
    }

    public static List<String> readFileLines(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            return readLines(fis);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> readResLines(String name) {
        InputStream is = IOUtils.class.getResourceAsStream(name);
        if (null != is) {
            List<String> lines = readLines(is);
            close(is);
            return lines;
        }
        return null;
    }

    public static void saveLines(File file, List<String> lines) {
        try (FileOutputStream fos = new FileOutputStream(file);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(osw)) {
            if (null != lines) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copy(File src, File dst) {
        copy(new byte[1024], src, dst);
    }

    private static void copy(byte[] buffer, File src, File dst) {
        if (src.exists()) {
            File parentFile = dst.getParentFile();
            if (null != parentFile && !parentFile.exists()) {
                parentFile.mkdirs();
            }
            if (src.isFile()) {
                try (FileInputStream fis = new FileInputStream(src);
                     FileOutputStream fos = new FileOutputStream(dst)) {
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (src.isDirectory()) {
                dst.mkdir();
                File[] files = src.listFiles();
                if (null != files && files.length > 0) {
                    for (File f : files) {
                        if (".".equals(f.getName()) || "..".equals(f.getName())) continue;

                        File fd = new File(dst, f.getName());
                        copy(buffer, f, fd);
                    }
                }
            }
        }
    }

    public static Document readDocument(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(isr));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveDocument(File file, Document document) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "YES");
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            transformer.transform(new DOMSource(document), new StreamResult(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void zip(ZipItem[] items, File out) {
        try (FileOutputStream fos = new FileOutputStream(out);
             ZipOutputStream zos = new ZipOutputStream(fos, StandardCharsets.UTF_8)) {
            byte[] buffer = new byte[1024];
            for (ZipItem item : items) {
                if (null != item.getFile()) {
                    if (item.getFile().isDirectory()) {
                        putChildrenToZip(item.getFile(), item.getPath(), zos, buffer);
                    } else if (item.getFile().isFile()) {
                        zos.putNextEntry(new ZipEntry(item.getPath()));
                        copyData(item.getFile(), zos, buffer);
                    }
                } else if (null != item.getData()) {
                    zos.putNextEntry(new ZipEntry(item.getPath()));
                    copyData(item.getData(), zos, buffer);
                    item.getData().close();
                } else {
                    zos.putNextEntry(new ZipEntry(item.getPath()));
                }
            }
            zos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void putChildrenToZip(File dir, String path, ZipOutputStream zos, byte[] buffer) throws IOException {
        File[] files = dir.listFiles();
        if (null != files && files.length > 0) {
            for (File f : files) {
                String name = f.getName();
                if (".".equals(name) || "..".equals(name)) continue;
                String curPath = path + "/" + name;
                if (f.isDirectory()) {
                    putChildrenToZip(f, curPath, zos, buffer);
                } else if (f.isFile()) {
                    zos.putNextEntry(new ZipEntry(curPath));
                    copyData(f, zos, buffer);
                }
            }
        }
    }

    private static void copyData(File file, OutputStream dst, byte[] buffer) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            copyData(fis, dst, buffer);
        }
    }

    public static void copyData(InputStream src, OutputStream dst, byte[] buffer) throws IOException {
        int len;
        while ((len = src.read(buffer)) > 0) {
            dst.write(buffer, 0, len);
        }
    }

    public static void copyResToFile(String resName, File file) throws IOException {
        try (InputStream is = Apk2Gradle.class.getResourceAsStream(resName);
             FileOutputStream dst = new FileOutputStream(file)) {
            IOUtils.copyData(is, dst, new byte[1024]);
        }
    }
}
