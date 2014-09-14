package com.pushnews.app.model;

/** 频道实体类 */
public class Channel {
	/** 新闻ID */
	private int channel_id;
	/** 新闻类型 */
	private String newsType;
	/** 新闻内容 */
	private String newsContent;
	/** 新闻的配置标签 */
	private int mark;
	/** 新闻的请求码 */
	private int newsCode;

	public Channel() {
		super();
	}

	public Channel(String newsType, int mark, String newsContent, int newsCode) {
		super();
		this.newsType = newsType;
		this.mark = mark;
		this.newsContent = newsContent;
		this.newsCode = newsCode;
	}

	public Channel(int mark, String newsContent) {
		super();
		this.mark = mark;
		this.newsContent = newsContent;
	}

	public String getNewsType() {
		return newsType;
	}

	public void setNewsType(String newsType) {
		this.newsType = newsType;
	}

	public int getNewsCode() {
		return newsCode;
	}

	public void setNewsCode(int newsCode) {
		this.newsCode = newsCode;
	}

	public int getMark() {
		return mark;
	}

	public void setMark(int mark) {
		this.mark = mark;
	}

	public int getId() {
		return channel_id;
	}

	public void setId(int channel_id) {
		this.channel_id = channel_id;
	}

	@Override
	public String toString() {
		return "channel_id=" + channel_id + ";newsType=" + newsType + ";mark="
				+ mark;
	}

	public String getNewsContent() {
		return newsContent;
	}

	public void setNewsContent(String newsContent) {
		this.newsContent = newsContent;
	}
}
