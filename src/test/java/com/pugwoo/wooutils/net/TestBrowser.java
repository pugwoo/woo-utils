package com.pugwoo.wooutils.net;

import java.io.IOException;

import org.junit.Test;

public class TestBrowser {
	
	@Test
	public void testGet() throws Exception {
		System.out.println(new Browser().get("http://www.baidu.com").getContentString());
	}
	
	@Test
	public void testPost() throws Exception {
		Browser browser = new Browser();
		browser.get("http://www.oschina.net/");
		
		HttpResponse httpResponse = browser.get("http://my.oschina.net/pugwoo/admin/inbox");
		System.out.println(httpResponse.getContentString());
	}

	public static void main(String[] args) throws IOException {

	}
}
