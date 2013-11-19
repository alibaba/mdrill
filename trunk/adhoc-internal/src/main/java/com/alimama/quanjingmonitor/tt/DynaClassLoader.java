package com.alimama.quanjingmonitor.tt;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * 从指定目录、jar文件加载类</p>
 * 
 * 使用：通过下面的方式扩展当前加载器
 *		ClassLoader cld=new DynaClassLoader(Thread.currentThread().getContextClassLoader());
 *		cld.addEtries(****);
 *		Thread.currentThread().setContextClassLoader();
 */
public class DynaClassLoader extends ClassLoader {

    //类资源,目录或者是jar
    private List<File> entries = new ArrayList<File>();

    /**
     * @param parent
     * @param entries
     */
    public DynaClassLoader(ClassLoader parent, File[] entries) {
        super(parent);
        this.entries.addAll(Arrays.asList(entries));
    }

    /**
     * @param parent
     */
    public DynaClassLoader(ClassLoader parent) {
        super(parent);
    }

    /**
     * @param fs
     */
    public void addEtries(File[] fs) {
        this.entries.addAll(Arrays.asList(fs));
    }

    public void clear() {
        this.entries.clear();
    }

    /**
     * @param fs
     */
    public void addEtry(File fs) {
        this.entries.add(fs);
    }

    public String toString() {
        String buf = "";
        for (File file : entries) {
            buf += file.getAbsolutePath() + ";";
        }
        return buf;
    }

    /**
     * 加载class
     * <p/>
     * todo 是否需做缓存、仔细仿照父loadClass的过程、使用参数b
     */
    protected Class loadClass(String name, boolean b)
            throws ClassNotFoundException {

        //优先从父加载器获取
        Class clazz = null;
        try {
            clazz = super.loadClass(name, b);
            if (clazz != null) {
                return clazz;
            }
        } catch (ClassNotFoundException e) {
        }

        //System.out.println("DynaClassLoader:load class->" + name);

        int dotIndex = name.indexOf(".");

        String separator = File.separator;
        if (separator.equals("\\")) {
            separator = "\\\\";
        }

        String fileName;
        if (dotIndex != -1) {
            fileName = new StringBuilder().append(name.replaceAll("\\.", separator)).append(".class").toString();
        } else {
            fileName = new StringBuilder().append(name).append(".class").toString();
        }

        InputStream is = getLocalResourceAsStream(fileName);
        if (is == null) {
            throw new ClassNotFoundException(name);
        }

        try {
            Class loadedClass = readClass(name, is);
            is.close();
            if (loadedClass != null) {
                return loadedClass;
            } else {
                throw new ClassNotFoundException(name);
            }
        } catch (Exception e) {
            throw new ClassNotFoundException(name);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }

    }

    private Class readClass(String className, InputStream is) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(is);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024 * 10];
        int readBytes;
        while ((readBytes = bis.read(bytes)) != -1) {
            os.write(bytes, 0, readBytes);
        }
        byte[] b = os.toByteArray();
        return defineClass(className, b, 0, b.length);
    }

    /**
     *
     * @param name
     * @return
     */
    private URL getLocalResource(String name) {

        for (int i = 0; i < entries.size(); i++) {
            File entry = entries.get(i);
            if (entry.isDirectory() && entry.exists()) {
                File f = new File(entry, name);
                if (f.exists()) {
                    URL url;
                    try {
                        url = f.toURI().toURL();

                    } catch (MalformedURLException ex) {
                        continue;
                    }
                    return url;
                }
            } else if (entry.isFile() && entry.exists()) {
                URL url = null;
                try {
                    ZipFile zf = new ZipFile(entry);
                    name = name.replaceAll("\\\\", "/");
                    ZipEntry zipEntry = zf.getEntry(name);
                    if (zipEntry == null) {
                        continue;
                    }
                    String url_0_ = entry.getAbsolutePath().replaceAll("\\\\", "/");
                    if (!url_0_.startsWith("/")) {
                        url_0_ = new StringBuilder().append("/").append(url_0_).toString();
                    }
                    url = new URL(new StringBuilder().append("jar:file://").append(url_0_).append("!/").append(name).toString());
                } catch (ZipException zipexception) {
                    zipexception.printStackTrace();
                } catch (IOException ioexception) {
                    ioexception.printStackTrace();
                }
                return url;
            }
        }
        return null;
    }

    private InputStream getLocalResourceAsStream(String name) {
        URL res = getLocalResource(name);
        if (res == null) {
            return null;
        }
        InputStream inputstream = null;
        try {
            inputstream = res.openStream();
        } catch (IOException ex) {
            ex.printStackTrace();

        }
        return inputstream;
    }

    /**
     * 加载数据流
     */
    public InputStream getResourceAsStream(String name) {
        InputStream _super = super.getResourceAsStream(name);
        return _super != null ? _super : getLocalResourceAsStream(name);
    }

    /**
     * 加载资源
     */
    public URL getResource(String name) {
        URL resource = super.getResource(name);
        return resource != null ? resource : getLocalResource(name);
    }
}
