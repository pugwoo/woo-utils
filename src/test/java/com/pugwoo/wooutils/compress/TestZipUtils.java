package com.pugwoo.wooutils.compress;

import com.pugwoo.wooutils.compress.ZipUtils.ZipItem;
import com.pugwoo.wooutils.io.IOUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestZipUtils {
    
    /**
     * 测试用的路径
     *  在windows下，该文件会默认放在当前项目执行时的盘符下，如 D:\tmp\test.zip
     */
    private final String testPath = "/tmp/test.zip";
    private final String testPathGbk = "/tmp/gbk.zip";

    static {
        new File("/tmp").mkdirs();
    }
    
    @Test
    public void test01_zipUtf8() throws IOException {
        System.out.println(" ================================= 使用默认编码(UTF-8)进行打包 ZipUtils.zip : " + testPath);
        String str = "hello";
        List<ZipItem> items = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            ZipItem zipItem = new ZipItem();
            zipItem.fileName = "hello/" + i + "/" + i + ".txt";
            zipItem.in = new ByteArrayInputStream(str.getBytes());
            items.add(zipItem);
        }
        ZipUtils.zip(items, new FileOutputStream(testPath));
    }
    
    @Test
    public void test02_zipGbk() throws IOException {
        System.out.println(" ================================= 指定使用GBK进行打包 ZipUtils.zip : " + testPathGbk);
        String str = "hello";
        List<ZipItem> items = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            ZipItem zipItem = new ZipItem();
            zipItem.fileName = "hello/" + i + "/德玛西亚.txt";
            zipItem.in = new ByteArrayInputStream(str.getBytes());
            items.add(zipItem);
        }
        ZipUtils.zip(items, new FileOutputStream(testPathGbk), Charset.forName("GBK"));
    }
    
    @Test
    public void test11_unzipUtf8() throws IOException {
        unzip(testPath, null, "兼容方式打开utf8编码的zip");
    }
    
    @Test
    public void test12_unzip_gbk() throws IOException {
        unzip(testPathGbk, null, "兼容方式打开gbk编码的zip");
    }
    
    @Test
    public void test13_unzip_gbk() throws IOException {
        boolean isThrowException = false;
        try {
            unzip(testPathGbk, StandardCharsets.UTF_8, "UTF8打开gbk编码的zip");
        } catch (Exception exception) {
            String message = exception.getMessage();
            // java.lang.IllegalArgumentException: MALFORMED[1] 编码无法打开的会抛这个异常
            if (message != null && exception.getMessage().toUpperCase().startsWith("MALFORMED")) {
                System.out.println("编码异常: " + exception.getClass() + " : " + exception.getMessage());
                return;
            }
            // throw exception;
            isThrowException = true;
        }
        assert isThrowException;
    }
    
    private void unzip(String filePath, Charset charset, String msg) throws IOException {
        System.out.println(" ================================= " + msg + "ZipUtils.unzip : " + filePath + " - " + charset);
        ZipUtils.unzip(filePath, charset, zipItemList -> {
            if (zipItemList.isEmpty()) {
                throw new RuntimeException("压缩包中没有文件");
            }
            for (ZipItem zipItem : zipItemList) {
                System.out.print(zipItem.fileName);
                System.out.print(":");
                try {
                    IOUtils.copy(zipItem.in, System.out);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // zipItem.in会自行关闭 调用者也可自行关闭
                // IOUtils.close(zipItem.in);
                System.out.println();
            }
        });
    }
}
