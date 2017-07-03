package com.pugwoo.wooutils.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 2016年2月4日 15:16:42 
 * 模拟一个浏览器发HTTP请求，不包括页面处理
 * 
 * 计划支持的特性：
 * 1. 支持指定为输出流 【done】
 * 2. 支持cookie,不支持过期特性 【done】
 * 3. 支持指定proxy【done】
 * 4. 支持超时和重试，默认超时1分钟，重试次数10次
 * 5. 支持程序写cookie，模拟javascript写cookie
 * 
 * @author pugwoo@gmail.com
 */
public class Browser {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Browser.class);
	
	public class HttpResponseFuture {
		/**已经下载的字节数*/
		public long downloadedBytes;
		/**是否已经下载完成*/
		public boolean isFinished;
	}
		
	/**cookie, domain(域根目录) -> key/value*/
	private Map<String, Map<String, String>> cookies = new HashMap<String, Map<String,String>>();

	/** 默认浏览器userAgent:Chrome Win7*/
	private String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.93 Safari/537.36";

	public void setUserAgent(String userAgent) {
		this.USER_AGENT = userAgent;
	}
	
	/**连接和读取的超时时间，也即最长的超时时间是timeoutSeconds*2*/
	private int timeoutSeconds = 60;
	/**重试次数*/
	private int retryTimes = 10;
	
	/**
	 * 代理
	 */
	private Proxy proxy = null;
	
	/**
	 * 设置http代理
	 * @param ip
	 * @param port
	 * @return
	 */
	public Browser setHttpProxy(String ip, int port) {
		proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
		return this;
	}
	
	/**
	 * post方式请求HTTP
	 * @param httpUrl
	 * @return
	 * @throws IOException
	 */
	public HttpResponse post(String httpUrl) throws IOException {
		return post(httpUrl, new HashMap<String, Object>());
	}
	
	/**
	 * post方式请求HTTP
	 * @param httpUrl
	 * @return
	 * @throws IOException
	 */
	public HttpResponse post(String httpUrl, OutputStream outputStream) throws IOException {
		return post(httpUrl, new HashMap<String, Object>(), outputStream);
	}
	
	/**
	 * post方式请求HTTP
	 * @param httpUrl
	 * @return
	 * @throws IOException
	 */
	public HttpResponse postAsync(String httpUrl, OutputStream outputStream) throws IOException {
		return postAsync(httpUrl, new HashMap<String, Object>(), outputStream);
	}
	
	/**
	 * post方式请求HTTP
	 * @param httpUrl
	 * @return
	 * @throws IOException
	 */
	public HttpResponse post(String httpUrl, Map<String, Object> params) throws IOException {
		return post(httpUrl, buildPostString(params));
	}
	
	/**
	 * post方式请求HTTP
	 * @param httpUrl
	 * @return
	 * @throws IOException
	 */
	public HttpResponse post(String httpUrl, Map<String, Object> params, OutputStream outputStream) throws IOException {
		return post(httpUrl, buildPostString(params), outputStream);
	}
	
	/**
	 * post方式请求HTTP
	 * @param httpUrl
	 * @return
	 * @throws IOException
	 */
	public HttpResponse postAsync(String httpUrl, Map<String, Object> params, OutputStream outputStream) throws IOException {
		return postAsync(httpUrl, buildPostString(params), outputStream);
	}
	
	/**
	 * post方式请求HTTP
	 * @param httpUrl
	 * @return
	 * @throws IOException
	 */
	public HttpResponse post(String httpUrl, byte[] postData) throws IOException {
		return post(httpUrl, new ByteArrayInputStream(postData), null);
	}
	
	/**
	 * post方式请求HTTP
	 * @param httpUrl
	 * @return
	 * @throws IOException
	 */
	public HttpResponse post(String httpUrl, byte[] postData, OutputStream outputStream) throws IOException {
		return post(httpUrl, new ByteArrayInputStream(postData), outputStream);
	}
	
	/**
	 * post方式请求HTTP
	 * @param httpUrl
	 * @return
	 * @throws IOException
	 */
	public HttpResponse postAsync(String httpUrl, byte[] postData, OutputStream outputStream) throws IOException {
		return postAsync(httpUrl, new ByteArrayInputStream(postData), outputStream);
	}
	
	/**
	 * post方式请求HTTP
	 * @param httpUrl
	 * @return
	 * @throws IOException
	 */
	public HttpResponse post(String httpUrl, InputStream inputStream) throws IOException {
		return post(httpUrl, inputStream, null);
	}
	
	/**
	 * post方式请求HTTP
	 * @param httpUrl
	 * @param outputStream 如果指定了输出流，则输出到指定的输出流，此时返回的值没有html正文bytes
	 * @return
	 * @throws IOException
	 */
	public HttpResponse post(String httpUrl, InputStream inputStream, OutputStream outputStream)
			throws IOException {
		return post(httpUrl, inputStream, outputStream, false);
	}
	
	/**
	 * post方式请求HTTP，异步方式
	 * @param httpUrl
	 * @param inputStream
	 * @param outputStream 如果指定了输出流，则输出到指定的输出流，此时返回的值没有html正文bytes
	 * @return
	 * @throws IOException
	 */
	public HttpResponse postAsync(String httpUrl, InputStream inputStream, OutputStream outputStream)
	        throws IOException {
		return post(httpUrl, inputStream, outputStream, true);
	}
	
	private HttpResponse post(String httpUrl, InputStream inputStream, OutputStream outputStream,
			boolean isAsync) throws IOException {
		IOException ie = null;
		for(int i = 0; i < retryTimes; i++) {
			try {
				HttpURLConnection urlConnection = getUrlConnection(httpUrl, "POST");
				
				// POST 数据
				if(inputStream != null) {
					urlConnection.setDoOutput(true);
			        OutputStream os = urlConnection.getOutputStream();
			        byte[] buf = new byte[4096];
			        int readBytes = 0;
			        while((readBytes = inputStream.read(buf)) != -1) {
			        	os.write(buf, 0, readBytes);
			        }
			        os.flush();
			        os.close();
				}
		        
				return makeHttpResponse(httpUrl, urlConnection, outputStream, isAsync);
			} catch (IOException e) {
				LOGGER.error("get url:{} exception", httpUrl, e);
				ie = e;
			}
		}
		throw ie;

	}
	
	/////////////////////// =========================================================
	
	/**
	 * get方式请求HTTP,处理细节：<br>
	 * 1. 如果是301或302跳转，且跳转链接不同于当前链接，则再请求跳转的URL
	 * 
	 * @param httpUrl
	 * @return
	 * @throws IOException
	 */
	public HttpResponse get(String httpUrl) throws IOException {
		return get(httpUrl, null, null);
	}

	/**
	 * get方式请求HTTP,处理细节：<br>
	 * 1. 如果是301或302跳转，且跳转链接不同于当前链接，则再请求跳转的URL
	 * 
	 * @param httpUrl
	 * @return
	 * @throws IOException
	 */
	public HttpResponse get(String httpUrl, OutputStream outputStream) throws IOException {
		return get(httpUrl, null, outputStream);
	}
	
	/**
	 * get方式请求HTTP,处理细节：<br>
	 * 1. 如果是301或302跳转，且跳转链接不同于当前链接，则再请求跳转的URL
	 * 
	 * @param httpUrl
	 * @return
	 * @throws IOException
	 */
	public HttpResponse getAsync(String httpUrl, OutputStream outputStream) throws IOException {
		return getAsync(httpUrl, null, outputStream);
	}
	
	/**
	 * get方式请求HTTP,处理细节：<br>
	 * 1. 如果是301或302跳转，且跳转链接不同于当前链接，则再请求跳转的URL
	 * 
	 * @param httpUrl
	 * @return
	 * @throws IOException
	 */
	public HttpResponse get(String httpUrl, Map<String, Object> params) throws IOException {
		return get(httpUrl, params, null, false);
	}
	
	/**
	 * get方式请求HTTP,处理细节：<br>
	 * 1. 如果是301或302跳转，且跳转链接不同于当前链接，则再请求跳转的URL
	 * 
	 * @param httpUrl
	 * @param params
	 * @param outputStream
	 * @return
	 */
	public HttpResponse get(String httpUrl, Map<String, Object> params, OutputStream outputStream) 
	        throws IOException {
		return get(httpUrl, params, outputStream, false);
	}
	
	/**
	 * get方式请求HTTP,处理细节：<br>
	 * 1. 如果是301或302跳转，且跳转链接不同于当前链接，则再请求跳转的URL
	 * 
	 * @param httpUrl
	 * @param params
	 * @param outputStream
	 * @return
	 */
	public HttpResponse getAsync(String httpUrl, Map<String, Object> params, OutputStream outputStream) 
	        throws IOException {
		return get(httpUrl, params, outputStream, true);
	}
	
	/**
	 * get方式请求HTTP,处理细节：<br>
	 * 1. 如果是301或302跳转，且跳转链接不同于当前链接，则再请求跳转的URL
	 * 
	 * @param httpUrl
	 * @return
	 * @throws IOException
	 */
	private HttpResponse get(String httpUrl, Map<String, Object> params, OutputStream outputStream,
			boolean isAsync) throws IOException {
		httpUrl = appendParamToUrl(httpUrl, params);
		IOException ie = null;
		for(int i = 0; i < retryTimes; i++) {
			try {
				HttpURLConnection urlConnection = getUrlConnection(httpUrl, "GET");
				
				// 301 302 跳转处理
				int responseCode = urlConnection.getResponseCode();
				if(responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
					List<String> location = urlConnection.getHeaderFields().get("Location");
					if(location != null && !location.isEmpty()) {
						if(!httpUrl.equals(location.get(0))) {
							return get(location.get(0), params, outputStream);
						}
					}
				}

				return makeHttpResponse(httpUrl, urlConnection, outputStream, isAsync);
			} catch (IOException e) {
				LOGGER.error("get url:{} error", httpUrl, e);
				ie = e;
			}
		}
		throw ie;
	}
	
	/////////////////// 以下是工具方法 ////////////////////////////
	
	/**
	 * 获取httpUrl的域名
	 * @param httpUrl
	 * @return
	 */
	private static String getHost(String url) {
	    if(url == null || url.isEmpty()) {
	    	return "";
	    }

	    int doubleslash = url.indexOf("//");
	    if(doubleslash == -1) {
	        doubleslash = 0;
	    } else {
	        doubleslash += 2;
	    }

	    int end = url.indexOf('/', doubleslash);
	    end = end >= 0 ? end : url.length();

	    int port = url.indexOf(':', doubleslash);
	    end = (port > 0 && port < end) ? port : end;

	    return url.substring(doubleslash, end);
	}
			
	/**
	 * 拿到http连接对象
	 * @param httpUrl
	 * @param method
	 * @return
	 * @throws IOException
	 */
	private HttpURLConnection getUrlConnection(String httpUrl, String method) throws IOException {
		URL url = new URL(httpUrl);
		HttpURLConnection urlConnection = null;
		if(proxy == null) {
			urlConnection = (HttpURLConnection) url.openConnection();
		} else {
			urlConnection = (HttpURLConnection) url.openConnection(proxy);
		}
		urlConnection.setConnectTimeout(timeoutSeconds * 1000);
		urlConnection.setReadTimeout(timeoutSeconds * 1000);
		urlConnection.setRequestMethod(method);
		urlConnection.setRequestProperty("User-agent", USER_AGENT);
		urlConnection.setRequestProperty("Referer", httpUrl);
		
		// 设置cookie
		String host = getHost(httpUrl);
		StringBuilder cookieSb = new StringBuilder();
		boolean needAppendAnd = false;
		for(Entry<String, Map<String, String>> entry : cookies.entrySet()) {
			if(host.endsWith(entry.getKey())) {
				for(Entry<String, String> cookie : entry.getValue().entrySet()) {
					if(needAppendAnd) {
						cookieSb.append("; ");
					}
					cookieSb.append(cookie.getKey());
					cookieSb.append("=");
					cookieSb.append(cookie.getValue());
					needAppendAnd = true;
				}
			}
		}
		urlConnection.setRequestProperty("Cookie", cookieSb.toString());
		return urlConnection;
	}
	
	/**
	 * 构造httpResponse
	 * @param urlConnection
	 * @param httpResponse
	 * @param outputStream
	 * @param isAsync 是否异步，只有当outputStream!=null时，该值才有效。
	 *        当isAsync为true时，HttpResponse可以获得已下载的字节数。
	 * @throws IOException 
	 */
	private HttpResponse makeHttpResponse(String httpUrl, HttpURLConnection urlConnection,
			final OutputStream outputStream, boolean isAsync) throws IOException {
		HttpResponse httpResponse = new HttpResponse();
		httpResponse.setResponseCode(urlConnection.getResponseCode());
		httpResponse.setHeaders(urlConnection.getHeaderFields());
		
		// 处理cookie
		List<String> setCookies = urlConnection.getHeaderFields().get("Set-Cookie");
		if(setCookies != null && !setCookies.isEmpty()) {
			for(int i = setCookies.size() - 1; i >=0; i--) {
				String key = null, value = null, domain = ""; 
				// domain default, hack 因为有些网站Set-Cookie没有指定domain，但是现在domain又不好取root domain，所以只能hack让所有网站生效
				String strs[] = setCookies.get(i).split(";");
				boolean isFirst = true;
				for(String str : strs) {
					str = str.trim();
					String keyvalue[] = str.split("=", 2);
					if(isFirst && keyvalue.length == 2) {
						key = keyvalue[0];
						value = keyvalue[1];
					} else if(keyvalue.length == 2 && "Domain".equalsIgnoreCase(keyvalue[0])){
						domain = keyvalue[1];
					}
					isFirst = false;
				}
				if(domain != null && key != null) {
					if(!cookies.containsKey(domain)) {
						cookies.put(domain, new HashMap<String, String>());
					}
					cookies.get(domain).put(key, value);
				}
			}
		}
		
		final InputStream in = urlConnection.getInputStream();
		byte[] buf = new byte[4096];
		int len;
		if(outputStream != null) {
			if(isAsync) {
				final HttpResponseFuture future = new HttpResponseFuture();
				httpResponse.setFuture(future);
				new Thread(new Runnable() {
					@Override
					public void run() {
						byte[] buf = new byte[4096];
						int len;
						try {
							while((len = in.read(buf)) != -1) {
								future.downloadedBytes += len;
								outputStream.write(buf, 0, len);
							}
							future.isFinished = true;
						} catch (IOException e) {
							LOGGER.error("outputStream write error", e);
						}
					}
				}).start();;
			} else {
				while((len = in.read(buf)) != -1) {
					outputStream.write(buf, 0, len);
				}
			}
		} else {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while((len = in.read(buf)) != -1) {
				baos.write(buf, 0, len);
			}
			httpResponse.setContentBytes(baos.toByteArray());
		}
		
		return httpResponse;
	}
	
	private static byte[] buildPostString(Map<String, Object> params) {
		if(params == null || params.isEmpty()) {
			try {
				return "".getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				return new byte[0];
			}
		}
		StringBuilder sb = new StringBuilder();
		boolean needAppendAnd = false;
		for(Entry<String, Object> entry : params.entrySet()) {
			try {
				if(needAppendAnd) {
					sb.append("&");
				}
				sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
				sb.append("=");
				if(entry.getValue() != null) {
					sb.append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
				}
				needAppendAnd = true;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		try {
			return sb.toString().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			return new byte[0];
		}
	}
	
	/**
	 * 将请求参数加到httpUrl后面
	 * @param httpUrl
	 * @param params
	 * @return
	 */
	private static String appendParamToUrl(String httpUrl, Map<String, Object> params) {
		if(params == null || params == null || params.isEmpty()) {
			return httpUrl;
		}
		
		StringBuilder sb = new StringBuilder(httpUrl);
		int indexOfQuestion = httpUrl.indexOf("?");
		if(indexOfQuestion == -1) {
			sb.append("?");
		} else {
			if(indexOfQuestion < httpUrl.length() - 1 && !httpUrl.endsWith("&")) {
				sb.append("&");
			}
		}
		
		boolean needAppendAnd = false;
		for(Entry<String, Object> entry : params.entrySet()) {
			try {
				if(needAppendAnd) {
					sb.append("&");
				}
				sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
				sb.append("=");
				if(entry.getValue() != null) {
					sb.append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
				}
				needAppendAnd = true;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		return sb.toString();
	}
	
	
}
