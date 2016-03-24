package com.pugwoo.wooutils.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * 提供二进制读取的工具
 * 2014年4月10日 23:15:03
 */
public class BinaryReader {

	/**
	 * 读取小头LE的无符号int,4字节
	 * @return
	 */
	public static long readUintLE(InputStream inputStream)
			throws EOFException, IOException {
		byte[] buffer = new byte[4];
		int readCount = readFull(inputStream, buffer);
		
		if (readCount < 0) {
			throw new EOFException();
		} else if (readCount != 4) {
			throw new IOException("read fail, require 4 bytes, but only "
					+ readCount + "bytes.");
		}
		
		long result = 0;
		result += buffer[3] & 0xFF;
		result <<= 8;
		result += buffer[2] & 0xFF;
		result <<= 8;
		result += buffer[1] & 0xFF;
		result <<= 8;
		result += buffer[0] & 0xFF;
		
		return result;
	}
	
	/**
	 * 读取小头LE的无符号short,2字节
	 * @return
	 */
	public static int readUshortLE(InputStream inputStream)
			throws EOFException, IOException {
		byte[] buffer = new byte[2];
		int readCount = readFull(inputStream, buffer);
		
		if (readCount < 0) {
			throw new EOFException();
		} else if (readCount != 2) {
			throw new IOException("read fail, require 2 bytes, but only "
					+ readCount + "bytes.");
		}
		
		int result = 0;
		result += buffer[1] & 0xFF;
		result <<= 8;
		result += buffer[0] & 0xFF;
		
		return result;
	}
	
	/**
	 * 读取无符号byte,1字节
	 * @return
	 */
	public static int readUbyte(InputStream inputStream)
			throws EOFException, IOException {
		byte[] buffer = new byte[1];
		int readCount = readFull(inputStream, buffer);
		
		if (readCount < 0) {
			throw new EOFException();
		} else if (readCount != 1) {
			throw new IOException("read fail, require 1 bytes, but only "
					+ readCount + "bytes.");
		}
		
		return buffer[0] & 0xFF;
	}
	
	/**
	 * 必须读满bytes的长度，直到EOF
	 * 
	 * @param in
	 * @param bytes
	 * @throws IOException
	 * @return 实际读取的字节数
	 */
	private static int readFull(InputStream in, byte[] bytes) throws IOException {
		if(in == null || bytes == null) {
			return -1;
		}
		
		int needReadBytes = bytes.length;
		int readBytes = 0;
		while(true) {
			int b = in.read(bytes, readBytes, needReadBytes - readBytes);
			if(b == -1) { // EOF
				break;
			}
			readBytes += b;
			if(readBytes == needReadBytes) {
				break;
			}
		}
		return readBytes;
	}

}
