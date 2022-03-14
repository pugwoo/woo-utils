package com.pugwoo.wooutils.compress;

import com.pugwoo.wooutils.compress.ZipUtils.Iterator;
import com.pugwoo.wooutils.compress.ZipUtils.ZipItem;
import com.pugwoo.wooutils.io.IOUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestZipUtils {
	
	/**
	 * 测试用的路径
	 *  在windows下，该文件会默认放在当前项目执行时的盘符下，如 D:\tmp\test.zip
	 */
	private final String testPath = "/tmp/test.zip";
	private final String testPathGbk = "/tmp/gbk.zip";
	
	@Test
	public void test1_zip() throws IOException {
		System.out.println(" ================================= ZipUtils.zip : " + testPath);
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
	public void test2_unzip_inputStream() throws IOException {
		System.out.println("\n ================================= ZipUtils.unzip inputStream : " + testPath);
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(testPath);
			Iterator it = ZipUtils.unzip(fileInputStream);
			// 未处理完成之前不能调用close方法，否则it.next()会抛java.io.IOException: Stream Closed
			// fileInputStream.close();
			ZipItem zipItem;
			while ((zipItem = it.next()) != null) {
				System.out.print(zipItem.fileName);
				System.out.print(":");
				IOUtils.copy(zipItem.in, System.out);
				System.out.println();
				// 不能关闭 也不需要自己手动关闭 关闭后回去下一个会异常 java.io.IOException: Stream closed
				// IOUtils.close(zipItem.in);
			}
		} finally {
			IOUtils.close(fileInputStream);
		}
	}
	
	@Test
	public void test2_unzipAll_inputStream() throws IOException {
		System.out.println("\n ================================= ZipUtils.unzipAll inputStream : " + testPath);
		FileInputStream fileInputStream = null;
		List<ZipItem> zipItems = null;
		try {
			fileInputStream = new FileInputStream(testPath);
			zipItems = ZipUtils.unzipAll(fileInputStream);
			// fileInputStream不会自动close，调用unzipAll后可以立即关闭，因为数据都加载到内存中了
			IOUtils.close(fileInputStream);
			for (ZipItem z : zipItems) {
				System.out.print(z.fileName);
				System.out.print(":");
				IOUtils.copy(z.in, System.out);
				IOUtils.close(z.in);
				System.out.println();
			}
		} finally {
			IOUtils.close(fileInputStream);
			if (zipItems != null) {
				for (ZipItem zipItem : zipItems) {
					IOUtils.close(zipItem.in);
				}
			}
		}
	}
	
	@Test
	public void test3_unzip_file() throws IOException {
		testFileUnzip(testPath, null);
	}
	
	@Test
	public void test3_unzipAll_file() throws IOException {
		System.out.println("\n ================================= ZipUtils.unzipAll file : " + testPath);
		ZipFile zipFile = null;
		try {
			zipFile = ZipUtils.getZipFile(testPath);
			List<ZipItem> zipItems = ZipUtils.unzipAll(zipFile);
			// zipFile不会自动close，close之后不能操作zipItem.in，请在处理完压缩包内文件后关闭
			// IOUtils.close(zipFile);
			for (ZipItem zipItem : zipItems) {
				System.out.print(zipItem.fileName);
				System.out.print(":");
				// 如果提前关闭zipFile，抛 java.io.IOException: Stream closed
				try {
					IOUtils.copy(zipItem.in, System.out);
				} finally {
					IOUtils.close(zipItem.in);
				}
				System.out.println();
			}
		} finally {
			IOUtils.close(zipFile);
		}
	}
	
	/** 指定使用GBK进行打包 */
	@Test
	public void test5_zip_gbk() throws IOException {
		System.out.println(" ================================= ZipUtils.zip : " + testPathGbk);
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
	
	/** 使用ZipUtils中兼容编码方式进行解压GBK编码的压缩包 */
	@Test
	public void test6_unzip_gbk_default() throws IOException {
		testFileUnzip(testPathGbk, null);
	}
	
	/** 指定GBK编码解压GBK编码的压缩包 */
	@Test
	public void test6_unzip_gbk_openUseGbk() throws IOException {
		testFileUnzip(testPathGbk, Charset.forName("GBK"));
	}
	
	/** 指定UTF8编码解压GBK编码的压缩包 会抛异常 */
	@Test
	public void test6_unzip_gbk_openUseUtf8() throws IOException {
		// java.lang.IllegalArgumentException: MALFORMED
		Assert.assertThrows(IllegalArgumentException.class, () -> testFileUnzip(testPathGbk, StandardCharsets.UTF_8));
	}
	
	private void testFileUnzip(String filePath, Charset charset) throws IOException {
		System.out.println("\n ================================= ZipUtils.unzip file : " + filePath);
		ZipFile zipFile = null;
		try {
			zipFile = ZipUtils.getZipFile(new File(filePath), charset);
			Iterator it = ZipUtils.unzip(zipFile);
			// zipFile不会自动close，close之后不能再调用next()，请在处理完压缩包内文件后关闭
			// 否则抛 java.lang.IllegalStateException: zip file closed
			// IOUtils.close(zipFile);
			ZipItem zipItem;
			while ((zipItem = it.next()) != null) {
				System.out.print(zipItem.fileName);
				System.out.print(":");
				try {
					IOUtils.copy(zipItem.in, System.out);
				} finally {
					IOUtils.close(zipItem.in);
				}
				System.out.println();
			}
		} finally {
			IOUtils.close(zipFile);
		}
	}
	
}
