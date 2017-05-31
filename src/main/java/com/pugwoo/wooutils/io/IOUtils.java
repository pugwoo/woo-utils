package com.pugwoo.wooutils.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
	
}
