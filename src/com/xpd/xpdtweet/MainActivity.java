package com.xpd.xpdtweet;

import java.util.ArrayList;

import com.xpd.xpdtweet.Learningservice.ServiceBinder;
import android.os.*;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity{

	public ArrayList<ChatMessage> posts;
	ListView lv;
	String username;
	private ReplyAdapter adpater;
	XPDApplication XPD;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//instantiating variables
		posts=((XPDApplication)getApplicationContext()).listOfPosts;
		setContentView(R.layout.activity_main);

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		username = ((XPDApplication)getApplicationContext()).getUserName();
		if((username==null)|| username.trim().isEmpty()){
			//Intent mIntent = new Intent(this, Preferences.class);
			// startActivity(mIntent);
			//startActivityForResult(mIntent, 2);
			startActivity(new Intent(this, Preferences.class));
		      Toast.makeText(this, "setup your nick name", Toast.LENGTH_LONG).show();
		}


		final String android_id = Secure.getString(getApplicationContext().getContentResolver(),
				Secure.ANDROID_ID);


		final EditText editText = (EditText) findViewById(R.id.editText_message);
		Button send = (Button)findViewById(R.id.createTweet);
		Button createPoll = (Button)findViewById(R.id.createPoll);
		Button imageSharing = (Button)findViewById(R.id.imageSharing);

		//Getting application context variables-global variables
		XPDApplication appState = ((XPDApplication)getApplicationContext());
		String userName = appState.getUserName();

		// instantiate our ItemAdapter class
		lv = (ListView) findViewById(R.id.list);
		adpater = new ReplyAdapter(MainActivity.this, R.layout.row, posts);
		// setListAdapter(adpater);
		lv.setAdapter(adpater);



		//creating Tweet
		send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String message = editText.getText().toString();
				//add the text in the arrayList
				//arrayList.add("c: " + message);
				if (message.trim()==""||message==null||message.trim().equals(""))
				{
					Toast.makeText(getApplicationContext(), "Please type the Tweet subject", Toast.LENGTH_SHORT).show();
				}else{
				sendTweet(message);
				editText.setText("");
				}
				//refresh the list
				//mAdapter.notifyDataSetChanged();

			}
		});

		//creating poll
		createPoll.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String message = editText.getText().toString();
				//add the text in the arrayList
				//arrayList.add("c: " + message);
				if (message.trim()==""||message==null||message.trim().equals(""))
				{
					Toast.makeText(getApplicationContext(), "Please type the Poll question/subject", Toast.LENGTH_SHORT).show();
				}else{
				Intent mIntent = new Intent(MainActivity.this, PollActivity.class);
				mIntent.putExtra("question", editText.getText().toString());
				startActivity(mIntent);	
				editText.setText("");
				}
			}
		});


		//imagesharing
		imageSharing.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String message = editText.getText().toString();
				//add the text in the arrayList
				//arrayList.add("c: " + message);
				if (message.trim()==""||message==null||message.trim().equals(""))
				{
					Toast.makeText(getApplicationContext(), "Please type the message for image", Toast.LENGTH_SHORT).show();
				}else{
				Intent mIntent = new Intent(MainActivity.this, ImageActivity.class);
				mIntent.putExtra("message", editText.getText().toString());
				editText.setText("");
				startActivity(mIntent);
			}}
		});

		lv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub

				ChatMessage post=(ChatMessage) parent.getItemAtPosition(position);
				int postID = posts.indexOf(post);
				if(post.type==ChatMessage.TWEET){

					//Toast.makeText(getApplicationContext(), String.valueOf(postID),Toast.LENGTH_SHORT).show();

					Intent mIntent = new Intent().setClass(MainActivity.this, TweetReplys.class);
					mIntent.putExtra("postid", postID);
					Log.e("Comparison","starting intent");
					if(((XPDApplication)getApplicationContext()).spammers.contains(post.getAndroidID())){
						Toast.makeText(getApplicationContext(), "user marked as spam",Toast.LENGTH_SHORT).show();
					}else
					{
						startActivity(mIntent);	
					}
				}
				if(post.type==ChatMessage.POLL){
					Toast.makeText(getApplicationContext(), String.valueOf(postID),Toast.LENGTH_SHORT).show();
					Intent mIntent = new Intent().setClass(MainActivity.this, PollReply.class);
					mIntent.putExtra("postid", postID);
					Log.e("Comparison","starting intent");
					startActivity(mIntent);	
				}
				if(post.type == ChatMessage.IMAGE){
					Intent mIntent = new Intent(MainActivity.this, ImageViewingActivity.class);
					mIntent.putExtra("postid", postID);
					mIntent.putExtra("path", post.getPath());
					Log.i("Image path", post.getPath());
					startActivity(mIntent);
				}

			}

		});

		//binding to service and starting it
		Log.i("ServiceHandler", "starting");
		Log.i("MainActivity", "Binding Service");
		Intent intent = new Intent(this, Learningservice.class);
		if (!getApplicationContext().bindService(intent, mConnection,
				Context.BIND_AUTO_CREATE)) {
			Log.i("Main Activity", "Could not bind stupid service!");
		}else{
			startService(new Intent(this, Learningservice.class));
			//Log.d("MAIN ACTIVITY","WIFI DIRECT-ENABLED");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.itemPrefs:
			startActivity(new Intent(this, Preferences.class));
			break;

		case R.id.serviceOption:
			
			if (((XPDApplication)getApplicationContext()).isServiceRunning()) {
				Log.d("MENU", "stop");
				mService.onDestroy();
				//stopService(new Intent(this, Learningservice.class));
			} else {
				Log.d("MENU", "start");
				startService(new Intent(this, Learningservice.class));
			}
			break;
		}
		return true;
	}
@Override
public boolean onPrepareOptionsMenu(Menu menu) {
	// TODO Auto-generated method stub
	MenuItem toggleItem = menu.findItem(R.id.serviceOption); // <7>
	if (((XPDApplication)getApplicationContext()).isServiceRunning()) { // <8>
		toggleItem.setTitle(R.string.serviceStop);
		 toggleItem.setIcon(android.R.drawable.ic_media_pause);
	} else { // <9>
		toggleItem.setTitle(R.string.serviceStart);
		toggleItem.setIcon(android.R.drawable.ic_media_play);
	
	}
	return true;
	//return super.onPrepareOptionsMenu(menu);
}
	/*@Override
	public boolean onMenuOpened(int featureId, Menu menu) { // <6>
		MenuItem toggleItem = menu.findItem(R.id.serviceOption); // <7>
		if (((XPDApplication)getApplicationContext()).isServiceRunning()) { // <8>
			toggleItem.setTitle(R.string.serviceStop);
			 toggleItem.setIcon(android.R.drawable.ic_media_pause);
		} else { // <9>
			toggleItem.setTitle(R.string.serviceStart);
			toggleItem.setIcon(android.R.drawable.ic_media_play);
		
		}
		return true;
	}*/

	public void sendTweet(String message) {
		if ( mBound ) {
			Log.i("MAIN ACTIVITY","FROM: "+((XPDApplication)getApplicationContext()).android_id);
			mService.sendMessage(new ChatMessage(ChatMessage.TWEET, message, ((XPDApplication)getApplicationContext()).getUserName(), ((XPDApplication)getApplicationContext()).android_id));
		} else {
			Toast.makeText(getApplicationContext(), "SERVICE NOT BOUND", Toast.LENGTH_SHORT).show();
		}			
	}

	/*
	 * This is the method that should update the screen
	 */

	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			adpater.notifyDataSetChanged();

			super.handleMessage(msg);
		}
	};

	Learningservice mService;
	boolean mBound = false;

	private ServiceConnection mConnection = new ServiceConnection() {
		// Called when the connection with the service is established
		public void onServiceConnected(ComponentName className, IBinder service) {
			// Because we have bound to an explicit
			// service that is running in our own process, we can
			// cast its IBinder to a concrete class and directly access it.
			Log.i("MainActivity", "onServiceConnected");

			ServiceBinder binder = (ServiceBinder) service;
			mService = binder.getService();
			mService.service_subscribe(handler);
			mBound = true;
		}

		// Called when the connection with the service disconnects unexpectedly
		public void onServiceDisconnected(ComponentName className) {
			Log.e("Main Activity", "onServiceDisconnected");
			mBound = false;
		}
	};


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		// check if the request code is same as what is passed  here it is 2
		if(requestCode==2)
		{
			//  binding();
		}
	}





}
