package com.pushnews.app.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.pushnews.app.R;
import com.pushnews.app.adapter.ChannelListAdapter;
import com.pushnews.app.adapter.NewListAdapter;
import com.pushnews.app.cofig.Constants;
import com.pushnews.app.cofig.Messages;
import com.pushnews.app.cofig.Urls;
import com.pushnews.app.db.ChannelDbManger;
import com.pushnews.app.db.NewsItemDbManger;
import com.pushnews.app.https.HttpUtils;
import com.pushnews.app.https.TextLoadHandler;
import com.pushnews.app.model.Channel;
import com.pushnews.app.model.NewsItem;
import com.pushnews.app.servies.PushService;
import com.pushnews.app.slidingmenu.SlidingMenu;
import com.pushnews.app.slidingmenu.SlidingState;
import com.pushnews.app.view.NewsListView;
import com.pushnews.app.view.NewsListView.NewsListViewListener;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @author LZB
 * @see getLayoutInflater().inflate(int resource, ViewGroup root);
 *      这个是对自定义的xml布局文件进行解释，然后获取后android能够对其变为一个layout;
 *      对这个自定义的SlidingMenu添加左边和右边画面的组件 主要是确定他们的宽度
 */

public class NewsMainActivity extends BaseActivity implements NewsListViewListener {
	private final String TAG = "NewsMainActivity";

	private SlidingMenu slidingMenu;
	private Button slLeftCmlsBtn;
	private Button loginbutton;

	private String lr_title[] = { "设置", "消息推送", "我的收藏", "发送紧急新闻", "退出当前帐号" };
	/** 新闻显示列表 */
	private NewsListView mListView;
	/** 新闻列表适配器 */
	public static NewListAdapter mAdapter;
	/** 新闻列表集合 */
	private List<HashMap<String, Object>> newlist = new ArrayList<HashMap<String, Object>>();
	/** 新闻列表内容缓存集合 */
	private List<NewsItem> newListCache = new ArrayList<NewsItem>();
	/** 新闻内容数据库管理类 */
	private NewsItemDbManger dbManger;
	/** 新闻内容异步加载类 */
	private TextLoadHandler textLoadHandler;
	/** ui更新Handler */
	private Handler uiHandler;
	/** 新闻列表获取基本地址 */
	private String url = Urls.NewsListUrl;
	/** 最后一条的新闻时间 */
	private static long THELASTTIME = 0;
	/** 新闻ID */
	private String newsId;
	/** 新闻类型 */
	private String newsType;
	/** 新闻标题 */
	private String newsTitle;
	/** 新闻概要 */
	private String newsSummary;
	/** 新闻来源 */
	private String newsFrom;
	/** 新闻时间 */
	private long newsTime;
	/** 新闻图片地址 */
	private String imageUrl;
	/** 新闻详细内容地址 */
	private String detialUrl;
	/** 0：表示不是头条，1：表示头条 */
	private int topLine;
	/** 新闻栏目数据库管理器 */
	private ChannelDbManger channelDbManger;
	/** 新闻栏目列表内容 */
	private String[] newsContent = { "文本", "图片", "视频", "国内", "国际", "社会", "体育",
			"科技", "军事", "娱乐", "财经", "公益", "政务" };
	/** 新闻栏目列表内容标签 */
	private int[] mark = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
	/** 新闻栏目列表内容请求码 */
	private int[] code = { 1, 2, 3, 1, 2, 1, 2, 3, 4, 5, 6, 7, 8 };
	/** 新闻栏目列表内容类型 */
	private String[] type = { "t1", "t1", "t1", "t2", "t2", "t3", "t3", "t3",
			"t3", "t3", "t3", "t3", "t3" };
	private String[] lf_title;
	private ChannelListAdapter listAdapter;
	private List<Channel> channelList = new ArrayList<Channel>();
	private ListView lvLeft;

	/** 记录从这个NewsMainActivity跳到详细新闻界面 */
	private String comefrom = "NewsMainActivity";
	private String detailBaseurl = Urls.NewsDetailUrl;
	Channel cSelectInfo = new Channel();
	private HttpClient client = new DefaultHttpClient();
	private String newstype = "t1 = 1";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/* 设置全屏 */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sliding_menu);
		slidingMenu = (SlidingMenu) findViewById(R.id.slidingMenu);

		// 初始化视图
		initView();
		// 初始化相关工具类
		inintClass();
		setCenter();
		setLeft();
		setRight();
		// 频道管理
		slLeftCmlsBtn = (Button) findViewById(R.id.sl_left_cmls_btn);
		slLeftCmlsBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(NewsMainActivity.this,
						ChannelMangerActivity.class);
				startActivity(intent);
			}
		});
		// 登陆账号
		loginbutton = (Button) findViewById(R.id.layout_right_login);
		loginbutton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(NewsMainActivity.this,
						LoginActivity.class);
				startActivity(intent);

			}
		});
		//
		Intent servesIntent = new Intent(this, PushService.class);
		startService(servesIntent);
	}

	// =========================================================================================================

	/** 设置左边界面 */
	private void setLeft() {
		ViewGroup leftView = (ViewGroup) getLayoutInflater().inflate(
				R.layout.sl_left_v, null);
		/** 给新闻内容管理数据库赋值 */
		setData();
		getChannelContentFormDB();
		/* 这里获取自定义左右界面的宽度 */
		int leftWidth = (int) getResources()
				.getDimension(R.dimen.leftViewWidth);
		slidingMenu.setLeftView(leftView, leftWidth);
		/* 设置左边的listview */
		lvLeft = (ListView) leftView.findViewById(R.id.lvLeft);
		listAdapter = new ChannelListAdapter(getApplicationContext(),
				channelList);
		lvLeft.setAdapter(listAdapter);

		lvLeft.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
//				cSelectInfo = channelList.get(arg2);
//				newstype = cSelectInfo.getNewsType()+"="+cSelectInfo.getNewsCode();
//				refreshDataFromHttp();
			}
		});
	}

	private void setData() {
		channelDbManger.open();		
		if (channelDbManger.getdiaries().getCount() == 0) {
			for (int i = 0; i < type.length; i++) {
				Channel channel = new Channel(type[i], mark[i], newsContent[i],
						code[i]);
				channelDbManger.addChannel(channel);
			}
		}
		channelDbManger.close();
	}

	private void getChannelContentFormDB() {
		channelDbManger.open();
		channelList.clear();
		Cursor cursor = channelDbManger.getdiaries();
		if (cursor.moveToFirst()) {
			do {
				Channel channel = new Channel();
				channel.setNewsType(cursor.getString(cursor
						.getColumnIndex(Constants.ChannelTable.NEWS_TYPE)));
				channel.setNewsContent(cursor.getString(cursor
						.getColumnIndex(Constants.ChannelTable.NEWS_CONTENT)));
				channel.setMark(cursor.getInt(cursor
						.getColumnIndex(Constants.ChannelTable.MARK)));
				int test = channel.getMark();
				channel.setNewsCode(cursor.getInt(cursor
						.getColumnIndex(Constants.ChannelTable.NEWS_CODE)));
				if (channel.getMark() == 1) {
					channelList.add(channel);
				}
			} while (cursor.moveToNext());
		}
		channelDbManger.close();
	}

	// ====================================================================================================
	/** 设置中间界面 */
	private void setCenter() {
		ViewGroup centerView = (ViewGroup) getLayoutInflater().inflate(
				R.layout.sl_center_v, null);
		/* 对这个自定义的SlidingMenu添加中间画面的组件 */
		slidingMenu.setCenterView(centerView);

		View ivRight = centerView.findViewById(R.id.news_list_menu_right);
		/* 设置右边ImageView的响应事件 */
		ivRight.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				/*
				 * 获取当前界面的状态 总共有三种:SHOWRIGHT,SHOWCENTER,SHOWLEFT
				 * 主要是跟SHOWCENTER相对比
				 */
				if (slidingMenu.getCurrentUIState() == SlidingState.SHOWRIGHT) {
					slidingMenu.showViewState(SlidingState.SHOWCENTER);
				} else {
					slidingMenu.showViewState(SlidingState.SHOWRIGHT);
				}
			}
		});

		/* 同上 */
		View ivLeft = centerView.findViewById(R.id.news_list_menu_left);

		ivLeft.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (slidingMenu.getCurrentUIState() == SlidingState.SHOWLEFT) {
					slidingMenu.showViewState(SlidingState.SHOWCENTER);
				} else {
					slidingMenu.showViewState(SlidingState.SHOWLEFT);
				}
			}
		});

		/* 设置中间的listview */
		mListView = (NewsListView) centerView
				.findViewById(R.id.news_list_ListView);

		mListView.setPullLoadEnable(true);
		// 第一次新闻加载的逻辑
		if (HttpUtils.isMobileDataEnable(this.getApplicationContext())
				|| HttpUtils.isWifiDataEnable(this.getApplicationContext())) {// 如果有网络
			if (THELASTTIME == 0) {// 如果是第一次加载
				refreshDataFromHttp();// 从网络端获取新闻内容
			} else {
				refreshDataFromDb(10);// 从数据库获取内容
			}
		} else {
			refreshDataFromDb(10);// 从数据库获取内容
		}

		mListView.setAdapter(mAdapter);
		mListView.setNewsListViewListener(this);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id1) {
				// 从存储新闻信息的newlist中拿出当前被点击的那个item的所有新闻信息
				// 在这里获取所有的信息主要是为了，当用户点击收藏时，可以把这些数据直接存储到收藏列表中
				HashMap<String, Object> hashMap = newlist.get(position - 1);
				newsId = hashMap.get("id").toString();
				detialUrl = detailBaseurl + "?id=" + newsId;
				newsTitle = hashMap.get("title").toString();
				newsSummary = hashMap.get("short_content").toString();
				newsTime = (Long) hashMap.get("time");
				imageUrl = hashMap.get("imageUrl").toString();
				Log.i(TAG, "主页面加载数据成功");
				Intent intent = new Intent(NewsMainActivity.this,
						NewsDetailsActivity.class);
				// 将数据传到NewsDetailsActivity
				intent.putExtra("detialUrl", detialUrl);
				intent.putExtra("newsId", newsId);
				intent.putExtra("newsTitle", newsTitle);
				intent.putExtra("newsSummary", newsSummary);
				intent.putExtra("newsTime", newsTime);
				intent.putExtra("imageUrl", imageUrl);
				// 下面这些数据是新闻的类型和一个用于记录从这个NewsMainActivity跳到详细新闻界面 的String
				intent.putExtra("comefrom", comefrom);
				startActivity(intent);
				Log.i("输出", "OK");
			}
		});

	}

	/** 初始化控件方法 */
	private void initView() {
		// 新闻列表适配器
		mAdapter = new NewListAdapter(this, newlist, R.layout.newslist_item,
				new String[] { "id", "imageUrl", "title", "time",
						"short_content" }, new int[] {
						R.id.newslist_item_news_picture,
						R.id.newslist_item_title, R.id.newslist_item_time,
						R.id.newslist_item_digest });
	}

	/** 初始化工具类方法 */
	private void inintClass() {
		dbManger = new NewsItemDbManger(this);
		channelDbManger = new ChannelDbManger(this);
	}

	/** 结束列表加载进行状态 */
	private void onLoad() {
		mListView.stopRefresh();
		mListView.stopLoadMore();
		mListView.setRefreshTime("刚刚");
	}

	/** 列表下拉刷新方法 */
	@Override
	public void onRefresh() {
		if (HttpUtils.isMobileDataEnable(this.getApplicationContext())
				|| HttpUtils.isWifiDataEnable(this.getApplicationContext())) {// 如果有网络
			refreshDataFromHttp();// 从网络端更新数据
		} else {
			refreshDataFromDb(10);// 否则从数据库获取数据
		}
	}

	/** 列表加载更多方法 */
	@Override
	public void onLoadMore() {
		/* 测试时时间不要设置最后一个时间，这样不会从数据库获取数据，因为数据太少，数据库没有比这个时间更旧的 */
		if (newlist.size() == 0) {
			onLoad();
			return;
		}
		THELASTTIME = (Long) newlist.get(newlist.size() - 1).get("time");//
		// 获取列表中最后一条新闻的时间
		System.out.println("" + THELASTTIME);
		if (HttpUtils.isMobileDataEnable(this.getApplicationContext())
				|| HttpUtils.isWifiDataEnable(this.getApplicationContext())) {// 如果有网络
			if (THELASTTIME > getLastTimeFromDb() || THELASTTIME == 0) {//
				// 如果列表时间大于数据库时间或者为空
				loadMoreDataFromHttp(THELASTTIME);// 以最后一条的时间为参数，从网络端获取新数据
			} else {
				loadMoreDataFromDb(THELASTTIME);// 从数据库获取数据
				saveCacheToDb();// 并且把数据从缓存中保存到数据库
			}
		} else {
			loadMoreDataFromDb(THELASTTIME);// 从数据库获取数据
		}
	}

	/** 下拉刷新从网络端获取数据方法 */
	private void refreshDataFromHttp() {
		uiHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {// ui更新消息
				switch (msg.what) {
				case Messages.TASK_SUCCESSS:// 如果成功
					if (msg.obj != null) {// 如果返回数据不为空
						newlist.clear();// 列表清除
						getNewsList((List<NewsItem>) msg.obj);// 获取显示列表
						testData(newlist);
						newListCache.addAll((List<NewsItem>) msg.obj);// 加入缓存中
						mListView.setAdapter(mAdapter);
					}
					onLoad();
					break;
				}
			}
		};
		long i = java.lang.System.currentTimeMillis();
		/** 文本加载线程 */
//		 textLoadHandler = new TextLoadHandler(this, uiHandler, url + "?pt=" +
//				 i+"&"+newstype);

		textLoadHandler = new TextLoadHandler(this, uiHandler, url);
		Log.i(TAG, url + "?pt=" + java.lang.System.currentTimeMillis());
		textLoadHandler.start();
	}

	/** 加载更多从网络端获取数据 */
	private void loadMoreDataFromHttp(long newsTime) {
		uiHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {// ui更新消息
				switch (msg.what) {
				case Messages.TASK_SUCCESSS:// 如果成功
					if (msg.obj != null) {// 如果返回数据不为空
						getNewsList((List<NewsItem>) msg.obj);// 获取显示列表
						testData(newlist);
						newListCache.addAll((List<NewsItem>) msg.obj);// 加入缓存中
						System.out.println("已经获取数据");
						System.out.println("" + mAdapter.getCount());
						mAdapter.notifyDataSetChanged();
					}
					onLoad();
					break;
				}
			}
		};
		/** 文本加载线程 */

//		textLoadHandler = new TextLoadHandler(this, uiHandler, url + "?pt="
//				+ newsTime+"&"+newstype);
		 textLoadHandler = new TextLoadHandler(this, uiHandler, url);
		Log.i(TAG, url + "?pt=" + newsTime);
		textLoadHandler.start();
	}

	/**
	 * @description 下拉刷新从数据库获取数据方法
	 * 
	 * @param itemsNum
	 *            获取列表的数目
	 * 
	 * */
	private void refreshDataFromDb(int itemsNum) {
		dbManger.open();
		newlist.clear();
		Cursor cursor = dbManger.getdiaries();
		startManagingCursor(cursor);
		if (cursor.moveToLast()) {
			do {
				HashMap<String, Object> dataMap = new HashMap<String, Object>();
				dataMap.put("id", cursor.getString(cursor
						.getColumnIndex(Constants.NewsListTable.NEWS_ID)));
				dataMap.put(
						"imageUrl",
						cursor.getString(cursor
								.getColumnIndex(Constants.NewsListTable.NEWS_IMAGE_URL)));
				dataMap.put("title", cursor.getString(cursor
						.getColumnIndex(Constants.NewsListTable.NEWS_TITLE)));
				dataMap.put("time", cursor.getLong(cursor
						.getColumnIndex(Constants.NewsListTable.NEWS_TIME)));
				dataMap.put("short_content", cursor.getString(cursor
						.getColumnIndex(Constants.NewsListTable.NEWS_SUMMRY)));
				newlist.add(dataMap);
				cursor.moveToPrevious();
				itemsNum--;
			} while (itemsNum != 0);
		}
		testData(newlist);
		mListView.setAdapter(mAdapter);
		onLoad();
		dbManger.close();
	}

	/**
	 * @description 加载更多从数据库获取数据方法
	 * 
	 * @param newsTime
	 *            时间参数
	 * 
	 * */
	private void loadMoreDataFromDb(final long newsTime) {
		dbManger.open();
		/* 以时间和类型为参数，从数据库刷选数据 */
		Cursor cursor = dbManger.getData("国内", "" + newsTime, "10");
		startManagingCursor(cursor);
		if (cursor.moveToFirst()) {
			do {
				HashMap<String, Object> dataMap = new HashMap<String, Object>();
				dataMap.put("id", cursor.getString(cursor
						.getColumnIndex(Constants.NewsListTable.NEWS_ID)));
				dataMap.put(
						"imageUrl",
						cursor.getString(cursor
								.getColumnIndex(Constants.NewsListTable.NEWS_IMAGE_URL)));
				dataMap.put("title", cursor.getString(cursor
						.getColumnIndex(Constants.NewsListTable.NEWS_TITLE)));
				dataMap.put("time", cursor.getLong(cursor
						.getColumnIndex(Constants.NewsListTable.NEWS_TIME)));
				dataMap.put("short_content", cursor.getString(cursor
						.getColumnIndex(Constants.NewsListTable.NEWS_SUMMRY)));
				newlist.add(dataMap);
			} while (cursor.moveToNext());
		}
		testData(newlist);
		System.out.println("" + mAdapter.getCount());
		mAdapter.notifyDataSetChanged();
		onLoad();
		dbManger.close();

	}

	/** 将缓存保存至数据库方法 */
	private void saveCacheToDb() {
		dbManger.open();
		for (NewsItem newsItem : newListCache) {
			dbManger.addNews(newsItem);
		}
		newListCache.clear();
		dbManger.close();
	}

	/** 从数据库获取最后一个数据的时间 */
	private long getLastTimeFromDb() {
		long time = 0;
		dbManger.open();
		Cursor cursor = dbManger.getdiaries();
		startManagingCursor(cursor);
		if (cursor.moveToLast()) {
			time = cursor.getLong(cursor
					.getColumnIndex(Constants.NewsListTable.NEWS_TIME));
		}
		dbManger.close();
		return time;

	}

	/** 获得适配器适合的列表集合方法 */
	private void getNewsList(List<NewsItem> newsItemsList) {
		List<HashMap<String, Object>> lHashMaps = new ArrayList<HashMap<String, Object>>();
		for (NewsItem newsItem : newsItemsList) {
			HashMap<String, Object> dataMap = new HashMap<String, Object>();
			dataMap.put("id", newsItem.getNews_id());
			dataMap.put("imageUrl", newsItem.getImageUrl());
			dataMap.put("title", newsItem.getNewsTitle());
			dataMap.put("time", newsItem.getNewsTime());
			dataMap.put("short_content", newsItem.getNewsSummary());
			newlist.add(dataMap);
		}

	}

	/** 测试数据方法 */
	private void testData(List<HashMap<String, Object>> list) {
		for (int i = 0; i < list.size(); i++) {
			Log.i(TAG, (String) list.get(i).get("id"));
			Log.i(TAG, (String) list.get(i).get("imageUrl"));
			Log.i(TAG, list.get(i).get("time").toString());
			Log.i(TAG, (String) list.get(i).get("title"));
			Log.i(TAG, (String) list.get(i).get("short_content"));
		}
		System.out.println("listSize======" + list.size());
	}

	// ==================================================================================================
	/** 设置右边界面 */
	private void setRight() {
		ViewGroup rightView = (ViewGroup) getLayoutInflater().inflate(
				R.layout.sl_right_v, null);

		/* 这里获取自定义左右界面的宽度 */
		int rightWidth = (int) getResources().getDimension(
				R.dimen.rightViewWidth);
		slidingMenu.setRightView(rightView, rightWidth);

		/* 设置右边的listview */
		ListView lvRight = (ListView) rightView.findViewById(R.id.lvRight);
		lvRight.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item,
				R.id.tv_item, lr_title));
		lvRight.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (arg2 == 0) {
					Intent intent = new Intent(NewsMainActivity.this,
							SystemSetActivity.class);
					startActivity(intent);
				}
				if (arg2 == 1) {
					Intent intent = new Intent(NewsMainActivity.this,
							PushNewListActivity.class);
					startActivity(intent);
				}
				if (arg2 == 2) {
					Intent intent = new Intent(NewsMainActivity.this,
							MycollectActivity.class);
					startActivity(intent);
				}
				if (arg2 == 3) {
					Intent intent = new Intent(NewsMainActivity.this,
							SendNewsActivity.class);
					startActivity(intent);
				}
//				if (arg2 == 4) {
//				new AsyncTask<String, Void, Boolean>() {
//					@Override
//					protected Boolean doInBackground(String... params) {
//						// TODO Auto-generated method stub
//						return logout(params[0]);
//					}
//
//					@Override
//					protected void onPostExecute(Boolean result) {
//						if (result) {
//							Toast.makeText(NewsMainActivity.this, "登出成功",
//									Toast.LENGTH_SHORT).show();
//						} else {
//							Toast.makeText(NewsMainActivity.this, "登出失败",
//									Toast.LENGTH_SHORT).show();
//						}
//						super.onPostExecute(result);
//					}
//					
//				}.execute(Urls.LoginOutUrl);
//				}
			}
		});
	}

	
	/**
	 * 登出
	 * @param url
	 */
	public Boolean logout(String url){
		HttpGet hg = new HttpGet(url);
        HttpResponse hr; 
		try {
			hr = client.execute(hg);
			HttpEntity entity = hr.getEntity();
			if(entity != null){
				System.out.println(EntityUtils.toString(entity,HTTP.UTF_8));
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
		
	}
	@Override
	protected void onResume() {
		getChannelContentFormDB();
		listAdapter.notifyDataSetChanged();
		super.onResume();
	}
	
	@Override
	protected void onStop() {
		saveCacheToDb();
		super.onStop();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		Intent servesIntent = new Intent(this, PushService.class);
		stopService(servesIntent);
		super.onDestroy();
	}
}