package cn.stanliski.crawler.entity;

import java.util.Date;

/**
 * 
 * @author Stanley
 *
 */
public class News {
	
	private int id;
	
	private String content;
	
	private Date date;
	
	private String url;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
}
