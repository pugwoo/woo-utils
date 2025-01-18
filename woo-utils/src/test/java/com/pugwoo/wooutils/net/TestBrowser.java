package com.pugwoo.wooutils.net;

import com.pugwoo.wooutils.collect.MapUtils;
import com.pugwoo.wooutils.lang.DateUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
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

	// 测试上传文件
	@Test
	public void testPost() throws Exception {
		Browser browser = new Browser();
		Map<String, Object> params = new HashMap<>();
		BrowserPostFile file = new BrowserPostFile("hello.txt", "text/plain", "hello111".getBytes());
		params.put("file", file);
		params.put("info", "myinfo");
		HttpResponse resp = browser.post("http://127.0.0.1:8080/upload", params);
		assert resp.getResponseCode() == 200;
		assert "hello111,myinfo".equals(resp.getContentString());
	}

	@Test
	public void testPostJson() throws Exception {
		Browser browser = new Browser();
		Map<String, Object> params = new HashMap<>();
		params.put("name", "nick");
		params.put("age", 18);
		HttpResponse resp = browser.postJson("http://127.0.0.1:8080/json_param", params);
		System.out.println(resp.getContentString());
		assert resp.getResponseCode() == 200;
		assert "name=nick, age=18".equals(resp.getContentString());
	}

	@Test
	public void testAsyncDownload() throws Exception {
		File tempFile = File.createTempFile("woo-utils-test", ".txt");
		FileOutputStream out = new FileOutputStream(tempFile);

		HttpResponse resp = new Browser().getAsync("http://127.0.0.1:8080/download?content=hello123456", out);
		while(!resp.isDownloadFinished()) {
			Thread.sleep(100);
		}

		byte[] fileBytes = Files.readAllBytes(tempFile.toPath());
		assert "hello123456".equals(new String(fileBytes));

		tempFile.deleteOnExit();
	}

	/**
	 * 测试重试场景下的inputstream和outputstream是否完整，请先准备一个接口，它会随机成功或失败
	 */
	@Test
	public void testRetryWithStream() throws IOException {
		Browser browser = new Browser();

		browser.setRetryTimes(100);

		for (int i = 0; i < 100; i++) {
			HttpResponse resp = browser.get("http://127.0.0.1:8080/random_fail", MapUtils.of("name", "nick3"));
			System.out.println("get status code:" + resp.getResponseCode() + "," + resp.getContentString());
			assert resp.getResponseCode() == 200;
			assert resp.getContentString().equals("ok, your name is:nick3");

			resp = browser.post("http://127.0.0.1:8080/random_fail", MapUtils.of("name", "nick7"));
			System.out.println("post status code:" + resp.getResponseCode() + "," + resp.getContentString());
			assert resp.getResponseCode() == 200;
			assert resp.getContentString().equals("ok, your name is:nick7");
		}

		File tempFile = File.createTempFile("woo-utils-test", ".txt");
		FileOutputStream out = new FileOutputStream(tempFile);
		out.write("name=nick5".getBytes());
		out.close();

		for (int i = 0; i < 100; i++) {
			FileInputStream in = new FileInputStream(tempFile);
			browser.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
			HttpResponse resp = browser.post("http://127.0.0.1:8080/random_fail", in);
			System.out.println("post status code:" + resp.getResponseCode() + "," + resp.getContentString());
			assert resp.getResponseCode() == 200;
			assert resp.getContentString().equals("ok, your name is:nick5");
		}

		tempFile.deleteOnExit();
	}
	
}
