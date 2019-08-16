package com.pugwoo.wooutils.net;

import com.pugwoo.wooutils.json.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 2016年2月4日 15:16:42 
 * 模拟一个浏览器发HTTP请求，不包括页面处理。默认编码是utf8，可以全局指定编码
 * 
 * 计划支持的特性：
 * 1. 支持指定为输出流 【done】
 * 2. 支持cookie,不支持过期特性 【done】
 * 3. 支持指定proxy【done】
 * 4. 支持超时和重试，默认超时1分钟，重试次数10次【done】
 * 5. 支持程序写cookie，模拟javascript写cookie【done】
 * 
 * @author pugwoo@gmail.com
 */
public class Browser {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Browser.class);
	
	public static class HttpResponseFuture {
		/**已经下载的字节数*/
		public long downloadedBytes;
		/**是否已经下载完成*/
		public boolean isFinished;
	}
		
	/**cookie, domain(域根目录) -> key/value*/
	private Map<String, Map<String, String>> cookies = new HashMap<String, Map<String,String>>();
	
	/**请求时的头部*/
	private Map<String, String> requestProperty = new HashMap<String, String>();

	/** 默认浏览器userAgent: Chrome Win10*/
	private String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.26 Safari/537.36 Core/1.63.6823.400";

	public void setUserAgent(String userAgent) {
		this.USER_AGENT = userAgent;
	}

	private String charset = "utf8";

	/**
	 * 设置整个Browser实例全局的字符编码，默认utf8
	 * @param charset
	 */
	public void setCharset(String charset) {
		this.charset = charset;
	}
	
	/**连接超时时间，秒*/
	private int connectTimeoutSeconds = 10;
    /**读取超时时间，秒*/
	private int readTimeoutSeconds = 60;
	/**重试次数*/
	private int retryTimes = 10;

	/**设置连接超时时间，默认10秒*/
	public void setConnectTimeoutSeconds(int connectTimeoutSeconds) {
		this.connectTimeoutSeconds = connectTimeoutSeconds;
	}

    /**设置读取超时时间，默认60秒*/
	public void setReadTimeoutSeconds(int readTimeoutSeconds) {
		this.readTimeoutSeconds = readTimeoutSeconds;
	}

	/**设置重试次数，默认10次*/
	public void setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
	}
	
	/**代理*/
	private Proxy proxy = null;
	
	/**信任所有ssl证书*/
	private TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}
		public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
		}
		public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
		}
	} };
	private SSLSocketFactory oldSSLSocketFactory = null;

	/**是否信任所有证书*/
	public void setIsTrustAllCerts(boolean isTrustAllCerts) {
		if(isTrustAllCerts) {
			try {
				SSLContext sc = SSLContext.getInstance("SSL");
				sc.init(null, trustAllCerts, new java.security.SecureRandom());
				if(oldSSLSocketFactory == null) {
					oldSSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
				}
				HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			} catch (Exception e) {
				LOGGER.error("set ignore ssl certs fail", e);
			}
		} else {
			HttpsURLConnection.setDefaultSSLSocketFactory(oldSSLSocketFactory);
		}
	}
	
	/**设置请求时的头部，该设置是Browser实例全局的。
	 * 注意：请不要用这个方法设置cookie，请使用addCookie方法
	 */
	public void addRequestHeader(String key, String value) {
		requestProperty.put(key, value);
	}

	/**设置请求时的头部，该设置是Browser实例全局的。<br>
	 * 设置HttpServletRequest的所有头部信息
	 * @param request HttpServletRequest
	 */
	public void addRequestHeader(HttpServletRequest request) {
		Enumeration<String> headerNames = request.getHeaderNames();
		while(headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			addRequestHeader(headerName, request.getHeader(headerName));
		}
	}

	/**
	 * 自行添加cookie
	 * @param domain 域名，例如abc.com
	 * @param key cookie名
	 * @param value cookie值
	 */
	public void addCookie(String domain, String key, String value) {
		if(cookies.get(domain) == null) {
			cookies.put(domain, new HashMap<>());
		}
		cookies.get(domain).put(key, value);
	}
	
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
	 * post方式请求HTTP，params使用queryString或formdata方式组装
	 * @param httpUrl
	 * @return
	 * @throws IOException
	 */
	public HttpResponse post(String httpUrl, Map<String, Object> params) throws IOException {
		return post(httpUrl, params, null);
	}
	
	private boolean isWithBrowserPostFile(Map<String, Object> params) {
		if(params == null) {
			return false;
		}
		for(Entry<String, Object> entry : params.entrySet()) {
			if(entry.getValue() instanceof BrowserPostFile) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * post方式请求HTTP，params使用queryString或formdata方式组装
	 * @param httpUrl
	 * @param outputStream 如果提供，则post内容将输出到该输出流，输出完之后自动close掉
	 * @return
	 * @throws IOException
	 */
	public HttpResponse post(String httpUrl, Map<String, Object> params, OutputStream outputStream) throws IOException {
		if(isWithBrowserPostFile(params)) {
			String boundary = "----WebKitFormBoundaryYp0ZBDEHwALiqVW5";
			Map<String, String> header = new HashMap<>();
			header.put("Content-Type", "multipart/form-data; boundary=" + boundary);
			return _post(httpUrl, new ByteArrayInputStream(buildPostString(params, boundary)),
					outputStream, false, header);
		} else {
			return post(httpUrl, buildPostString(params), outputStream);
		}
	}

    /**
     * post方式请求HTTP，转换成json形式提交
     * @param httpUrl
     * @param toJson
     * @return
     */
    public HttpResponse postJson(String httpUrl, Object toJson) throws IOException {
        return postJson(httpUrl, toJson, null);
    }

    /**
     * post方式请求HTTP，转换成json形式提交
     * @param httpUrl
     * @param toJson
     * @param outputStream
     * @return
     */
	public HttpResponse postJson(String httpUrl, Object toJson, OutputStream outputStream) throws IOException {
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");
        return _post(httpUrl, new ByteArrayInputStream(buildPostJson(toJson)),
                outputStream, false, header);
    }

    /**
     * 异步post方式请求HTTP，转换成json形式提交
     * @param httpUrl
     * @param toJson
     * @param outputStream
     * @return
     */
    public HttpResponse postJsonAsync(String httpUrl, Object toJson, OutputStream outputStream) throws IOException {
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");
        return _post(httpUrl, new ByteArrayInputStream(buildPostJson(toJson)),
                outputStream, true, header);
    }
	
	/**
	 * post方式请求HTTP，params使用queryString或formdata方式组装
	 * @param httpUrl
	 * @param outputStream 如果提供，则post内容将输出到该输出流，输出完之后自动close掉
	 * @return
	 * @throws IOException
	 */
	public HttpResponse postAsync(String httpUrl, Map<String, Object> params, OutputStream outputStream) throws IOException {
		if(isWithBrowserPostFile(params)) {
			String boundary = "----WebKitFormBoundaryYp0ZBDEHwALiqVW5";
			Map<String, String> header = new HashMap<>();
			header.put("Content-Type", "multipart/form-data; boundary=" + boundary);
			return _post(httpUrl, new ByteArrayInputStream(buildPostString(params, boundary)),
					outputStream, true, header);
		} else {
			return postAsync(httpUrl, buildPostString(params), outputStream);
		}
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
	 * @param outputStream 如果提供，则post内容将输出到该输出流，输出完之后自动close掉
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
	public HttpResponse post(String httpUrl, InputStream inputStream) throws IOException {
		return post(httpUrl, inputStream, null);
	}
	
	/**
	 * post方式请求HTTP
	 * @param httpUrl
	 * @param outputStream 如果指定了输出流，则输出到指定的输出流，此时返回的值没有html正文bytes; outputStream会自动close掉
	 * @return
	 * @throws IOException
	 */
	public HttpResponse post(String httpUrl, InputStream inputStream, OutputStream outputStream)
			throws IOException {
		return _post(httpUrl, inputStream, outputStream, false, null);
	}
	
	/**
	 * post方式请求HTTP
	 * @param httpUrl
	 * @param outputStream 如果提供，则post内容将输出到该输出流，输出完之后自动close掉
	 * @return
	 * @throws IOException
	 */
	public HttpResponse postAsync(String httpUrl, byte[] postData, OutputStream outputStream) throws IOException {
		return postAsync(httpUrl, new ByteArrayInputStream(postData), outputStream);
	}
	
	/**
	 * post方式请求HTTP，异步方式
	 * @param httpUrl
	 * @param inputStream
	 * @param outputStream 如果指定了输出流，则输出到指定的输出流，此时返回的值没有html正文bytes; outputStream会自动close掉
	 * @return
	 * @throws IOException
	 */
	public HttpResponse postAsync(String httpUrl, InputStream inputStream, OutputStream outputStream)
	        throws IOException {
		return _post(httpUrl, inputStream, outputStream, true, null);
	}
	
	private HttpResponse _post(String httpUrl, InputStream inputStream, OutputStream outputStream,
			boolean isAsync, Map<String, String> requestHeader) throws IOException {
		IOException ie = null;
		for(int i = 0; i < retryTimes; i++) {
			try {
				HttpURLConnection urlConnection = getUrlConnection(httpUrl, "POST");
				if(requestHeader != null) {
					for(Entry<String, String> entry : requestHeader.entrySet()) {
						urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
					}
				}
				
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
				LOGGER.error("post url:{} exception", httpUrl, e);
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
	 * @param outputStream 如果提供，则post内容将输出到该输出流，输出完之后自动close掉
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
	 * @param outputStream 如果提供，则post内容将输出到该输出流，输出完之后自动close掉
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
	 * @param outputStream 如果提供，则post内容将输出到该输出流，输出完之后自动close掉
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
	 * @param outputStream 如果提供，则post内容将输出到该输出流，输出完之后自动close掉
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
	 * @param outputStream 如果提供，则post内容将输出到该输出流，输出完之后自动close掉
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
							return get(location.get(0), params, outputStream, isAsync);
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
		urlConnection.setConnectTimeout(connectTimeoutSeconds * 1000);
		urlConnection.setReadTimeout(readTimeoutSeconds * 1000);
		urlConnection.setRequestMethod(method);
		urlConnection.setRequestProperty("User-agent", USER_AGENT);
		urlConnection.setRequestProperty("Referer", httpUrl);
		
		// 设置cookie
		String host = NetUtils.getUrlHostname(httpUrl);
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
		
		// 设置用户自定义RequestProperty
		for(Entry<String, String> entry : requestProperty.entrySet()) {
			urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
		}
		
		return urlConnection;
	}
	
	/**
	 * 构造httpResponse
	 * @param urlConnection
	 * @param outputStream 如果提供，则post内容将输出到该输出流，输出完之后自动close掉
	 * @param isAsync 是否异步，只有当outputStream!=null时，该值才有效。
	 *        当isAsync为true时，HttpResponse可以获得已下载的字节数。
	 * @throws IOException 
	 */
	private HttpResponse makeHttpResponse(String httpUrl, HttpURLConnection urlConnection,
			final OutputStream outputStream, boolean isAsync) throws IOException {
		HttpResponse httpResponse = new HttpResponse();
		httpResponse.setCharset(charset);
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
						} finally {
							try {
								outputStream.close();
							} catch (IOException e) {
								LOGGER.error("outputStrema close error", e);
							}
						}
					}
				}).start();
			} else {
				try {
					while((len = in.read(buf)) != -1) {
						outputStream.write(buf, 0, len);
					}
				} finally {
					try {
						outputStream.close();
					} catch (IOException e) {
						LOGGER.error("outputStrema close error", e);
					}
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
	
	/**multipart/form-data编码方式
	 * @throws IOException */
	private byte[] buildPostString(Map<String, Object> params, String boundary)
			throws IOException {
		if(params == null || params.isEmpty()) {
			return new byte[0];
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		for(Entry<String, Object> entry : params.entrySet()) {
			baos.write(toBytes("--" + boundary + "\r\n"));
			String str = "Content-Disposition: form-data; name=\"" + 
				 urlEncode(entry.getKey()) + "\"";
			baos.write(toBytes(str));
			if(entry.getValue() instanceof BrowserPostFile) {
				BrowserPostFile file = (BrowserPostFile) entry.getValue();
				String str1 = "; filename=\"" + 
					urlEncode(file.getFilename()) + "\"\r\n";
				baos.write(toBytes(str1));
				String contentType = "Content-Type: " + file.getContentType();
				baos.write(toBytes(contentType));
			}
			baos.write(toBytes("\r\n\r\n"));
			if(entry.getValue() instanceof BrowserPostFile) {
				BrowserPostFile file = (BrowserPostFile) entry.getValue();
				if(file.getBytes() != null) {
					baos.write(file.getBytes());
				} else {
					byte[] buff = new byte[4096];
					int len = 0;
					while((len = file.getIn().read(buff)) != -1) {
						baos.write(buff, 0, len);
					}
					try {
						if(file.getIn() != null) {
							file.getIn().close();
						}
					} catch (Exception e) {
					}
				}
			} else {
				Object value = entry.getValue();
				if(value == null) {value = "";}
				baos.write(toBytes(value.toString()));
			}
			baos.write(toBytes("\r\n"));
		}
		
		if(!params.isEmpty()) {
			baos.write(toBytes(("--" + boundary + "--")));
		}
		
		return baos.toByteArray();
	}

	/**转换成json格式*/
	private byte[] buildPostJson(Object obj) {
	    if(obj == null) {
	        return new byte[0];
        }
        return toBytes(JSON.toJson(obj));
    }
	
	/**application/x-www-form-urlencoded编码方式*/
	private byte[] buildPostString(Map<String, Object> params) {
		if(params == null || params.isEmpty()) {
			return new byte[0];
		}
		StringBuilder sb = new StringBuilder();
		boolean needAppendAnd = false;
		for(Entry<String, Object> entry : params.entrySet()) {
			if(needAppendAnd) {
				sb.append("&");
			}
			sb.append(urlEncode(entry.getKey()));
			sb.append("=");
			if(entry.getValue() != null) {
				sb.append(urlEncode(entry.getValue().toString()));
			}
			needAppendAnd = true;
		}

		return toBytes(sb.toString());
	}
	
	/**
	 * 将请求参数加到httpUrl后面
	 * @param httpUrl
	 * @param params
	 * @return
	 */
	private String appendParamToUrl(String httpUrl, Map<String, Object> params) {
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
			if(needAppendAnd) {
				sb.append("&");
			}
			sb.append(urlEncode(entry.getKey()));
			sb.append("=");
			if(entry.getValue() != null) {
				sb.append(urlEncode(entry.getValue().toString()));
			}
			needAppendAnd = true;
		}
		
		return sb.toString();
	}

	private byte[] toBytes(String str) {
		if(str == null) {
			return new byte[0];
		}
		if(charset == null) {
			return str.getBytes();
		} else {
			try {
				return str.getBytes(charset);
			} catch (UnsupportedEncodingException e) {
				LOGGER.error("transform string:{} to byte[] fail, charset:{}",
						str, charset, e);
				return new byte[0];
			}
		}
	}

	private String urlEncode(String str) {
		if(str == null) {
			return "";
		}
		if(charset == null) {
			try {
				return URLEncoder.encode(str, "utf8");
			} catch (UnsupportedEncodingException e) {
				LOGGER.error("url encode string:{} fail, charset:utf8", str, e);
				return "";
			}
		} else {
			try {
				return URLEncoder.encode(str, charset);
			} catch (UnsupportedEncodingException e) {
				LOGGER.error("url encode string:{} fail, charset:{}", str, charset, e);
				return "";
			}
		}
	}
	
}
