package com.pugwoo.wooutils.net;

import java.io.FileOutputStream;
import java.io.OutputStream;

import org.junit.Test;

public class TestBrowser {

	@Test
	public void testGet() throws Exception {
		HttpResponse resp = new Browser().get("http://www.baidu.com");
		System.out.println(resp.getHeaders());
		System.out.println(resp.getContentLength());
		System.out.println(resp.getContentString());
		
		
	}
	
	public static void main(String[] args) throws Exception {
		OutputStream out = new FileOutputStream("d:/a.pdf");
		HttpResponse resp = new Browser().getAsync("http://mirrors.163.com/centos/7/isos/x86_64/CentOS-7-x86_64-DVD-1611.iso",
				out);
		while(!resp.isDownloadFinished()) {
			System.out.println(resp.getDownloadedBytes());
			Thread.sleep(100);
		}
	}

}
