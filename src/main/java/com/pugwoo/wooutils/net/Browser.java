package com.pugwoo.wooutils.net;

import com.pugwoo.wooutils.io.IOUtils;
import com.pugwoo.wooutils.json.JSON;
import com.pugwoo.wooutils.thread.ThreadPoolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.*;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.zip.GZIPInputStream;

/**
 * 2016年2月4日 15:16:42 
 * 模拟一个浏览器发HTTP请求，不包括页面处理。默认编码是utf8，可以全局指定编码
 * <br>
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

	/** mock 浏览器userAgent: Chrome Win10*/
	public static final String WIN_CHROME_AGENT =
			"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Browser.class);

	private static final ThreadPoolExecutor asyncDownloadThreadPool =
			ThreadPoolUtils.createThreadPool(10, 100, 20, "asyncDownloadThreadPool"); // 异步下载共用线程池

	public static class HttpResponseFuture {
		/**已经下载的字节数*/
		public long downloadedBytes;
		/**是否已经下载完成*/
		public boolean isFinished;
        /**是否发生了异常，当值为true时，表示已经发生了异常并结束了请求*/
		public boolean isException;
	}
		
	/**cookie, domain(域根目录) -> key/value*/
	private final Map<String, Map<String, String>> cookies = new HashMap<>();
	
	/**全局的请求时的头部*/
	private final Map<String, String> requestProperty = new HashMap<>();

	private String USER_AGENT = "java";

	public void setUserAgent(String userAgent) {
		this.USER_AGENT = userAgent;
	}

	private String charset = "utf8";

	/**是否开启自动跳转，jdk默认也是开启的*/
	private boolean enableRedirect = true;

    /**支持禁止gzip压缩*/
	private boolean disableGzip = false;

	/**是否禁用ChunkedStreamingMode模式*/
	private boolean disableChunkedStreamingMode = false;

    /**禁用跳转，转为手工处理模式*/
	public void disableRedirect() {
		this.enableRedirect = false;
	}

	/**开启跳转，默认是开启的*/
	public void enableRedirect() {
		this.enableRedirect = true;
	}

    /**禁用gzip*/
	public void disableGzip() {
		this.disableGzip = true;
	}

	/**启用gzip*/
	public void enableGzip() {
		this.disableGzip = false;
	}

	/**禁用ChunkedStreamingMode，默认是启用状态*/
	public void disableChunkedStreamingMode() {
		this.disableChunkedStreamingMode = true;
	}

	/**启用ChunkedStreamingMode，默认是启用状态*/
	public void enableChunkedStreamingMode() {
		this.disableChunkedStreamingMode = false;
	}

	/**
	 * 设置整个Browser实例全局的字符编码，默认utf8
	 * @param charset 字符编码，默认utf8
	 */
	public void setCharset(String charset) {
		this.charset = charset;
	}
	
	/**连接超时时间，秒*/
	private int connectTimeoutSeconds = 10;
    /**读取超时时间，秒*/
	private int readTimeoutSeconds = 60;

	/**GET默认重试1次*/
	private int getRetryTimes = 1;
	/**POST默认不重试*/
	private int postRetryTimes = 0;
	/**重试的间隔时间，毫秒*/
	private int retryIntervalMs = 0;

	/**设置连接超时时间，默认10秒*/
	public void setConnectTimeoutSeconds(int connectTimeoutSeconds) {
		this.connectTimeoutSeconds = connectTimeoutSeconds;
	}

    /**设置读取超时时间，默认60秒*/
	public void setReadTimeoutSeconds(int readTimeoutSeconds) {
		this.readTimeoutSeconds = readTimeoutSeconds;
	}

	/**设置重试次数，GET和POST都设置，设置为0表示不重试*/
	public void setRetryTimes(int retryTimes) {
		this.getRetryTimes = retryTimes;
		this.postRetryTimes = retryTimes;
	}

    /**设置GET的重试次数，默认1次*/
	public void setGetRetryTimes(int getRetryTimes) {
		this.getRetryTimes = getRetryTimes;
	}

	/**设置POST的重试次数，默认不重试，即为0次*/
	public void setPostRetryTimes(int postRetryTimes) {
		this.postRetryTimes = postRetryTimes;
	}

	/**设置重试的间隔时间，毫秒，0表示不间隔*/
	public void setRetryIntervalMs(int retryIntervalMs) {
		this.retryIntervalMs = retryIntervalMs;
	}

	/**代理*/
	private Proxy proxy = null;
	
	/**信任所有ssl证书*/
	private final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
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

	public void delRequestHeader(String key) {
		requestProperty.remove(key);
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

	/**设置请求时的头部，该设置是Browser实例全局的。<br>
	 * 设置HttpServletRequest的所有头部信息
	 * @param request HttpServletRequest
	 */
	public void addRequestHeaderForJakarta(jakarta.servlet.http.HttpServletRequest request) {
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
		cookies.computeIfAbsent(domain, k -> new HashMap<>());
		cookies.get(domain).put(key, value);
	}
	
	/**
	 * 设置http代理
	 *
	 * @param ip http代理ip
	 * @param port http代理端口
	 * @return 原browser对象，便于链式写法
	 */
	public Browser setHttpProxy(String ip, int port) {
		proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
		return this;
	}

	// ======================== POST BEGIN ===========================================
	
	/**
	 * post方式请求HTTP，params使用queryString或formdata方式组装
	 *
	 * @param httpUrl 请求的url地址，可以带queryString参数（此处的queryString参数【不会】被编解码处理）
	 * @param params post的参数，以queryString或formdata形式（取决于是否上传文件）编码到post body中（params中的参数会被编码处理）
	 * @return 请求返回数据，请注意通过http状态码判断请求是否成功
	 * @throws IOException 当请求处理网络异常时抛出IOException
	 */
	public HttpResponse post(String httpUrl, Map<String, Object> params) throws IOException {
		return post(httpUrl, params, null);
	}

	/**判断post的参数中是否有文件上传*/
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
	 *
	 * @param httpUrl 请求的url地址，可以带queryString参数（此处的queryString参数【不会】被编解码处理）
	 * @param params post的参数，以queryString或formdata形式（取决于是否上传文件）编码到post body中（params中的参数会被编码处理）
	 * @param outputStream 如果提供，则post内容将输出到该输出流，输出完之后自动close掉
	 * @return 请求返回数据，请注意通过http状态码判断请求是否成功
	 * @throws IOException 当请求处理网络异常时抛出IOException
	 */
	public HttpResponse post(String httpUrl, Map<String, Object> params, OutputStream outputStream) throws IOException {
		if(isWithBrowserPostFile(params)) {
			String boundary = "----WebKitFormBoundaryYp0ZBDEHwALiqVW5";
			Map<String, String> header = new HashMap<>();
			header.put("Content-Type", "multipart/form-data; boundary=" + boundary);
			return _post(httpUrl, buildPostString(params, boundary),
					outputStream, false, header);
		} else {
			Map<String, String> header = new HashMap<>();
			header.put("Content-Type", "application/x-www-form-urlencoded");
			return _post(httpUrl, new ByteArrayInputStream(buildPostString(params)),
					outputStream, false, header);
		}
	}

    /**
     * post方式请求HTTP，转换成json形式提交
	 *
     * @param httpUrl 请求的url地址，可以带queryString参数（此处的queryString参数【不会】被编解码处理）
     * @param paramObject 请求的数据对象，会被转换为json字符串
     * @return 请求返回数据，请注意通过http状态码判断请求是否成功
     */
    public HttpResponse postJson(String httpUrl, Object paramObject) throws IOException {
		Map<String, String> header = new HashMap<>();
		header.put("Content-Type", "application/json");
		return _post(httpUrl, new ByteArrayInputStream(buildPostJson(paramObject)),
				null, false, header);
    }

    /**
     * post方式请求HTTP，转换成json形式提交
	 *
     * @param httpUrl 请求的url地址，可以带queryString参数（此处的queryString参数【不会】被编解码处理）
     * @param paramObject 请求的数据对象，会被转换为json字符串
     * @param outputStream 如果提供，则post内容将输出到该输出流，输出完之后自动close掉
     * @return 请求返回数据，请注意通过http状态码判断请求是否成功
     */
	public HttpResponse postJson(String httpUrl, Object paramObject, OutputStream outputStream) throws IOException {
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");
        return _post(httpUrl, new ByteArrayInputStream(buildPostJson(paramObject)),
                outputStream, false, header);
    }

    /**
     * 异步post方式请求HTTP，转换成json形式提交
     * @param httpUrl 请求的url地址，可以带queryString参数（此处的queryString参数【不会】被编解码处理）
     * @param toJson 请求的数据对象，会被转换为json字符串
     * @param outputStream 如果提供，则post内容将输出到该输出流，输出完之后自动close掉
     * @return 请求返回数据对象，异步的，支持获取当前请求状态，请注意通过http状态码判断请求是否成功
     */
    public HttpResponse postJsonAsync(String httpUrl, Object toJson, OutputStream outputStream) throws IOException {
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");
        return _post(httpUrl, new ByteArrayInputStream(buildPostJson(toJson)),
                outputStream, true, header);
    }
	
	/**
	 * post方式请求HTTP，params使用queryString或formdata方式组装
	 * @param httpUrl 请求的url地址，可以带queryString参数（此处的queryString参数【不会】被编解码处理）
	 * @param params post的参数，以queryString或formdata形式（取决于是否上传文件）编码到post body中（params中的参数会被编码处理）
	 * @param outputStream 如果提供，则post内容将输出到该输出流，输出完之后自动close掉
	 * @return 请求返回数据对象，异步的，支持获取当前请求状态，请注意通过http状态码判断请求是否成功
	 */
	public HttpResponse postAsync(String httpUrl, Map<String, Object> params, OutputStream outputStream) throws IOException {
		if(isWithBrowserPostFile(params)) {
			String boundary = "----WebKitFormBoundaryYp0ZBDEHwALiqVW5";
			Map<String, String> header = new HashMap<>();
			header.put("Content-Type", "multipart/form-data; boundary=" + boundary);
			return _post(httpUrl, buildPostString(params, boundary),
					outputStream, true, header);
		} else {
			Map<String, String> header = new HashMap<>();
			header.put("Content-Type", "application/x-www-form-urlencoded");
			return _post(httpUrl, new ByteArrayInputStream(buildPostString(params)),
					outputStream, false, header);
		}
	}
	
	/**
	 * post方式请求HTTP
	 * @param httpUrl 请求的url地址，可以带queryString参数（此处的queryString参数【不会】被编解码处理）
	 * @param postData post的二进制数据，该数据不会作任何处理
	 * @return 请求返回数据，请注意通过http状态码判断请求是否成功
	 */
	public HttpResponse post(String httpUrl, byte[] postData) throws IOException {
		Map<String, String> header = new HashMap<>();
		if(!requestProperty.containsKey("Content-Type")) {
			header.put("Content-Type", "text/plain");
		}
		return _post(httpUrl, new ByteArrayInputStream(postData), null, false, header);
	}
	
	/**
	 * post方式请求HTTP
	 * @param httpUrl 请求的url地址，可以带queryString参数（此处的queryString参数【不会】被编解码处理）
	 * @param postData post的二进制数据，该数据不会作任何处理
	 * @param outputStream 如果提供，则post内容将输出到该输出流，输出完之后自动close掉
	 * @return 请求返回数据，请注意通过http状态码判断请求是否成功
	 */
	public HttpResponse post(String httpUrl, byte[] postData, OutputStream outputStream) throws IOException {
		Map<String, String> header = new HashMap<>();
		if(!requestProperty.containsKey("Content-Type")) {
			header.put("Content-Type", "text/plain");
		}
		return _post(httpUrl, new ByteArrayInputStream(postData), outputStream, false, header);
	}
	
	/**
	 * post方式请求HTTP
	 * @param httpUrl 请求的url地址，可以带queryString参数（此处的queryString参数【不会】被编解码处理）
	 * @param inputStream post的二进制数据，以inputStream的形式提供，请自行设置Content-Type，默认为text/plain
	 * @return 请求返回数据，请注意通过http状态码判断请求是否成功
	 */
	public HttpResponse post(String httpUrl, InputStream inputStream) throws IOException {
		Map<String, String> header = new HashMap<>();
		if(!requestProperty.containsKey("Content-Type")) {
			header.put("Content-Type", "text/plain");
		}
		return _post(httpUrl, inputStream, null, false, header);
	}
	
	/**
	 * post方式请求HTTP
	 * @param httpUrl 请求的url地址，可以带queryString参数（此处的queryString参数【不会】被编解码处理）
	 * @param inputStream post的二进制数据，以inputStream的形式提供
	 * @param outputStream 如果指定了输出流，则输出到指定的输出流，此时返回的值没有html正文bytes; outputStream会自动close掉
	 * @return 请求返回数据，请注意通过http状态码判断请求是否成功
	 */
	public HttpResponse post(String httpUrl, InputStream inputStream, OutputStream outputStream)
			throws IOException {
		Map<String, String> header = new HashMap<>();
		if(!requestProperty.containsKey("Content-Type")) {
			header.put("Content-Type", "text/plain");
		}
		return _post(httpUrl, inputStream, outputStream, false, header);
	}
	
	/**
	 * post方式请求HTTP
	 * @param httpUrl 请求的url地址，可以带queryString参数（此处的queryString参数【不会】被编解码处理）
	 * @param postData post的二进制数据，该数据不会作任何处理
	 * @param outputStream 如果提供，则post内容将输出到该输出流，输出完之后自动close掉，请注意通过http状态码判断请求是否成功
	 */
	public HttpResponse postAsync(String httpUrl, byte[] postData, OutputStream outputStream) throws IOException {
		Map<String, String> header = new HashMap<>();
		if(!requestProperty.containsKey("Content-Type")) {
			header.put("Content-Type", "text/plain");
		}
		return _post(httpUrl, new ByteArrayInputStream(postData), outputStream, true, header);
	}
	
	/**
	 * post方式请求HTTP，异步方式
	 * @param httpUrl 请求的url，可以带queryString参数（此处的queryString参数【不会】被编解码处理）
	 * @param inputStream post的二进制数据，以inputStream的形式提供
	 * @param outputStream 如果指定了输出流，则输出到指定的输出流，此时返回的值没有html正文bytes; outputStream会自动close掉
	 * @return HttpResponse 请注意通过http状态码判断请求是否成功
	 * @throws IOException IOException
	 */
	public HttpResponse postAsync(String httpUrl, InputStream inputStream, OutputStream outputStream)
	        throws IOException {
		Map<String, String> header = new HashMap<>();
		if(!requestProperty.containsKey("Content-Type")) {
			header.put("Content-Type", "text/plain");
		}
		return _post(httpUrl, inputStream, outputStream, true, header);
	}
	
	private HttpResponse _post(String httpUrl, InputStream inputStream, OutputStream outputStream,
			boolean isAsync, Map<String, String> requestHeader) throws IOException {
		IOException ie = null;
		for(int i = -1; i < postRetryTimes; i++) { // 0表示不重试，即只请求1次

			if (i >= 0 && inputStream != null) {
				if (inputStream instanceof FileInputStream) {
					FileChannel fileChannel = ((FileInputStream) inputStream).getChannel();
					fileChannel.position(0);
				} else if (inputStream.markSupported()) {
					inputStream.reset();
				} else {
					LOGGER.error("inputStream is not support reset, do not support retry");
					throw ie;
				}
			}

			if (i >= 0 && outputStream != null) {
				if (outputStream instanceof FileOutputStream) {
					FileChannel channel = ((FileOutputStream) outputStream).getChannel();
					channel.position(0);
					channel.truncate(0);
				} else {
					LOGGER.error("outputStream is not FileOutputStream, do not support retry");
					throw ie;
				}
			}

			HttpURLConnection urlConnection = null;
			try {
				urlConnection = getUrlConnection(httpUrl, "POST");
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

					IOUtils.close(os);
				}

				HttpResponse resp = makeHttpResponse(urlConnection, outputStream, isAsync);
				if (resp.getResponseCode() >= 500 && i < postRetryTimes - 1) {
					continue; // 状态码500及以上，有机会重试的情况下，重试
				}
				return resp;
			} catch (IOException e) {
				LOGGER.error("post url:{} exception msg:{}", httpUrl, e.getMessage());
				ie = e;
				if (retryIntervalMs > 0) {
					try {
						Thread.sleep(retryIntervalMs);
					} catch (InterruptedException ex) {
						// ignore
					}
				}
			} finally {
				if(!isAsync) { // 只有同步才关闭
					try {
						if (urlConnection != null){
							urlConnection.disconnect();
						}
					} catch (Exception e) {
						LOGGER.error("disconnect urlConnection fail", e);
					}
				}
			}
		}
		IOUtils.close(inputStream);
		throw ie;
	}
	
	// ================================= GET BEGIN ================================
	
	/**
	 * get方式请求HTTP,处理细节：<br>
	 * 1. 如果是301或302跳转，且跳转链接不同于当前链接，则会再请求跳转的URL
	 * 
	 * @param httpUrl 请求的url，可以带queryString参数（此处的queryString参数【不会】被编解码处理）
	 * @return 请求返回的数据，请注意通过http状态码判断请求是否成功
	 */
	public HttpResponse get(String httpUrl) throws IOException {
		return get(httpUrl, null, null);
	}

	/**
	 * get方式请求HTTP,处理细节：<br>
	 * 1. 如果是301或302跳转，且跳转链接不同于当前链接，则再请求跳转的URL
	 * 
	 * @param httpUrl 请求的url，可以带queryString参数（此处的queryString参数【不会】被编解码处理）
	 * @param outputStream 如果提供，则post内容将输出到该输出流，输出完之后自动close掉
	 * @return 请求返回的数据，请注意通过http状态码判断请求是否成功
	 */
	public HttpResponse get(String httpUrl, OutputStream outputStream) throws IOException {
		return get(httpUrl, null, outputStream);
	}
	
	/**
	 * get方式请求HTTP,处理细节：<br>
	 * 1. 如果是301或302跳转，且跳转链接不同于当前链接，则再请求跳转的URL
	 * 
	 * @param httpUrl 请求的url，可以带queryString参数（此处的queryString参数【不会】被编解码处理）
	 * @param outputStream 如果提供，则post内容将输出到该输出流，输出完之后自动close掉
	 * @return 请求返回的数据，请注意通过http状态码判断请求是否成功
	 */
	public HttpResponse getAsync(String httpUrl, OutputStream outputStream) throws IOException {
		return getAsync(httpUrl, null, outputStream);
	}
	
	/**
	 * get方式请求HTTP,处理细节：<br>
	 * 1. 如果是301或302跳转，且跳转链接不同于当前链接，则再请求跳转的URL
	 * 
	 * @param httpUrl 请求的url，可以带queryString参数（此处的queryString参数【不会】被编解码处理）
	 * @param params get请求的参数，以queryString编码到url中（params中的参数会被编码处理）
	 * @return 请求返回的数据，请注意通过http状态码判断请求是否成功
	 */
	public HttpResponse get(String httpUrl, Map<String, Object> params) throws IOException {
		return _get(httpUrl, params, null, false);
	}
	
	/**
	 * get方式请求HTTP,处理细节：<br>
	 * 1. 如果是301或302跳转，且跳转链接不同于当前链接，则再请求跳转的URL
	 * 
	 * @param httpUrl 请求的url，可以带queryString参数（此处的queryString参数【不会】被编解码处理）
	 * @param params get请求的参数，以queryString编码到url中（params中的参数会被编码处理）
	 * @param outputStream 如果提供，则post内容将输出到该输出流，输出完之后自动close掉
	 * @return 请求返回的数据，请注意通过http状态码判断请求是否成功
	 */
	public HttpResponse get(String httpUrl, Map<String, Object> params, OutputStream outputStream)
	        throws IOException {
		return _get(httpUrl, params, outputStream, false);
	}
	
	/**
	 * get方式请求HTTP,处理细节：<br>
	 * 1. 如果是301或302跳转，且跳转链接不同于当前链接，则再请求跳转的URL
	 * 
	 * @param httpUrl 请求的url，可以带queryString参数（此处的queryString参数【不会】被编解码处理）
	 * @param params get请求的参数，以queryString编码到url中（params中的参数会被编码处理）
	 * @param outputStream 如果提供，则post内容将输出到该输出流，输出完之后自动close掉
	 * @return 请求返回的数据，请注意通过http状态码判断请求是否成功
	 */
	public HttpResponse getAsync(String httpUrl, Map<String, Object> params, OutputStream outputStream) 
	        throws IOException {
		return _get(httpUrl, params, outputStream, true);
	}
	
	/**
	 * get方式请求HTTP,处理细节：<br>
	 * 1. 如果是301或302跳转，且跳转链接不同于当前链接，则再请求跳转的URL
	 */
	private HttpResponse _get(String httpUrl, Map<String, Object> params, OutputStream outputStream,
							  boolean isAsync) throws IOException {
		httpUrl = appendParamToUrl(httpUrl, params);
		IOException ie = null;
		for(int i = -1; i < getRetryTimes; i++) { // 0表示不重试，即只请求1次
			// 如果是重试，那么需要特别处理一下输出流，因为上一次的失败可能已经写入了部分数据，这部分数据是属于上一次的，应该清空
			if (i >= 0 && outputStream != null) {
				if (outputStream instanceof FileOutputStream) {
					FileChannel channel = ((FileOutputStream) outputStream).getChannel();
					channel.position(0);
					channel.truncate(0);
				} else {
					LOGGER.error("outputStream is not FileOutputStream, do not support retry");
					throw ie;
				}
			}

			HttpURLConnection urlConnection = null;
			try {
				urlConnection = getUrlConnection(httpUrl, "GET");

				if(enableRedirect) {
					// 301 302 跳转处理
					int responseCode = urlConnection.getResponseCode();
					if(responseCode == HttpURLConnection.HTTP_MOVED_PERM
							|| responseCode == HttpURLConnection.HTTP_MOVED_TEMP
							|| responseCode == HttpURLConnection.HTTP_SEE_OTHER) {
						List<String> location = urlConnection.getHeaderFields().get("Location");
						if(location != null && !location.isEmpty()) {
							if(!httpUrl.equals(location.get(0))) { // 避免死循环
								// 跳转之前处理cookie
								handleCookies(urlConnection.getHeaderFields());
								return _get(location.get(0), params, outputStream, isAsync);
							}
						}
					}
				}

				HttpResponse resp = makeHttpResponse(urlConnection, outputStream, isAsync);
				if (resp.getResponseCode() >= 500 && i < getRetryTimes - 1) {
					continue; // 状态码500及以上，有机会重试的情况下，重试
				}
				return resp;
			} catch (IOException e) {
				LOGGER.error("get url:{} error msg:{}", httpUrl, e.getMessage());
				ie = e;
				if (retryIntervalMs > 0) {
					try {
						Thread.sleep(retryIntervalMs);
					} catch (InterruptedException ex) {
						// ignore
					}
				}
			} finally {
				if(!isAsync) { // 只有同步才关闭
					try {
						if (urlConnection != null){
							urlConnection.disconnect();
						}
					} catch (Exception e) {
						LOGGER.error("disconnect urlConnection fail", e);
					}
				}
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

		if ("POST".equals(method) && !disableChunkedStreamingMode) {
			urlConnection.setChunkedStreamingMode(8192);
		}

		urlConnection.setConnectTimeout(connectTimeoutSeconds * 1000);
		urlConnection.setReadTimeout(readTimeoutSeconds * 1000);
		urlConnection.setRequestMethod(method);
		urlConnection.setRequestProperty("User-agent", USER_AGENT);
		if (!disableGzip) {
			urlConnection.setRequestProperty("Accept-Encoding", "gzip"); // support gzip
		}

		if(!enableRedirect) {
			urlConnection.setInstanceFollowRedirects(false);
		}

		// 设置cookie
		if(!cookies.isEmpty()) {
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
		}

		// 设置用户自定义RequestProperty
		for(Entry<String, String> entry : requestProperty.entrySet()) {
			urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
		}
		
		return urlConnection;
	}

	/**处理cookie*/
	private void handleCookies(Map<String, List<String>> headerFields) {
		List<String> setCookies = headerFields.get("Set-Cookie");
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
	}
	
	/**
	 * 构造httpResponse
	 * @param urlConnection
	 * @param outputStream 如果提供，则post内容将输出到该输出流，输出完之后自动close掉
	 * @param isAsync 是否异步，只有当outputStream!=null时，该值才有效。
	 *        当isAsync为true时，HttpResponse可以获得已下载的字节数。
	 * @throws IOException 
	 */
	private HttpResponse makeHttpResponse(HttpURLConnection urlConnection,
			final OutputStream outputStream, boolean isAsync) throws IOException {
		HttpResponse httpResponse = new HttpResponse();
		httpResponse.setCharset(charset);
		httpResponse.setResponseCode(urlConnection.getResponseCode());

		Map<String, List<String>> headerFields = urlConnection.getHeaderFields();
		httpResponse.setHeaders(headerFields);
		
		handleCookies(headerFields);

		boolean isGzip = false;
		if (!disableGzip) {
			List<String> contentEncoding = headerFields.get("Content-Encoding");
			isGzip = contentEncoding != null && !contentEncoding.isEmpty()
					&& "gzip".equals(contentEncoding.get(0));
		}

		InputStream connectionIn;
		if (httpResponse.getResponseCode() >= 400) {
			connectionIn = urlConnection.getErrorStream();
		} else {
			connectionIn = urlConnection.getInputStream();
		}
		
		final InputStream in = connectionIn == null ? null : (isGzip ? new GZIPInputStream(connectionIn) : connectionIn);
		byte[] buf = new byte[4096];
		int len;
		if(outputStream != null) {
			if(isAsync) {
				final HttpResponseFuture future = new HttpResponseFuture();
				httpResponse.setFuture(future);

				asyncDownloadThreadPool.submit(() -> {
                    byte[] buf1 = new byte[4096];
                    int len1;
                    try {
						if (in != null) {
							while((len1 = in.read(buf1)) != -1) {
								future.downloadedBytes += len1;
								outputStream.write(buf1, 0, len1);
							}
						}
                        future.isFinished = true;
                    } catch (IOException e) {
						future.isException = true;
                        LOGGER.error("outputStream write error", e);
                    } finally {
                        IOUtils.close(outputStream);
                        IOUtils.close(in);
                        try {
                            urlConnection.disconnect();
                        } catch (Exception e) {
                            LOGGER.error("disconnect urlConnection fail", e);
                        }
                    }
                });

			} else {
				try {
					if (in != null) {
						while((len = in.read(buf)) != -1) {
							outputStream.write(buf, 0, len);
						}
					}
				} finally {
					IOUtils.close(outputStream);
					IOUtils.close(in);
				}
			}
		} else {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				if (in != null) {
					while((len = in.read(buf)) != -1) {
						baos.write(buf, 0, len);
					}
				}
				httpResponse.setContentBytes(baos.toByteArray());
			} finally {
				IOUtils.close(in);
			}
		}
		
		return httpResponse;
	}
	
	/**multipart/form-data编码方式
	 * @throws IOException */
	private InputStream buildPostString(Map<String, Object> params, String boundary)
			throws IOException {
		if(params == null || params.isEmpty()) {
			return new ByteArrayInputStream(new byte[0]);
		}

		List<InputStream> inputStreams = new ArrayList<>();

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
				} else if (file.getIn() != null) {
					inputStreams.add(new ByteArrayInputStream(baos.toByteArray()));
					baos.reset();
					inputStreams.add(file.getIn());
				} else {
					baos.write(new byte[0]);
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

		inputStreams.add(new ByteArrayInputStream(baos.toByteArray()));
		return new SequenceInputStream(Collections.enumeration(inputStreams));
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
