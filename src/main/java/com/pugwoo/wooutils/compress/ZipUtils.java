package com.pugwoo.wooutils.compress;

import com.pugwoo.wooutils.io.IOUtils;
import com.pugwoo.wooutils.string.StringTools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * 压缩工具
 * @author nick
 */
public class ZipUtils {
    
    public static class ZipItem {
        /** 文件名，包含目录形式：用/隔开，例如a/b/c.txt */
        public String fileName;
        /** 文件对应的输入流 */
        public InputStream in;
    }
    
    /**
     * 压缩文件
     * @param zipItems zipItemList 全部zipItem.in会自动关闭
     * @param out 会自动close
     */
    public static void zip(List<ZipItem> zipItems, OutputStream out) throws IOException {
        zip(zipItems, out, null);
    }
    
    /**
     * 压缩文件
     * @param zipItems zipItemList 全部zipItem.in会自动关闭
     * @param out 会自动close
     * @param charsetNullable 字符集 如果为null，则使用ZipOutputStream默认的UTF-8
     */
    public static void zip(List<ZipItem> zipItems, OutputStream out, Charset charsetNullable) throws IOException {
        ZipOutputStream zipOut = null;
        try {
            zipOut = charsetNullable == null ? new ZipOutputStream(out) : new ZipOutputStream(out, charsetNullable);
            if (zipItems != null) {
                for (ZipItem zipItem : zipItems) {
                    zipOut.putNextEntry(new ZipEntry(zipItem.fileName));
                    IOUtils.copy(zipItem.in, zipOut);
                }
            }
        } finally {
            if (zipItems != null) {
                for (ZipItem zipItem : zipItems) {
                    IOUtils.close(zipItem.in);
                }
            }
            IOUtils.close(zipOut);
            IOUtils.close(out);
        }
    }
    
    /**
     * 解压文件
     *   会先尝试使用gbk编码打开，如果GBK打开失败，则使用默认的编码(UTF-8)
     * @param zipFilePath 文件路径
     * @param consumer zipItemList操作
     */
    public static void unzip(String zipFilePath, Consumer<List<ZipItem>> consumer) throws IOException {
        unzip(zipFilePath, null, consumer);
    }
    
    /**
     * 解压文件
     * @param zipFilePath 文件路径
     * @param charsetNullable 字符集；
     *                        如果提供，则使用指定的编码打开；
     *                        如果未提供，会先尝试使用gbk编码打开，如果GBK打开失败，则使用默认的编码(UTF-8)
     * @param consumer zipItemList操作
     * @throws IOException 见 {@link ZipFile#getInputStream(ZipEntry)}
     */
    public static void unzip(String zipFilePath, Charset charsetNullable, Consumer<List<ZipItem>> consumer) throws IOException {
        ZipFile zipFile = null;
        List<ZipItem> zipItemList = null;
        try {
            zipFile = getZipFile(zipFilePath, charsetNullable);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            zipItemList = new ArrayList<>(zipFile.size());
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (zipEntry.isDirectory()) {
                    continue;
                }
                ZipItem zipItem = new ZipItem();
                zipItem.fileName = zipEntry.getName();
                zipItem.in = zipFile.getInputStream(zipEntry);
                zipItemList.add(zipItem);
            }
            
            consumer.accept(zipItemList);
        } finally {
            if (zipItemList != null) {
                for (ZipItem zipItem : zipItemList) {
                    IOUtils.close(zipItem.in);
                }
            }
            IOUtils.close(zipFile);
        }
    }
    
    /**
     * 打开zipFile
     * @param filePath zip文件路径
     * @param charset 如果提供，则使用指定的编码打开；
     *                如果未提供，会先尝试使用gbk编码打开，如果失败，则使用默认的编码(UTF-8)
     * @return zipFile
     * @throws IOException 见{@link ZipFile#ZipFile(File, Charset)}
     */
    private static ZipFile getZipFile(String filePath, Charset charset) throws IOException {
        if (charset != null) {
            return new ZipFile(filePath, charset);
        }
        ZipFile zipFile = getZipFileCharsetGbk(filePath);
        if (zipFile == null) {
            zipFile = new ZipFile(filePath);
        }
        return zipFile;
    }
    
    /**
     * 尝试用GBK打开的zip
     *   会判断文件名编码是否为真正的GBK，否则返回null
     * @param filePath zip文件路径
     * @return zipFile or null(不是GBK编码)
     * @throws IOException 见{@link ZipFile#ZipFile(File, Charset)}
     * @throws IllegalArgumentException 除字符集问题外的其他异常
     */
    private static ZipFile getZipFileCharsetGbk(String filePath) throws IOException {
        ZipFile zipFile = null;
        // 默认关闭掉使用GBK打开的zipFile
        boolean close = true;
        try {
            zipFile = new ZipFile(filePath, Charset.forName("GBK"));
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry;
                try {
                    entry = entries.nextElement();
                } catch (IllegalArgumentException exception) {
                    if (!exception.getMessage().toUpperCase().startsWith("MALFORMED")) {
                        throw exception;
                    }
                    // java.lang.IllegalArgumentException: MALFORMED[1] 编码无法打开的会抛这个异常，返回null
                    return null;
                }
                
                // 不是gbk字符 返回null
                if (!StringTools.isGbkCharset(entry.getName())) {
                    return null;
                }
            }
            // gbk可以打开且校验正常，不关闭zipFile
            close = false;
        } finally {
            if (close) {
                IOUtils.close(zipFile);
            }
        }
        return zipFile;
    }
}
