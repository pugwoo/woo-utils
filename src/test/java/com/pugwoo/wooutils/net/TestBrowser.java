package com.pugwoo.wooutils.net;

import java.io.IOException;

import org.junit.Test;

public class TestBrowser {
	
	@Test
	public void testGet() throws Exception {
		System.out.println(new Browser().get("http://www.baidu.com").getContentString());
	}

	public static void main(String[] args) throws IOException {

	}
}
