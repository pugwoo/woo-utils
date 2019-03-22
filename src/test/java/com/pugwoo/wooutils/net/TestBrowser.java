package com.pugwoo.wooutils.net;

import org.junit.Test;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class TestBrowser {

	@Test
	public void testGet1() throws Exception {
		Browser browser = new Browser();
		HttpResponse resp = browser.get("http://www.baidu.com");
		System.out.println(resp.getContentString());
	}
	
	// 测试上传文件
	@Test
	public void testPost() throws Exception {
		Browser browser = new Browser();
//		browser.setHttpProxy("127.0.0.1", 8888);
		Map<String, Object> params = new HashMap<>();
		BrowserPostFile file = new BrowserPostFile("hello.txt", "text/plain", "hello111".getBytes());
		params.put("file", file);
//		BrowserPostFile file2 = new BrowserPostFile("hello.png", "image/png",
//				new FileInputStream("C:\\Users\\nickt\\Desktop\\1.png"));
//		params.put("file2", file2);
		params.put("info", "myinfo");
		browser.post("http://127.0.0.1:8080/admin/admin_upload/upload",
				params);
	}

	@Test
	public void testGet() throws Exception {
//		HttpResponse resp = new Browser().get("http://www.baidu.com");
//		System.out.println(resp.getHeaders());
//		System.out.println(resp.getContentLength());
//		System.out.println(resp.getContentString());

		OutputStream out = new FileOutputStream("d:/a.txt");
		HttpResponse resp = new Browser().getAsync("http://www.baidu.com",
				out);
		while(!resp.isDownloadFinished()) {
			System.out.println(resp.getDownloadedBytes());
			Thread.sleep(100);
		}
	}
	
	public static void main(String[] args) throws Exception {
		OutputStream out = new FileOutputStream("d:/a.pdf");
		HttpResponse resp = new Browser().getAsync("http://mirrors.163.com/centos/7/isos/x86_64/CentOS-7-x86_64-DVD-1611.iso",
				out);
		while(!resp.isDownloadFinished()) {
			System.out.println(resp.getDownloadedBytes());
			Thread.sleep(100);
		}
		
		/////////////////////////// 把远程的下载转输的outputStream出到下载的InputStream，使用pipe的方式
		
		final String downUrl = "http://下载链接";
		
    	PipedInputStream in = new PipedInputStream();
    	final PipedOutputStream out2 = new PipedOutputStream(in); // 将输入流和输出流对起来
    	new Thread(new Runnable() {
    	    public void run () {
    	    	try {
					new Browser().get(downUrl, out2);
					out2.close();
				} catch (IOException e) {
//					LOGGER.error("down report fail, url:{}", downUrl, e);
				}
    	    }
    	}).start();
    	
    	Map<String, String> headers = new HashMap<String, String>();
    	headers.put("Content-type", "application/pdf");
//    	return new StreamDownloadBean("sample-" + sampleNumber + ".pdf", in, headers);

	}
	
}
