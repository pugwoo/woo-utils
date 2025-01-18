package com.pugwoo.wooutils.net;

import com.pugwoo.wooutils.collect.MapUtils;
import com.pugwoo.wooutils.lang.DateUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TestBrowser {

	@Test
	public void testGet1() throws Exception {
		Browser browser = new Browser();
		HttpResponse resp = browser.get("http://www.baidu.com");
		System.out.println(resp.getContentString());
		assert resp.getContentString().contains("百度一下");
	}

	@Test
	public void testBasicPost() throws Exception {
		Browser browser = new Browser();
		browser.setHttpProxy("127.0.0.1", 8888);
		//browser.post("http://www.baidu.com",
		//		MapUtils.of("key1", "val1", "key2", "val2"));

		OutputStream out = new FileOutputStream("d:/a.txt");

		Map<String, Object> map = MapUtils.of("key1", "val1", "key2", 33);


		//browser.addRequestHeader("Content-Type", "text/plain");

//		browser.post("http://127.0.0.1:8080/post",
//				"content".getBytes(), out);
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
//		browser.post("http://127.0.0.1:8080/admin/admin_upload/upload",
//				params);
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

	/**
	 * 下载一个将近1G的文件
	 * @throws Exception
	 */
	@Test
	public void  testDownload() throws Exception {
		OutputStream out = new FileOutputStream("d:/a.iso");
		HttpResponse resp = new Browser().getAsync(
				"https://mirrors.163.com/centos/8-stream/isos/x86_64/CentOS-Stream-8-20230523.0-x86_64-boot.iso",
				out);
		while(!resp.isDownloadFinished()) {
			System.out.println(DateUtils.format(new Date()) + " 已下载:" + resp.getDownloadedBytes());
			Thread.sleep(1000);
		}

		System.out.println("下载完成");
	}

	public static void main(String[] args) throws Exception {

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


	/**
	 * 测试重试场景下的inputstream和outputstream是否完整，请先准备一个接口，它会随机成功或失败
	 */
	@Test
	public void testRetryWithStream() throws IOException {
		Browser browser = new Browser();

		browser.setRetryTimes(100);

		for (int i = 0; i < 100; i++) {
			HttpResponse resp = browser.get("http://127.0.0.1:8080/admin-demo/random_fail", MapUtils.of("name", "nick3"));
			System.out.println("get status code:" + resp.getResponseCode() + "," + resp.getContentString());

			resp = browser.post("http://127.0.0.1:8080/admin-demo/random_fail", MapUtils.of("name", "nick7"));
			System.out.println("post status code:" + resp.getResponseCode() + "," + resp.getContentString());
		}

		for (int i = 0; i < 100; i++) {
			FileInputStream in = new FileInputStream("d:/a.txt");
			browser.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
			HttpResponse resp = browser.post("http://127.0.0.1:8080/admin-demo/random_fail", in);
			System.out.println("post status code:" + resp.getResponseCode() + "," + resp.getContentString());
		}
	}
	
}
