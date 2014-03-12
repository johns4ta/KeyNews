package com.johns4ta.news_app;

import java.util.Date;

public class Article implements Comparable<Article> {

	private Date dateTime;
	private String title;
	private String source;
	private String link;

	//Get article Link
	public String getLink() {
		return link;
	}

	//Set article link
	public void setLink(String link) {
		this.link = link;
	}

	//Get article date
	public Date getDateTime() {
		return dateTime;
	}

	//Set article data
	public void setDateTime(Date datetime) {
		this.dateTime = datetime;
	}

	//Set article source
	public String getSource() {
		return source;
	}

	//Get article source
	public void setSource(String source) {
		this.source = source;
	}

	//Get article title
	public String getTitle() {
		return title;
	}

	//Set article title
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public int compareTo(Article o) {
		return o.getDateTime().compareTo(getDateTime());
		// return getDateTime().compareTo(o.getDateTime());
	}
}
