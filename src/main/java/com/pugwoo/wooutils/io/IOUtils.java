package com.pugwoo.wooutils.io;

import com.pugwoo.wooutils.collect.ListUtils;
import com.pugwoo.wooutils.string.RegexUtils;
import com.pugwoo.wooutils.string.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * IO相关常用操作
 * @author nick
 */
public class IOUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(IOUtils.class);

	/**
	 * 从inputstream读取字节并输出到to中，直到inputstream EOF
	 * @param from 输入流
	 * @param to 需要自行关闭输出流
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
	 * @param in 读取完后in不会关闭
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
	 * 读取所有的输入流数据为byte[]
	 * @param in 自动关闭
	 * @return
	 */
	public static byte[] readAllAndClose(InputStream in) throws IOException {
		try {
			return readAll(in);
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch (Exception e) {
					// ignore
				}
			}
		}
	}
	
	/**
	 * 读取input所有数据到String中，可用于读取文件内容到String。
	 * @param in 读取完后in不会关闭
	 * @param charset
	 * @return
	 */
	public static String readAll(InputStream in, String charset) {
		Scanner scanner = new Scanner(in, charset);

		try {
			String content = scanner.useDelimiter("\\Z").next();
			return content;
		} catch (NoSuchElementException e) {
			return ""; // file is empty, ignore exception
		} finally {
			scanner.close();
		}
	}

	/**
	 * 读取input所有数据到String中，可用于读取文件内容到String。
	 * @param in 读取完后in自动关闭
	 * @param charset
	 * @return
	 */
	public static String readAllAndClose(InputStream in, String charset) {
		try {
			return readAll(in, charset);
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch (Exception e) {
					// ignore
				}
			}
		}
	}

	/**
	 * 读取classpath目录下的资源，返回为String，默认是utf-8编码。
	 * 如果需要其它编码，请获得byte[]之后自行转换。
	 * 说明：当有多个同名的资源时，会返回第一个加载到jvm的资源内容，因此这里具有随机性。
	 * @param path 路径，例如：abc.txt
	 * @return 文件不存在返回null
	 */
	public static String readClasspathResourceAsString(String path) throws IOException {
		InputStream in = readClasspathResourceInputStream(path);
		if (in == null) {
			return null;
		}
		return readAll(in, "UTF-8");
	}

	/**
	 * 读取classpath目录下的资源，返回为byte[]。
	 * 说明：当有多个同名的资源时，会返回第一个加载到jvm的资源内容，因此这里具有随机性。
	 * @param path 路径，例如：abc.txt
	 * @return 文件不存在返回null
	 */
	public static byte[] readClasspathResourceAsBytes(String path) throws IOException {
		InputStream in = readClasspathResourceInputStream(path);
		if (in == null) {
			return null;
		}
		return readAll(in);
	}

	private static InputStream readClasspathResourceInputStream(String path) {
		if (StringTools.isEmpty(path)) {
			return null;
		}
		// 分为以/开头和没有以/开头的path进行尝试，优先没有/开头的，以为classLoader的方式不需要/开头
		boolean beginWithSlash = path.startsWith("/");
		String noSlash = beginWithSlash ? path.substring(1) : path;
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(noSlash);
		if (in != null) {
			return in;
		}

		// 尝试再用/开头的进行
		String withSlash = beginWithSlash ? path : "/" + path;
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(withSlash);
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

	/**
	 * 关闭资源，不会抛出异常
	 * @param closeable
	 */
	public static void close(Closeable closeable) {
		if(closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (Exception e) {
			LOGGER.error("close resource fail", e);
		}
	}


	/**
	 * 遍历获得所有的文件（不包括文件夹）
	 * @param file    文件夹
	 * @return
	 */
	public static List<File> listFiles(File file) {
		if (file.isFile()) {
			return ListUtils.newArrayList(file);
		}

		List<File> result = new ArrayList<>();
		File[] files = file.listFiles();
		if(files == null) {
			return result;
		}
		for (File f : files) {
			result.addAll(listFiles(f));
		}
		return result;
	}

	/**
	 * 获取符合条件的文件，不包含目录
	 *
	 * @param file
	 * @param regex 文件名(不包括路径)的正则表达式
	 * @return
	 */
	public static List<File> listFiles(File file, String regex) {
		return listFiles(file, Pattern.compile(regex));
	}

	/**
	 * 获取符合条件的文件，不包含目录
	 *
	 * @param file
	 * @param pattern 文件名(不包括路径)的正则表达式
	 * @return
	 */
	public static List<File> listFiles(File file, Pattern pattern) {
		if (file.isFile()) {
			if(RegexUtils.isMatch(file.getName(), pattern)) {
				return ListUtils.newArrayList(file);
			} else {
				return new ArrayList<>();
			}
		}

		List<File> result = new ArrayList<>();
		File[] files = file.listFiles();
		if(files == null) {
			return result;
		}
		for (File f : files) {
			result.addAll(listFiles(f, pattern));
		}
		return result;
	}

}
