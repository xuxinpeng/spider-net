package com.boundary.aaron.spider.recruit.service.crawler;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import lombok.extern.slf4j.Slf4j;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.AbstractDownloader;
import us.codecraft.webmagic.downloader.HttpClientGenerator;
import us.codecraft.webmagic.selector.PlainText;
import us.codecraft.webmagic.utils.HttpConstant;
import us.codecraft.webmagic.utils.UrlUtils;

@Slf4j
public class LagouClientDownLoader extends AbstractDownloader {

	private static final HttpClientGenerator httpClientGenerator = new HttpClientGenerator();
	private final Map<String, HttpClient> httpClients = new ConcurrentHashMap<String, HttpClient>();

	private HttpClient getHttpClient(Site site) {
		if (site == null) {
			return httpClientGenerator.getClient(null);
		}
		HttpClient httpClient = httpClients.get(site.getDomain());
		if (httpClient == null) {
			synchronized (this) {
				httpClient = httpClients.get(site.getDomain());
				if (httpClient == null) {
					httpClient = httpClientGenerator.getClient(site);
					httpClients.put(site.getDomain(), httpClient);
				}
			}
		}
		return httpClient;
	}

	@Override
	public Page download(Request request, Task task) {
		log.info("page download start..");
		HttpResponse httpResponse = null;
		try {
			Site site = task.getSite();
			HttpUriRequest httpUriRequest = getHttpUriRequest(request, site, site.getHeaders());
			httpResponse = getHttpClient(site).execute(httpUriRequest);
			if (HttpStatus.SC_OK == httpResponse.getStatusLine().getStatusCode()) {
				Page page = handleResponse(request, site.getCharset(), httpResponse, task);
				onSuccess(request);
				return page;
			}
			return null;
		} catch (IOException e) {
			onError(request);
			return null;
		} finally {
			try {
				if (httpResponse != null) {
					EntityUtils.consume(httpResponse.getEntity());
				}
			} catch (IOException e) {
				log.info("page download exception :{} ", e);
			}
		}

	}

	protected Page handleResponse(Request request, String charset, HttpResponse httpResponse,
			Task task) throws IOException {
		String content = getContent(charset, httpResponse);
		Page page = new Page();
		page.setRawText(content);
		page.setUrl(new PlainText(request.getUrl()));
		page.setRequest(request);
		page.setStatusCode(httpResponse.getStatusLine().getStatusCode());
		return page;
	}

	protected String getContent(String charset, HttpResponse httpResponse) throws IOException {
		if (charset != null) {
			return IOUtils.toString(httpResponse.getEntity().getContent(), charset);
		}
		byte[] contentBytes = IOUtils.toByteArray(httpResponse.getEntity().getContent());
		String htmlCharset = getHtmlCharset(httpResponse, contentBytes);
		if (htmlCharset != null) {
			return new String(contentBytes, htmlCharset);
		}
		return new String(contentBytes);
	}

	protected String getHtmlCharset(HttpResponse httpResponse, byte[] contentBytes)
			throws IOException {
		String charset;
		String value = httpResponse.getEntity().getContentType().getValue();
		charset = UrlUtils.getCharset(value);
		if (StringUtils.isNotBlank(charset)) {
			return charset;
		}
		Charset defaultCharset = Charset.defaultCharset();
		String content = new String(contentBytes, defaultCharset.name());
		if (StringUtils.isNotEmpty(content)) {
			Document document = Jsoup.parse(content);
			Elements links = document.select("meta");
			for (Element link : links) {
				String metaContent = link.attr("content");
				String metaCharset = link.attr("charset");
				if (metaContent.indexOf("charset") != -1) {
					metaContent = metaContent.substring(metaContent.indexOf("charset"),
							metaContent.length());
					charset = metaContent.split("=")[1];
					break;
				} else if (StringUtils.isNotEmpty(metaCharset)) {
					charset = metaCharset;
					break;
				}
			}
		}
		return charset;
	}

	private HttpUriRequest getHttpUriRequest(Request request, Site site,
			Map<String, String> headers) {
		RequestBuilder requestBuilder = createRequestBuilder(request);
		if (headers != null) {
			for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
				requestBuilder.addHeader(headerEntry.getKey(), headerEntry.getValue());
			}
		}
		RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
				.setConnectionRequestTimeout(site.getTimeOut()).setSocketTimeout(site.getTimeOut())
				.setConnectTimeout(site.getTimeOut()).setCookieSpec(CookieSpecs.DEFAULT);
		requestBuilder.setConfig(requestConfigBuilder.build());
		return requestBuilder.build();

	}

	private RequestBuilder createRequestBuilder(Request request) {
		String method = StringUtils.isBlank(request.getMethod()) ? HttpConstant.Method.GET
				: request.getMethod().toUpperCase();
		switch (method) {
		case HttpConstant.Method.GET:
			return RequestBuilder.get(request.getUrl());
		case HttpConstant.Method.POST:
			return RequestBuilder.post(request.getUrl())
					.addParameters((NameValuePair[]) request.getExtra("nameValuePair"));
		case HttpConstant.Method.HEAD:
			return RequestBuilder.get(request.getUrl());
		case HttpConstant.Method.PUT:
			return RequestBuilder.get(request.getUrl());
		case HttpConstant.Method.DELETE:
			return RequestBuilder.get(request.getUrl());
		case HttpConstant.Method.TRACE:
			return RequestBuilder.get(request.getUrl());
		default:
			return RequestBuilder.get(request.getUrl());

		}
	}

	@Override
	public void setThread(int thread) {
		httpClientGenerator.setPoolSize(thread);
	}

}
