package com.xpd.xpdtweet;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.Map;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;

public class XPDApplication extends Application implements  OnSharedPreferenceChangeListener {
	public static String userName;
	String IPAddress;
	String Port;
	String android_id;
	private SharedPreferences prefs;
	private boolean serviceRunning;

	//this guy is our content provider-database kind of//	
	public ArrayList<ChatMessage> listOfPosts=new ArrayList<ChatMessage>();
	public Map<String, ArrayList<ChatMessage>> replys = new HashMap<String, ArrayList<ChatMessage>>();
	public Map<String, ArrayList<ChatMessage>> personalreplys = new HashMap<String, ArrayList<ChatMessage>>();
	public Map<String, ArrayList<String>> polloptions = new HashMap<String, ArrayList<String>>();
	public Map<String, ArrayList<Integer>> pollreplys = new HashMap<String, ArrayList<Integer>>();
	public ArrayList<String> spammers = new ArrayList<String>();
	public void onCreate() {
		super.onCreate();
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		this.prefs.registerOnSharedPreferenceChangeListener(this);

		android_id = Secure.getString(getApplicationContext().getContentResolver(),Secure.ANDROID_ID);
		//SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		//userName= preferences.getString("userName", "Anonymous");
	};

	public String getUserName() {
		if(userName==null){
			 XPDApplication.userName = this.prefs.getString("userName", null);
		}
		return XPDApplication.userName;
	}
	public void setUserName(String userName) {
		XPDApplication.userName = userName;
	}
	public boolean isServiceRunning() {
	    return serviceRunning;
	  }

	  public void setServiceRunning(boolean serviceRunning) {
	    this.serviceRunning = serviceRunning;
	  }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		// TODO Auto-generated method stub
		XPDApplication.userName=null;
	}

	public String getAndroid_id() {
		return android_id;
	}

	public void setAndroid_id(String android_id) {
		this.android_id = android_id;
	}



}
