package com.yxh.init_tool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.json.JSONObject;
import com.google.gson.Gson;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts.Data;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import android.view.View;

public class DownUtil {
	public static final int TYPE_CONTACT = 0;

	/**
	 * 获取输入流
	 * 
	 * @param path
	 *            请求地址
	 * @return InputStream
	 */
	private static InputStream getInputStream(String path) {
		InputStream is = null;
		try {
			URL url = new URL(path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			is = conn.getInputStream();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return is;
	}

	/**
	 * InputStream读数据
	 * 
	 * @param path
	 *            请求地址
	 * @return String
	 */
	private static String getData(String path) {
		String result = null;
		InputStream is = getInputStream(path);
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String str = null;
			while ((str = br.readLine()) != null) {
				result += str;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (result != null) {
			result = result.substring(4);
		}
		return result;
	}

	/**
	 * 解析json成联系人集合
	 * 
	 * @param result
	 *            json数据
	 * @return 联系人集合
	 */
	public static List<Contact> parseContact(String result) {
		List<Contact> contacts = null;
		try {
			Gson gson = new Gson();
			DataContact data = gson.fromJson(result, DataContact.class);
			if (data != null) {
				contacts = data.getList();
			}

		} catch (Exception e) {
			System.err.println("解析Json失败-->parseContact()");
			e.printStackTrace();
		}
		return contacts;
	}

	/**
	 * 存储到内存中
	 * 
	 * @param result
	 *            需要存储的数据
	 * @param path
	 *            路径
	 * @param isAppend
	 *            是否覆盖 true为不覆盖，false覆盖
	 */
	public static void storageContactToDisk(Handler handler,String result, String path, boolean isAppend) {
		BufferedWriter bw = null;
		try {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				File file = new File(Environment.getExternalStorageDirectory().getPath() + "//" + path);
				if (file.exists()) {
					System.out.println("检测到文件已经存在");
				} else {
					System.err.println("检测到文件不存在");
					if (file.createNewFile()) {
						System.out.println("创建文件成功");
					} else {
						System.err.println("创建文件失败");
					}
				}
				FileOutputStream fos = new FileOutputStream(file, isAppend);
				bw = new BufferedWriter(new OutputStreamWriter(fos, "utf-8"));
				bw.write(result);
				handler.sendEmptyMessage(1);
				System.out.println("写入内存成功");
			} else {
				System.err.println("内存卡不存在");
			}
		} catch (IOException e) {
			System.err.println("IO异常");
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("存储失败");
			e.printStackTrace();
		} finally {
			try {
				if (bw != null) {
					bw.flush();
					bw.close();
				}
			} catch (IOException e) {
				System.err.println("关闭流失败");
				e.printStackTrace();
			}
		}
	}

	/**
	 * 存储到通讯录
	 * 
	 * @param context
	 *            上下文对象
	 * @param list
	 *            联系人集合
	 * @return 返回失败位置的index
	 */
	public static int storageContactToContactApp(Context context, List<Contact> list) {
		int flag=-1;
		if(list!=null){
			flag = list.size() - 1;
			ContentResolver resolver = context.getContentResolver();
			long initTime=System.currentTimeMillis();
			for (int i = 0; i < list.size(); i++) {
				Contact contact = list.get(i);
				try {
					// 插入一个联系人id
					ContentValues values = new ContentValues();
					Uri rawContactUri = resolver.insert(RawContacts.CONTENT_URI, values);
					long rawContactId = ContentUris.parseId(rawContactUri);
					// 插入电话数据
					values.clear();
					values.put(Data.RAW_CONTACT_ID, rawContactId);
					values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
					values.put(Phone.NUMBER, contact.getMobilephone());
					values.put(Phone.TYPE, Phone.TYPE_MOBILE);
					resolver.insert(android.provider.ContactsContract.Data.CONTENT_URI, values);
					// 插入姓名数据
					values.clear();
					values.put(Data.RAW_CONTACT_ID, rawContactId);
					values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
					values.put(StructuredName.GIVEN_NAME, contact.getUserName());
					resolver.insert(android.provider.ContactsContract.Data.CONTENT_URI, values);
				} catch (Exception e) {
					System.err.println("存储到通讯录失败！");
					flag = i;
					long endTime=System.currentTimeMillis();
				}
				Log.d("wsg", contact.toString());
			}
			System.out.println("存储到通讯录成功！");
			long endTime=System.currentTimeMillis();
			SimpleDateFormat sdf=new SimpleDateFormat("mm:ss",Locale.CHINA);
			System.out.println("写入到通讯录消费时间："+sdf.format(new Date(endTime)));
		}
		return flag;
	}

	/**
	 * 向服务端标记返回结果
	 * 
	 * @param contacts
	 *            联系人集合
	 * @param flag
	 *            失败位置的下标
	 */
	public static void flagResult(List<Contact> contacts, int flag,Handler handler) {
		if(flag==-1){
			System.err.println("服务端未返回数据");
			handler.sendEmptyMessage(0);
			return;
		}
		try {
			String path = null;
			int[] success = new int[flag];
			for (int i = 0; i < success.length; i++) {
				success[i] = contacts.get(i).getId();
			}
			if (flag != contacts.size() - 1) {
				int[] failure = new int[contacts.size() - flag];
				for (int i = 0; i < failure.length; i++) {
					failure[i] = contacts.get(contacts.size() - i).getId();
					path = assemblePath(success, failure);
				}
			} else {
				path = assemblePath(success);
			}
			String result = getData(path);
			JSONObject object = new JSONObject(result);
			int _result = object.getInt("result");
			if (_result == 1) {
				handler.sendEmptyMessage(1);
				System.out.println("向服务端标记返回结果成功！");
			} else {
				System.err.println("向服务端标记返回结果失败！");
			}
		} catch (Exception e) {
			System.err.println("向服务端标记返回结果失败！");
			e.printStackTrace();
		}
	}

	/**
	 * 装配数据，有失败的情况
	 * 
	 * @param success
	 *            成功的id数组
	 * @param failure
	 * @return 返回地址
	 */
	public static String assemblePath(int[] success, int[] failure) {
		String succ = assembleData(success);
		String fail = assembleData(failure);
		return "http://proxy.zed1.cn:88/data/cgi/user!update.action?para={\"success\":[" + succ + "],\"failure\":["
				+ fail + "]}";
	}

	/**
	 * 装配数据，没有失败的情况
	 * 
	 * @param success
	 *            成功的id数组
	 * @return 返回地址
	 */
	public static String assemblePath(int[] success) {
		return "http://proxy.zed1.cn:88/data/cgi/user!update.action?para={\"success\":[" + assembleData(success)
				+ "],\"failure\":[]}";
	}

	/**
	 * 装配数据
	 * 
	 * @param data
	 *            成功或者失败的id数组
	 * @return 示例：1,2。。。，7
	 */
	public static String assembleData(int[] data) {
		String result = null;
		for (int i = 0; i < data.length; i++) {
			result += "," + data[i];
		}
		return result;
	}

	public static class DownloadThread extends Thread {
		private String path;
		private int type;
		private Context context;
		private Handler handler;

		public DownloadThread(Context context, Handler handler, int type, String path) {
			this.handler=handler;
			this.context = context;
			this.type = type;
			this.path = path;
		}

		@Override
		public void run() {
			switch (type) {
			case TYPE_CONTACT:
				String result = getData(path);
				storageContactToDisk(handler,result, "config.txt", true);
				List<Contact> contacts = parseContact(result);
				int flag = storageContactToContactApp(context, contacts);
				flagResult(contacts, flag,handler);
				break;
			}
		}
	}
}
