package test;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JarUtils {

    public static Map<String, byte[]> load(final File file) throws IOException {
        try (ZipFile zipFile = new ZipFile(file)) {
            Map<String, byte[]> out = new HashMap<>();
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                try (InputStream is = zipFile.getInputStream(entry)) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = is.read(buf)) >= 0) baos.write(buf, 0, len);
                    out.put(entry.getName(), baos.toByteArray());
                }
            }
            return out;
        }
    }

    public static void write(final File file, final Map<String, byte[]> entries) throws IOException {
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(file))) {
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                jos.putNextEntry(new ZipEntry(entry.getKey()));
                jos.write(entry.getValue());
                jos.closeEntry();
            }
        }
    }

}
