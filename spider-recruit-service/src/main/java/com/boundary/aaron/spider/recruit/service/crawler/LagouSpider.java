package com.boundary.aaron.spider.recruit.service.crawler;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;

import lombok.Data;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.downloader.HttpClientGenerator;

@Data
public class LagouSpider {
	private static final String SET_COOKIE = "Set-Cookie";
	private static final HttpClientGenerator httpClientGenerator = new HttpClientGenerator();

	private String url;
	private Site site;

	public LagouSpider(String url, String charSet) {
		this.url = url;
		Site lagou = Site.me().setRetryTimes(3).setSleepTime(100).setTimeOut(10 * 1000)
				.setCharset(charSet).setUserAgent(
						"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_4) AppleWebKit/537.36 (KHTML,"
								+ " like Gecko Chrome/74.0.3729.75 Safari/537.36");
		initRequestHeaders(url, lagou);
	}

	private void initRequestHeaders(String url, Site lagou) {
		HttpClient httpClient = httpClientGenerator.getClient(lagou);
		HttpUriRequest httpRequest = RequestBuilder.get(url).build();

		try {
			HttpResponse httpResposne = httpClient.execute(httpRequest);
			Header[] headers = httpResposne.getHeaders(SET_COOKIE);
			String cookie = "";
			for (int i = 0; i < headers.length; i++) {
				cookie += headers[i].getValue() + ";";
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	public static void main(String[] args) {
		new LagouSpider(
				"https://www.lagou.com/jobs/list_?city=%E4%B8%8A%E6%B5%B7&cl=false&fromSearch=true&labelWords=&suginput=",
				"UTF-8");
	}
}
