package com.pushnews.app.cofig;

public class Urls {
	public static final String LocalIP = "192.168.1.107";
	public static final String SocketAddressIP = LocalIP;//"222.200.98.233";
	public static final String NewsListUrl = "http://"+SocketAddressIP+":8080/nms/wap/listNews";	
	public static final String NewsDetailUrl ="http://"+SocketAddressIP+":8080/nms/wap/loadNews";
//	public static final String SendnewsUrl = "http://222.200.98.233:9123/nms/wap/addNews.action";
	public static final String LoginUrl = "http://222.200.98.233:9123/nms/wap/login.action";
	public static final String RegisterUrl = "http://222.200.98.233:9123/nms/wap/register.action";
	
	public static final int SocketAddressPort = 10000;
	public static final String SendnewsUrl = "http://"+LocalIP+":8080/nms/wap/addNews.action";
	public static final String LoginOutUrl = "http://"+LocalIP+":8080/nms/wap/logout.action";
	// public static final String NewsListUrl =
	// "http://192.168.1.105:8080/wap/listNews";
	// public static final String NewsDetailUrl =
	// "http://192.168.1.105:8080/wap/loadNews";

}
