package com.alimama.mdrill.utils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.alimama.mdrill.utils.zip.ZipEntry;
import com.alimama.mdrill.utils.zip.ZipFile;
import com.alimama.mdrill.utils.zip.ZipOutputStream;

/**
 * <p>
 * ZIP工具包
 * </p>
 * <p>
 * 依赖：ant-1.7.1.jar
 * </p>
 * 
 * @author IceWee
 * @date 2012-5-26
 * @version 1.0
 */
public class ZipUtils {
    
    /**
     * 使用GBK编码可以避免压缩中文文件名乱码
     */
    private static final String CHINESE_CHARSET = "GBK";
    
    /**
     * 文件读取缓冲区大小
     */
    private static final int CACHE_SIZE = 10240;
    
    /**
     * <p>
     * 压缩文件
     * </p>
     * 
     * @param sourceFolder 压缩文件夹
     * @param zipFilePath 压缩文件输出路径
     * @throws Exception
     */
    public static void zip(FileSystem fs,String sourceFolder1, FileSystem fs2,String zipFilePath2) throws Exception {
        OutputStream out = fs2.create(new Path(zipFilePath2),true);
        BufferedOutputStream bos = new BufferedOutputStream(out);
        ZipOutputStream zos = new ZipOutputStream(bos);
        // 解决中文文件名乱码
        zos.setEncoding(CHINESE_CHARSET);
        Path basePath = null;
        Path src=new Path(sourceFolder1);
        FileStatus f=fs.getFileStatus(src);
        if (f.isDir()) {
            basePath =f.getPath();
        } else {
            basePath = f.getPath().getParent();
        }
        zipFile(fs,f, basePath, zos);
        zos.closeEntry();
        zos.close();
        bos.close();
        out.close();
    }
    
    /**
     * <p>
     * 递归压缩文件
     * </p>
     * 
     * @param parentFile
     * @param basePath
     * @param zos
     * @throws Exception
     */
    private static void zipFile(FileSystem fs,FileStatus parentFile, Path basePath, ZipOutputStream zos) throws Exception {
    	FileStatus[] files = new FileStatus[0];
        if (parentFile.isDir()) {
            files = fs.listStatus(parentFile.getPath());
        } else {
            files = new FileStatus[1];
            files[0] = parentFile;
        }
        String pathName;
        InputStream is;
        BufferedInputStream bis;
        byte[] cache = new byte[CACHE_SIZE];
        for (FileStatus file : files) {
            if (file.isDir()) {
                pathName = file.getPath().toString().substring(basePath.toString().length() + 1) + "/";
                zos.putNextEntry(new ZipEntry(pathName));
                zipFile(fs,file, basePath, zos);
            } else {
                pathName = file.getPath().toString().substring(basePath.toString().length() + 1);
                is = fs.open(file.getPath());
                bis = new BufferedInputStream(is);
                zos.putNextEntry(new ZipEntry(pathName));
                int nRead = 0;
                while ((nRead = bis.read(cache, 0, CACHE_SIZE)) != -1) {
                    zos.write(cache, 0, nRead);
                }
                bis.close();
                is.close();
            }
        }
    }
    
    /**
     * <p>
     * 解压压缩包
     * </p>
     * 
     * @param zipFilePath 压缩文件路径
     * @param destDir 压缩包释放目录
     * @throws Exception
     */
    public static void unZip(FileSystem fs,String zipFilePath, FileSystem fs2,String destDir) throws Exception {
    	FSDataInputStream in=fs.open(new Path(zipFilePath));
	    long length = fs.getFileStatus(new Path(zipFilePath)).getLen();

        ZipFile zipFile = new ZipFile(in,length, CHINESE_CHARSET,true);
        Enumeration<?> emu = zipFile.getEntries();
        BufferedInputStream bis;
        FSDataOutputStream fos;
        BufferedOutputStream bos;
        Path file, parentFile;
        ZipEntry entry;
        byte[] cache = new byte[CACHE_SIZE];
        while (emu.hasMoreElements()) {
            entry = (ZipEntry) emu.nextElement();
            if (entry.isDirectory()) {
            	fs2.mkdirs(new Path(destDir , entry.getName()));
                continue;
            }
            bis = new BufferedInputStream(zipFile.getInputStream(entry));
            file = new Path(destDir , entry.getName());
            parentFile = file.getParent();
            if (parentFile != null && (!fs2.exists(parentFile))) {
            	fs2.mkdirs(parentFile);
            }
            fos =fs2.create(file,true);
            bos = new BufferedOutputStream(fos, CACHE_SIZE);
            int nRead = 0;
            while ((nRead = bis.read(cache, 0, CACHE_SIZE)) != -1) {
                fos.write(cache, 0, nRead);
            }
            bos.flush();
            bos.close();
            fos.close();
            bis.close();
        }
        zipFile.close();
    }
    
}

