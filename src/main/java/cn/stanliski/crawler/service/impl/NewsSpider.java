package cn.stanliski.crawler.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import cn.stanliski.crawler.entity.News;
import cn.stanliski.crawler.service.Spider;


public class NewsSpider extends Spider{

	@Override
	public List<News> extractPage(String html) {
		String page = request(html);
		Document doc = Jsoup.parse(page);
		Elements elements = doc.select(itemsScreening);
		List<News> list = new ArrayList<News>(elements.size());
		for(Element ele : elements){
			News news = new News();
			String contentHref = ele.attr("href");
			String contentHtml = request(contentHref);
			String content = extractPageDetail(contentHtml, detailContentScreening);
			news.setContent(content);
			news.setUrl(contentHref);
			news.setDate(new Date());
			list.add(news);
		}
		return list;
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

	public static void main(String args[]){
		Spider spider = new NewsSpider();
		//  europeangeoparks spider can get from the database.
		String url = "http://www.europeangeoparks.org/";
		spider.setDetailContentScreening("#container");
		spider.setItemsScreening("#index-news ul li h3 a");
		spider.setOriginHtml(url);
		final long start = System.nanoTime();
		List list = spider.crawler();
		final long end = System.nanoTime();
		System.out.println("Time (seconds) taken is " + (end - start)/1.0e9);
		System.out.println("list size:" + list.size());
	}

}
