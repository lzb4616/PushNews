package com.pushnews.app.servies;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import android.util.Log;

import com.pushnews.app.cofig.UserInfoManager;
import com.pushnews.app.model.User;
import com.pushnews.app.observable.MyObserver;

/**
 * 消息推送客户端
 * 
 * @author Administrator 最近修改时间2013-12-20
 */
public class PushClient extends Thread {

	private static final int BUFFER_SIZE = 1024;

	/**
	 * 远程地址
	 */
	private final InetSocketAddress mRemoteAddress;

	/**
	 * 连接通道
	 */
	private SocketChannel mSocketChannel;

	/**
	 * 接收缓冲区
	 */
	private final ByteBuffer mReceiveBuf;

	/**
	 * 端口选择器
	 */
	private Selector mSelector;

	/**
	 * 线程是否结束的标志
	 */
	private final AtomicBoolean mShutdown;

	static {
		java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
		java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");
	}

	public PushClient(InetSocketAddress remoteAddress) {
		mRemoteAddress = remoteAddress;

		// 初始化缓冲区
		mReceiveBuf = ByteBuffer.allocateDirect(BUFFER_SIZE);
		if (mSelector == null) {
			// 创建新的Selector
			try {
				mSelector = Selector.open();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		mShutdown = new AtomicBoolean(false);
	}

	/**
	 * 发送字符串到服务器
	 */
	public void sendMsg(String message) throws IOException {
		ByteBuffer sendBuf = ByteBuffer.allocate(BUFFER_SIZE);
		try {
			byte[] out = message.getBytes();
			if (out == null || out.length < 1) {
				return;
			}
			sendBuf.clear();
			sendBuf.put(out);
			sendBuf.flip();
			int code = mSocketChannel.hashCode();
			System.out.println(code);
			Log.e("mSocketChannel", mSocketChannel.hashCode()
					+ "mSocketChannel");
			mSocketChannel.write(sendBuf);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 打开通道
	 */
	private void startup() {
		try {
			// 打开通道
			mSocketChannel = SocketChannel.open();
			// 绑定到本地端口
			mSocketChannel.socket().setSoTimeout(30000);
			mSocketChannel.configureBlocking(false);
			if (mSocketChannel.connect(mRemoteAddress)) {
				System.out.println("开始建立连接:" + mRemoteAddress);
			}
			mSocketChannel.register(mSelector, SelectionKey.OP_CONNECT
					| SelectionKey.OP_READ, this);
			System.out.println("端口打开成功");

		} catch (final IOException e1) {
			e1.printStackTrace();
		}
	}

	private void select() {
		int nums = 0;
		try {
			if (mSelector == null) {
				return;
			}
			nums = mSelector.select(1000);
		} catch (final Exception e) {
			e.printStackTrace();
		}

		// 如果select返回大于0，处理事件
		if (nums > 0) {
			Iterator<SelectionKey> iterator = mSelector.selectedKeys()
					.iterator();
			while (iterator.hasNext()) {
				// 得到下一个Key
				final SelectionKey key = iterator.next();
				iterator.remove();
				// 检查其是否还有效
				if (!key.isValid()) {
					continue;
				}
				// 处理事件
				try {
					if (key.isConnectable()) {
						connect();
					} else if (key.isReadable()) {
						read(key);
					}
				} catch (final Exception e) {
					e.printStackTrace();
					key.cancel();
				}
			}
		}
	}

	@Override
	public void run() {
		startup();
		// 启动主循环流程
		while (!mShutdown.get()) {
			try {
				// do select
				select();
				Thread.sleep(500L);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		shutdown();
	}
	

	private void connect() throws IOException {
		if (isConnected()) {
			return;
		}
		// 完成SocketChannel的连接
		mSocketChannel.finishConnect();
		while (!mSocketChannel.isConnected()) {
			try {
				Thread.sleep(300);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
			mSocketChannel.finishConnect();
		}
	}

	public void disConnect() {
		mShutdown.set(true);
	}

	private void shutdown() {
		if (isConnected()) {
			try {
				mSocketChannel.close();
				while (mSocketChannel.isOpen()) {
					try {
						Thread.sleep(300);
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
					mSocketChannel.close();
				}
				System.out.println("端口关闭成功");
			} catch (final IOException e) {
				System.err.println("端口关闭错误:");
				e.printStackTrace();
			} finally {
				mSocketChannel = null;
			}
		} else {
			System.out.println("通道为空或者没有连接");
		}
		// 关闭端口选择器
		if (mSelector != null) {
			try {
				mSelector.close();
				System.out.println("端口选择器关闭成功");
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				mSelector = null;
			}
		}
	}

	private void read(SelectionKey key) throws IOException {
		// 接收消息
		final byte[] msg = recieve();
		if (msg != null) {
			String tmp = new String(msg);
			System.out.println("返回内容：");
			System.out.println(tmp);
			if (tmp.equals("login:FAIL") || tmp.equals("")) {
				MyObserver.getObserver().setMessage(MyObserver.LOGINFAILE);
				return;
			}
			if (tmp.startsWith("login:OK")) {
				String usermessage[] = tmp.split(",");
				User user = new User();
				user.setUid(usermessage[1]);
				user.setUsername(usermessage[2]);
				user.setNickname(usermessage[3]);
				UserInfoManager.user = user;
				MyObserver.getObserver().setMessage(MyObserver.LOGINSUCCESS);
				return;
			}
			String temp[] = tmp.split(",");
			for (int i = 0; i < temp.length; i++) {
				// 将相关的newsid发送给观察者，让它去提示页面做相关的处理
				MyObserver.getObserver().setMessage(temp[i]);
			}
		}
	}

	private byte[] recieve() throws IOException {
		if (isConnected()) {
			int len = 0;
			int readBytes = 0;

			synchronized (mReceiveBuf) {
				mReceiveBuf.clear();
				try {
					while ((len = mSocketChannel.read(mReceiveBuf)) > 0) {
						readBytes += len;
					}
				} finally {
					mReceiveBuf.flip();
				}
				if (readBytes > 0) {
					final byte[] tmp = new byte[readBytes];
					mReceiveBuf.get(tmp);
					return tmp;
				} else {
					System.out.println("接收到数据为空,重新启动连接");
					return null;
				}
			}
		} else {
			System.out.println("端口没有连接");
		}
		return null;
	}

	private boolean isConnected() {
		return mSocketChannel != null && mSocketChannel.isConnected();
	}

	// public static void main(String[] args) {
	// try {
	// final PushClient pc = new PushClient(new InetSocketAddress(
	// "10.10.121.194", 4444));
	// pc.start();
	// System.out.println("先向服务器发送数据---------->");
	// new Thread(new Runnable() {
	//
	// public void run() {
	// while (true) {
	// try {
	// InputStreamReader input = new InputStreamReader(
	// System.in);
	// BufferedReader br = new BufferedReader(input);
	// String sendText = br.readLine();
	// pc.sendMsg(sendText);
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	//
	// }
	// }
	// }).start();
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
}
