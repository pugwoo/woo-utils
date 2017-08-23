package com.pugwoo.wooutils.compress;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.pugwoo.wooutils.compress.ZipUtils.Iterator;
import com.pugwoo.wooutils.compress.ZipUtils.ZipItem;
import com.pugwoo.wooutils.io.IOUtils;

public class TestZipUtils {

	public static void main(String[] args) throws Exception {
		String str = "hello";
		List<ZipItem> items = new ArrayList<>();
		for(int i = 0; i < 100; i++) {
			ZipItem zipItem = new ZipItem();
			zipItem.fileName = "hello/" + i + "/" + i + ".txt";
			zipItem.in = new ByteArrayInputStream(str.getBytes());
			items.add(zipItem);
		}
		ZipUtils.zip(items, new FileOutputStream("d:/test.zip"));
		
		Iterator it = ZipUtils.unzip(new FileInputStream("d:/test.zip"));
		ZipItem zipItem = null;
		while((zipItem = it.next()) != null) {
			System.out.print(zipItem.fileName);
			System.out.print(":");
			IOUtils.copy(zipItem.in, System.out);
			System.out.println();
		}
		
		List<ZipItem> zipItems = ZipUtils.unzipAll(new FileInputStream("d:/test.zip"));
		for(ZipItem z : zipItems) {
			System.out.print(z.fileName);
			System.out.print(":");
			IOUtils.copy(z.in, System.out);
			System.out.println();
		}
	}
	
}
