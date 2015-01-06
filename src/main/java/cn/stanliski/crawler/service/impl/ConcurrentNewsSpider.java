package cn.stanliski.crawler.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import cn.stanliski.crawler.entity.News;
import cn.stanliski.crawler.service.Spider;

/**
 * Concurrency News Spider.
 * @author Stanley
 *
 */
public class ConcurrentNewsSpider extends Spider {

	@Override
	public List<News> extractPage(String html) {
		String page = request(html);
		Document doc = Jsoup.parse(page);
		Elements elements = doc.select(itemsScreening);
		List<News> list = new ArrayList<News>(elements.size());
		final int numberOfCores = Runtime.getRuntime().availableProcessors();
		final double blockingCoefficient = 0.9;
		final int poolSize = (int)(numberOfCores / (1 - blockingCoefficient));
		final List<Callable<RequestEntry>> partitions = 
				new ArrayList<Callable<RequestEntry>>(poolSize);
		for(Element ele : elements){
			final String contentHref = ele.attr("href");
			partitions.add(new Callable<RequestEntry>(){
				public RequestEntry call() throws Exception {
					String pageHtml = request(contentHref);
					return new RequestEntry(contentHref, pageHtml);
				}
			});
		}
		final ExecutorService exectutorPool = Executors.newFixedThreadPool(poolSize);
		try {
			final List<Future<RequestEntry>> pageResult = 
					exectutorPool.invokeAll(partitions, 10000, TimeUnit.SECONDS);
			for(Future<RequestEntry> future : pageResult){
				News news = new News();
				String content = extractPageDetail(future.get().getHtml(), detailContentScreening);
				news.setContent(content);
				news.setUrl(future.get().getUrl());
				news.setDate(new Date());
				list.add(news);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return list;
	}

	public String requestHtml(String url){
		return request(url);
	}

	@Override
	public List crawler() {
		return extractPage(originHtml);
	}


	@Override
	public String extractPageDetail(String html, String screening) {
		Document docContent = Jsoup.parse(html);
		Elements content = docContent.select(screening);
		return content.get(0).html();
	}

	/**
	 * Request Entity
	 * 
	 * @author Stanley
	 *
	 */
	private class RequestEntry {

		public String url;

		public String html;

		public RequestEntry(String url, String html){
			this.url = url;
			this.html = html;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getHtml() {
			return html;
		}

		public void setHtml(String html) {
			this.html = html;
		}

	}
	
	public static void main(String args[]){
		Spider spider = new ConcurrentNewsSpider();
	//  europeangeoparks spider can get from the database.
		String url = "http://www.europeangeoparks.org/";
		spider.setDetailContentScreening("#container");
		spider.setItemsScreening("#index-news ul li h3 a");
		spider.setOriginHtml(url);
		final long start = System.nanoTime();
		List list = spider.crawler();
		final long end = System.nanoTime();
		System.out.println("Time (seconds) taken is " + (end - start)/1.0e9);
		System.out.println("list size = " + list.size());
	}

}
