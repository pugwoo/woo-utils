package com.pugwoo.wooutils.compress;

import com.pugwoo.wooutils.io.IOUtils;
import com.pugwoo.wooutils.string.StringTools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
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
    
    public interface Iterator {
        /**
         * 获得下一个ZipItem，当返回null则表示读取完
         * @return zipItem or null
         * @throws IOException 解压操作异常 包括读取流异常/字符集错误等
         */
        ZipItem next() throws IOException;
    }
    
    public static class ZipInputStreamIterator implements Iterator {
        private final ZipInputStream zin;
        private ZipInputStreamIterator(ZipInputStream zin) {
            this.zin = zin;
        }
        
        /**
         * 获得下一个ZipItem，当返回null则表示读取完。<br>
         * 【重要】ZipItem中的输入流in必须在本次读取完。
         * @return zipItem
         * @throws IOException 解压操作异常 {@link ZipInputStream#getNextEntry()}
         */
        @Override
        public ZipItem next() throws IOException {
            ZipEntry ze = zin.getNextEntry();
            if (ze == null) {
                return null;
            }
            ZipItem zipItem = new ZipItem();
            zipItem.fileName = ze.getName();
            zipItem.in = zin;
            return zipItem;
        }
    }
    
    public static class ZipFileIterator implements Iterator {
        private final ZipFile zipFile;
        private final Enumeration<? extends ZipEntry> entries;
        public ZipFileIterator(ZipFile zipFile) {
            this.zipFile = zipFile;
            this.entries = zipFile.entries();
        }
        
        /**
         * 获得下一个ZipItem，当返回null则表示读取完。<br>
         * zipFile关闭之后不能再调用next()
         */
        @Override
        public ZipItem next() throws IOException {
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (!zipEntry.isDirectory()) {
                    ZipItem zipItem = new ZipItem();
                    zipItem.fileName = zipEntry.getName();
                    zipItem.in = zipFile.getInputStream(zipEntry);
                    return zipItem;
                }
            }
            return null;
        }
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
     * 解压文件，节省内存方式
     * @param in 不会自动close，close之后不能再调用Iterator.next()，请在处理完压缩包内文件后关闭
     * @return zipInputStreamIterator
     */
    public static ZipInputStreamIterator unzip(InputStream in) {
        ZipInputStream zin = new ZipInputStream(in);
        return new ZipInputStreamIterator(zin);
    }
    
    /**
     * 解压文件，会将文件解压到内存中，该方式会占用较多内存
     * @param in 不会自动close，调用后可以立即关闭，因为数据都加载到内存中了
     * @return zipItemList 注意zipItem中的in要自行关闭
     */
    public static List<ZipItem> unzipAll(InputStream in) throws IOException {
        Iterator it = unzip(in);
        ZipItem zipItem;
        List<ZipItem> zipItems = new ArrayList<>();
        try {
            while ((zipItem = it.next()) != null) {
                ByteArrayOutputStream out = null;
                try {
                    out = new ByteArrayOutputStream();
                    IOUtils.copy(zipItem.in, out);
                    zipItem.in = new ByteArrayInputStream(out.toByteArray());
                    zipItems.add(zipItem);
                } finally {
                    IOUtils.close(out);
                }
            }
        } catch (IOException ioException) {
            for (ZipItem item : zipItems) {
                IOUtils.close(item.in);
            }
            throw ioException;
        }
        return zipItems;
    }
    
    /**
     * 解压文件
     * @param zipFile 不会自动close，close之后不能再调用next()，请在处理完压缩包内文件后关闭
     * @return ZipFileIterator
     */
    public static ZipFileIterator unzip(ZipFile zipFile) {
        return new ZipFileIterator(zipFile);
    }
    
    /**
     * 解压文件
     * @param zipFile 不会自动close，close之后不能操作zipItem.in，请在处理完压缩包内文件后关闭
     * @return zipItemList
     * @throws IOException zip操作的相关异常 见{@link ZipFile#getInputStream(ZipEntry)}
     */
    public static List<ZipItem> unzipAll(ZipFile zipFile) throws IOException {
        Iterator it = unzip(zipFile);
        List<ZipItem> zipItems = new ArrayList<>();
        ZipItem zipItem;
        while((zipItem = it.next()) != null) {
            zipItems.add(zipItem);
        }
        return zipItems;
    }
    
    /**
     * 获取zipFile
     *   会先尝试使用gbk编码打开，如果失败，则使用默认的编码(UTF-8)
     * @param zipPath 路径
     * @return zipFile
     * @throws IOException 见{@link ZipFile#ZipFile(File)}
     */
    public static ZipFile getZipFile(String zipPath) throws IOException {
        return getZipFile(zipPath, null);
    }
    
    /**
     * 获取zipFile
     * @param zipPath 路径
     * @param charset 如果提供，则使用指定的编码打开；
     *                如果未提供，会先尝试使用gbk编码打开，如果失败，则使用默认的编码(UTF-8)
     * @return zipFile
     * @throws IOException 见{@link ZipFile#ZipFile(File, Charset)}
     */
    public static ZipFile getZipFile(String zipPath, Charset charset) throws IOException {
        return getZipFile(new File(zipPath), charset);
    }
    
    /**
     * 获取zipFile
     *   会先尝试使用gbk编码打开，如果失败，则使用默认的编码(UTF-8)
     * @param file zip文件
     * @return zipFile
     * @throws IOException 见{@link ZipFile#ZipFile(File, Charset)}
     */
    public static ZipFile getZipFile(File file) throws IOException {
        return getZipFile(file, null);
    }
    
    /**
     * 获取zipFile
     * @param file zip文件
     * @param charset 如果提供，则使用指定的编码打开；
     *                如果未提供，会先尝试使用gbk编码打开，如果失败，则使用默认的编码(UTF-8)
     * @return zipFile
     * @throws IOException 见{@link ZipFile#ZipFile(File, Charset)}
     */
    public static ZipFile getZipFile(File file, Charset charset) throws IOException {
        if (charset != null) {
            return new ZipFile(file, charset);
        }
        ZipFile zipFile = getZipFileCharsetGbk(file);
        if (zipFile == null) {
            zipFile = new ZipFile(file);
        }
        return zipFile;
    }
    
    /**
     * 获取用GBK打开的zip
     *   会判断文件名编码是否为真正的GBK，否则返回null
     * @param file zip文件
     * @return zipFile or null
     * @throws IOException 见{@link ZipFile#ZipFile(File, Charset)} 仅过滤掉字符的异常
     */
    private static ZipFile getZipFileCharsetGbk(File file) throws IOException {
        ZipFile zipFile = null;
        // 默认不关闭 因为要返回
        boolean close = true;
        try {
            zipFile = new ZipFile(file, Charset.forName("GBK"));
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
