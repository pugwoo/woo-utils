package com.pugwoo.wooutils.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.common.io.ByteStreams;

/**
 * IO相关常用操作
 * @author nick
 */
public class IOUtils {

	public static void copy(InputStream in, OutputStream out) throws IOException {
		ByteStreams.copy(in, out);
	}
	
}
