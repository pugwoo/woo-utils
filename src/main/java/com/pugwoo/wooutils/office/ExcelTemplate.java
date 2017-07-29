package com.pugwoo.wooutils.office;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jxls.exception.ParsePropertyException;
import net.sf.jxls.transformer.XLSTransformer;

/**
 * excel渲染模版
 * @author nick
 */
public class ExcelTemplate {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ExcelTemplate.class);

	/**
	 * 渲染Excel文件
	 * @param in 输入流 输出后render会关闭掉
	 * @param params 要渲染的数据
	 * @param out 输出流 输出后render会关闭掉
	 * @return
	 */
	public static boolean render(InputStream in, Map<String, Object> params, OutputStream out) {
		try {
			new XLSTransformer().transformXLS(in, params).write(out);
		} catch (ParsePropertyException | IOException | InvalidFormatException e) {
			LOGGER.error("ExcelTemplate render exception", e);
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch (Exception e) {}
			}
			if(out != null) {
				try {
					out.close();
				} catch (Exception e) {}
			}
		}
		return true;
	}
	
}
