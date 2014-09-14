package com.pushnews.app.https;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import com.pushnews.app.R;
import com.pushnews.app.cofig.UserInfoManager;
import com.pushnews.app.entity.BitMapTools;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

/**
 * 异步加载图片类
 * 
 * @author LZB
 * 
 * */

public class ImageLoaderUntil {
	private final String TAG = "ImageLoader";
	private Context mContext;
	// 图片缓存集合
	private HashMap<String, SoftReference<Bitmap>> mHashMap_caches;
	ImageLoadTask task ;


	// 构造方法，
	public ImageLoaderUntil(Context context) {
		this.mContext = context;
		mHashMap_caches = new HashMap<String, SoftReference<Bitmap>>();
		this.task =  new ImageLoadTask();
	}

	/**
	 * 图片下载任务类
	 * 
	 */
	private class ImageLoadTask {
		/** 图片下载地址 */
		String path;
		/** 下载的图片 */
		Bitmap bitmap;
		/** 新闻ID */
		String newsId;
	}

	/**
	 * 利用图片路径下载图片 利用内存缓存和文件缓存双缓存机制优化下载
	 * 
	 * @param path
	 *            图片下载地址
	 * @param callback
	 *            回调接口
	 * @param newsId
	 *            新闻ID
	 * @return
	 */
	public Bitmap imageLoad(String path, String newsId) {
		Bitmap bitmap = null;
		// 如果内存缓存中存在该路径，则从内存中直接获取该图片
		if (path == null ||path.equals("")) {			
			Resources res = UserInfoManager.context.getResources();
			return BitmapFactory.decodeResource(res, R.drawable.defaultimage);
		}
		if (mHashMap_caches.containsKey(newsId)) {
			bitmap = mHashMap_caches.get(newsId).get();
			// 如果缓存中的图片已经被释放，则从该缓存中移除图片路径
			if (bitmap == null) {
				mHashMap_caches.remove(newsId);
			} else {
				return bitmap;
			}
		}
		// 如果缓存中未得到缓存需要的图片，则从文件中读取该图片
		// 获取本文件的图片类的文件存储路径
		File dir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
		Log.i(TAG, "dir==============" + dir);
		// 创建要获取的图片路径
		File file = new File(dir, newsId + ".jpg");
		// 从文件中读取指定路径的图片
		bitmap = BitMapTools.getBitMap(file.getAbsolutePath());
		// 如果文件中存在该图片，则直接从文件中获取图片
		if (bitmap != null) {
			// 向集合缓存中添加缓存
			mHashMap_caches.put(newsId, new SoftReference<Bitmap>(
						bitmap));
			return bitmap;
		}else{
			// 设置下载路径
			task.path = path;
			// 设置下载的id
			task.newsId = newsId;			
			return getbitmapFormWeb();
		}
	}

	public Bitmap getbitmapFormWeb() {
			try {
				Log.i(TAG, "开始任务");
				// 从网络端获取图片
				task.bitmap = getHttpBitmap(task.path);
				if (task.bitmap == null) {
					return null;
				}
				// 如果图片下载成功就向内存缓存和文件中放置缓存信息，以便之后从双缓存中读取图片信息
				if (task.bitmap != null) {
					// 向集合缓存中添加缓存
					mHashMap_caches.put(task.path, new SoftReference<Bitmap>(
							task.bitmap));
					// 向文件中添加缓存信息
					// 获取文件存储路径
					File dir = mContext
							.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
					// 如果文件路径不存在 ,则创建该路径
					if (!dir.exists()) {
						dir.mkdirs();
					}
					// 创建图片存储路径
					File file = new File(dir, task.newsId + ".jpg");
					// 向该文件路径中存储图片
					Log.i(TAG, file.getAbsolutePath());
					BitMapTools.saveBitmap(file.getAbsolutePath(), task.bitmap);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, e.getLocalizedMessage() + "======");
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e(TAG, e.getLocalizedMessage());
				e.printStackTrace();
				return null;
			}		
		return task.bitmap;
	}
	/**
	 * 获取网落图片资源函数
	 * 
	 * @param url
	 * @return
	 */
	public static Bitmap getHttpBitmap(String url) {
		URL myFileURL;
		Bitmap bitmap ;
		try {
			myFileURL = new URL(url);
			// 获得连接
			HttpURLConnection conn = (HttpURLConnection) myFileURL
					.openConnection();
			// 设置超时时间为6000毫秒，conn.setConnectionTiem(0);表示没有时间限制
			conn.setConnectTimeout(6000);
			conn.setRequestMethod("GET");
			// 连接设置获得数据流
			conn.setDoInput(true);
			/*
			 * 不使用缓存 conn.setUseCaches(false); // 这句可有可无，没有影响 conn.connect();
			 */
			// 得到数据流
			int responseCode = conn.getResponseCode();
			System.out.println("responseCode===" + responseCode);
			InputStream is = conn.getInputStream();
			// 解析得到图片
			bitmap = BitmapFactory.decodeStream(is);
			// 关闭数据流
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return bitmap;

	}

}
