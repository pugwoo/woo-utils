package com.pugwoo.wooutils.compress;

import com.pugwoo.wooutils.compress.ZipUtils.Iterator;
import com.pugwoo.wooutils.compress.ZipUtils.ZipItem;
import com.pugwoo.wooutils.io.IOUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestZipUtils {
	
	/**
	 * 测试用的路径
	 *  在windows下，该文件会默认放在当前项目执行时的盘符下
	 */
	private final String testPath = "/tmp/test.zip";
	
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
		Iterator it = null;
		try {
			fileInputStream = new FileInputStream(testPath);
			it = ZipUtils.unzip(fileInputStream);
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
	
}
