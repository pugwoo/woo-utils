package com.pugwoo.wooutils.compress;

import com.pugwoo.wooutils.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
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
    
    /**
     * 压缩文件
     * @param zipItems zipItemList 全部zipItem.in会自动关闭
     * @param out 会自动close
     */
    public static void zip(List<ZipItem> zipItems, OutputStream out) throws IOException {
        ZipOutputStream zipOut = null;
        try {
            zipOut = new ZipOutputStream(out);
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
    
}
