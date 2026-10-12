package com.pugwoo.wooutils.net;

import com.pugwoo.wooutils.collect.MapUtils;
import com.pugwoo.wooutils.lang.DateUtils;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

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

	/**
	 * 头部名大小写不敏感，服务端返回的不是Content-Encoding这种写法时也要能解压gzip
	 */
	@Test
	public void testGzipWithNonStandardHeaderCase() throws Exception {
		String content = "data: 你好，gzip\n\n";
		ByteArrayOutputStream gzipBytes = new ByteArrayOutputStream();
		try (GZIPOutputStream gzipOut = new GZIPOutputStream(gzipBytes)) {
			gzipOut.write(content.getBytes(StandardCharsets.UTF_8));
		}

		HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/gzip", exchange -> {
			exchange.getResponseHeaders().add("content-encoding", "gzip");
			exchange.sendResponseHeaders(200, gzipBytes.size());
			try (OutputStream out = exchange.getResponseBody()) {
				out.write(gzipBytes.toByteArray());
			}
		});
		server.start();
		try {
			HttpResponse resp = new Browser().get("http://127.0.0.1:" + server.getAddress().getPort() + "/gzip");
			assert content.equals(resp.getContentString());
		} finally {
			server.stop(0);
		}
	}
	
}
