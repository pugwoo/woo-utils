package com.pugwoo.wooutils.io;

import java.io.*;
import java.util.Scanner;

/**
 * IO相关常用操作
 * @author nick
 */
public class IOUtils {

	/**
	 * 从inputstream读取字节并输出到to中
	 * @param from
	 * @param to 需要自行关闭输出流
	 * @return
	 * @throws IOException
	 */
	public static long copy(InputStream from, OutputStream to) throws IOException {
	    byte[] buf = new byte[8192];
	    long total = 0;
	    while (true) {
	      int r = from.read(buf);
	      if (r == -1) {
	        break;
	      }
	      to.write(buf, 0, r);
	      total += r;
	    }
	    return total;
	}

	/**
	 * 读取所有的输入流数据为byte[]
	 * @param in
	 * @return
	 */
	public static byte[] readAll(InputStream in) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[4096];
		while ((nRead = in.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}

		buffer.flush();
		return buffer.toByteArray();
	}
	
	/**
	 * 读取input所有数据到String中，可用于读取文件内容到String。
	 * @param in
	 * @param charset
	 * @return
	 */
	public static String readAll(InputStream in, String charset) {
		Scanner scanner = new Scanner(in, charset);
		String content = scanner.useDelimiter("\\Z").next();
		scanner.close();
		return content;
	}

	/**
	 * 获取一条管道，也即提供了这条管道的输出流和输入流。
	 *  调用者使用后请自行调用 inputStream和outputStream的close方法关闭流
	 */
	public static MyPipe getPipe() throws IOException {
		PipedInputStream inputStream = new PipedInputStream();
		PipedOutputStream outputStream = new PipedOutputStream(inputStream);
		return new MyPipe(inputStream, outputStream);
	}

}
