package com.pugwoo.wooutils.io;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 2018-09-30
 */
public class MyPipe {

	private InputStream inputStream;
	private OutputStream outputStream;

	public MyPipe(InputStream inputStream, OutputStream outputStream) {
		this.inputStream = inputStream;
		this.outputStream = outputStream;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

}
