package com.pushnews.app.model;

/** 新闻实体类 */
public class News {
	private String id;
	/** 主标题 */
	private String mainTitle;
	/** 内容*/
	private String content;
	/** 紧急程度 */
	private Integer degree;
	/** 类型id:13：文本，12：图片，11视频。 */
	private int type;
	/** 类型id：4：国内，5：国际 */
	private int type2;
	/** 类型 id：6~13*/
	private int type3;
	/**关键字*/
	private String keyword;
	private User publisher;

	public News(String mainTitle,String content,Integer degree,int type,int type2,int type3
			,String keyword,User publisher) {
		super();
		this.mainTitle = mainTitle;
		this.content = content;
		this.degree = degree;
		this.type = type;
		this.type2 = type2;
		this.type3 = type3;
		this.keyword = keyword;
		this.publisher = publisher;
	}

	public News() {

	}

	public String toStirng() {
		return "mainTitle =" + mainTitle ;
	}

	public String getMainTitle() {
		return mainTitle;
	}

	public void setMainTitle(String mainTitle) {
		this.mainTitle = mainTitle;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Integer getDegree() {
		return degree;
	}

	public void setDegree(Integer degree) {
		this.degree = degree;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getType2() {
		return type2;
	}

	public void setType2(int type2) {
		this.type2 = type2;
	}

	public int getType3() {
		return type3;
	}

	public void setType3(int type3) {
		this.type3 = type3;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public User getPublisher() {
		return publisher;
	}

	public void setPublisher(User publisher) {
		this.publisher = publisher;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
