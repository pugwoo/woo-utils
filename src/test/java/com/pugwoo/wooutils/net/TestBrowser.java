package com.pugwoo.wooutils.net;

import org.junit.Test;

public class TestBrowser {

	@Test
	public void testGet() throws Exception {
		System.out.println(new Browser().get("http://www.baidu.com").getContentString());
	}

	@Test
	public void testPost() throws Exception {
	}

}
