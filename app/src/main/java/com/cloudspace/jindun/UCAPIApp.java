package com.cloudspace.jindun;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Message;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Display;

import com.cloudspace.jindun.config.Configs;
import com.cloudspace.jindun.utils.ActivityUtil;
import com.cloudspace.jindun.utils.SharedPreferencesWrapper;
import com.cloudspace.jindun.utils.ToastMaker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.rong.imkit.RongIM;

public class UCAPIApp extends Application
{
	private static final String TAG = UCAPIApp.class.getSimpleName();

	private boolean mServiceStarted = false;

	private final List<Message> mQueue = new ArrayList<Message>();

	public static void setApp(UCAPIApp app)
	{
		instance = app;
	}

	public static UCAPIApp getApp()
	{
		if (instance == null)
		{
			return instance = new UCAPIApp();
		}
		return instance;
	}
	@Override
	public void onCreate()
	{
		RongIM.init(this);
		setApp(this);
		super.onCreate();
		doOnCreate(this);
	}

	private static UCAPIApp instance;

	public static UCAPIApp getInstance() {
		return instance;
	}

	public void doOnCreate(Application app)
	{
		ActivityUtil.init();
		ToastMaker.init(this);
		pre = getSharedPreferences();
	}

	public SharedPreferences getSharedPreferences() {
		if (pre == null) {
			SharedPreferences sp = this.getSharedPreferences(Configs.PRE_NAME,
					Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
			pre = new SharedPreferencesWrapper(sp);
		}
		return pre;
	}

	private Object mSpLock;
	public synchronized Object getSharedPreferenceLock() {
		if (mSpLock == null) {
			mSpLock = new Object();
		}
		return mSpLock;
	}

	private static Gson gson;

	public static Gson getGson() {
		if (gson == null) {
			return (new GsonBuilder()).create();
		}
		return gson;
	}

	private int width;
	private int height;
	private int density;

	public int getScreenWidth() {
		return width;
	}

	public static boolean isInitScreenParam = false;

	public void initScreenParams(Display display) {
		if (!isInitScreenParam) {
			isInitScreenParam = true;
			DisplayMetrics metric = new DisplayMetrics();
			display.getMetrics(metric);
			this.width = metric.widthPixels; // 屏幕宽度（像素）
			this.height = metric.heightPixels; // 屏幕高度（像素） metric.heightPixels
			// 包括信息栏
			this.density = metric.densityDpi;
		}
	}

	//运用list来保存们每一个activity是关键
	private List<AppCompatActivity> mList = new LinkedList<AppCompatActivity>();

	// add Activity
	public void addActivity(AppCompatActivity activity) {
		mList.add(activity);
	}

	public void removeActivity(AppCompatActivity activity) {
		mList.remove(activity);
	}

	public SharedPreferences pre;

	public int getScreenHeight() {
		return height;
	}

	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}
}
