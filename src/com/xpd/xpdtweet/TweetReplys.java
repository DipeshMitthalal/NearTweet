package com.xpd.xpdtweet;

import java.util.ArrayList;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

import com.xpd.xpdtweet.Learningservice.ServiceBinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

@SuppressWarnings("deprecation")
public class TweetReplys extends Activity{
	int PostID;
	private ReplyAdapter adapter;

	ChatMessage cm;
	ListView lv;
	public ArrayList<ChatMessage> replys = new ArrayList<ChatMessage>();
	TweetReplys tweet;
	public ArrayList<ChatMessage> post;
	AlertDialog.Builder builder;
	EditText user_reply;
	Button send_To_Owner ;
	Button send_To_All;
	Button send_To_FB,spam_button;
	String username;
	String OwnerID;
	String message;
	private static String APP_ID = "568457673187992";
	private Facebook facebook;
	private AsyncFacebookRunner mAsyncRunner;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.tweetreply);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		username = ((XPDApplication)getApplicationContext()).getUserName();
		PostID = getIntent().getExtras().getInt("postid");
		TextView userName =(TextView)findViewById(R.id.textUser_r);
		final TextView question =(TextView)findViewById(R.id.question);
		TextView createdat =(TextView)findViewById(R.id.textCreatedAt_r);
		cm	 = ((XPDApplication)getApplicationContext()).listOfPosts.get(PostID);
		OwnerID= cm.getAndroidID();
		Log.i("TWEET REPLY","OWNERid "+OwnerID);
		replys	 = ((XPDApplication)getApplicationContext()).replys.get(cm.getPostID());

		//setting the tweet in reply screen
		userName.setText(cm.getUser());
		question.setText(cm.getMessage());
		CharSequence relTime=DateUtils.getRelativeTimeSpanString(cm.getTime());
		createdat.setText(relTime);

		/*	if ((replys == null) || replys.isEmpty()) {
			replys = new ArrayList<ChatMessage>();
		}*/

		lv = (ListView) findViewById(R.id.list_r);
		adapter = new ReplyAdapter(TweetReplys.this, R.layout.row, replys);
		// setListAdapter(adpater);
		lv.setAdapter(adapter);

		//Initializing buttons
		send_To_Owner = (Button)findViewById(R.id.sendToOwner_button);
		send_To_All = (Button)findViewById(R.id.sendToAll_button);
		user_reply = (EditText)findViewById(R.id.editText_reply);
		send_To_FB = (Button)findViewById(R.id.sendToFB_button);
		spam_button =(Button)findViewById(R.id.spam_button);

		spam_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				((XPDApplication)getApplicationContext()).spammers.add(cm.getAndroidID());
			}
		});
		facebook = new Facebook(APP_ID);
		mAsyncRunner = new AsyncFacebookRunner(facebook);
		send_To_FB.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!isInputEmpty()){
				sendReply(user_reply.getText().toString());
				//user_reply.setText("");
				loginToFacebook(user_reply.getText().toString());
				}
			}

		});


		send_To_All.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(!isInputEmpty()){
				sendReply(user_reply.getText().toString());
				user_reply.setText("");
			}}

		});

		send_To_Owner.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//sendReply(user_reply.getText().toString());
				if(!isInputEmpty()){
				Log.i("TWEET REPLY","FROM: "+((XPDApplication)getApplicationContext()).android_id);
				Log.i("TWEET REPLY","TO: "+OwnerID);
				mService.sendMessage(new ChatMessage(ChatMessage.TWEET_PERSONAL_REPLY,user_reply.getText().toString(), username, ((XPDApplication)getApplicationContext()).android_id,cm.getPostID(),OwnerID));
				replys.add(new ChatMessage(ChatMessage.TWEET_PERSONAL_REPLY,user_reply.getText().toString(), username, ((XPDApplication)getApplicationContext()).android_id,cm.getPostID(),OwnerID));
				user_reply.setText("");
			}}

		});

		question.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onCreateDialog(tweet,question.getText().toString());
			}
		});

		//binding to service and starting it

		Log.i("Tweet Reply", "Binding Service");
		Intent intent = new Intent(this, Learningservice.class);
		if (!getApplicationContext().bindService(intent, mConnection,
				Context.BIND_AUTO_CREATE)) {
			Log.i("Tweet Reply", "Could not bind stupid service!");
		}

		if (OwnerID.equalsIgnoreCase(((XPDApplication)getApplicationContext()).android_id)){
			findViewById(R.id.sendToOwner_button).setEnabled(false);
		}
		
		
		lv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub

				ChatMessage post=(ChatMessage) parent.getItemAtPosition(position);
				int postID = replys.indexOf(post);
				
				message = replys.get(postID).getMessage();
				onCreateDialog(tweet,message);

			}

		});
		

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//serviceManager.unbind();
	}



	public void sendReply(String message) {
		if ( mBound ) {
			mService.sendMessage(new ChatMessage(ChatMessage.TWEET_REPLY, message, username, ((XPDApplication)getApplicationContext()).android_id,cm.getPostID()));
		} else {
			Toast.makeText(getApplicationContext(), "SERVICE NOT BOUND", Toast.LENGTH_SHORT).show();
		}			
	}

	/*
	 * This is the method that should update the screen
	 * 
	 * @see com.example.services.NearTweetService.NotificationListener#update()
	 */

	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			adapter.notifyDataSetChanged();
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
			Log.e("TweetActivity", "onServiceDisconnected");
			mBound = false;
		}
	};
	@SuppressWarnings("deprecation")
	public void postToWall() {
		// post on user's wall.
		facebook.dialog(this, "feed", new DialogListener() {

			@Override
			public void onFacebookError(FacebookError e) {
			}

			@Override
			public void onError(DialogError e) {
			}

			@Override
			public void onComplete(Bundle values) {
			}

			@Override
			public void onCancel() {
			}
		});

	}
	public void loginToFacebook(String fbWall) {
		Log.e("FACEBOOK","MESSAGE: "+fbWall);
		final String temp1 = fbWall;
		Log.e("FACEBOOK","MESSAGE: "+temp1);
		final SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
		String access_token = mPrefs.getString("access_token", null);
		long expires = mPrefs.getLong("access_expires", 0);

		if (access_token != null) {
			facebook.setAccessToken(access_token);
			//facebook.setAccessToken(access_token);
		}

		if (expires != 0) {
			facebook.setAccessExpires(expires);
		}

		if (!facebook.isSessionValid()) {
			facebook.authorize(this,
					new String[] { "email", "publish_stream" }, Facebook.FORCE_DIALOG_AUTH,
					new DialogListener() {

				@Override
				public void onCancel() {
					// Function to handle cancel event
				}

				@Override
				public void onComplete(Bundle values) {
					// Function to handle complete event
					// Edit Preferences and update facebook acess_token
					SharedPreferences.Editor editor = mPrefs.edit();
					editor.putString("access_token",
							facebook.getAccessToken());
					editor.putLong("access_expires",
							facebook.getAccessExpires());
					editor.commit();
					//postToWall(user_reply.getText().toString());
					new postToFB().execute(temp1);
				}

				@Override
				public void onError(DialogError error) {
					// Function to handle error
					error.printStackTrace();
				}

				@Override
				public void onFacebookError(FacebookError fberror) {
					// Function to handle Facebook errors
					fberror.printStackTrace();
				}

			});
		}else
		{
			//postToWall(user_reply.getText().toString());
			//postToFB();
			new postToFB().execute(temp1);
			
		}
	}
	//private AsyncTask<String, String, String> task;
	public class  postToFB extends AsyncTask<String, String, String> {
			@Override
			protected String doInBackground(String... arg0) {
				Bundle parameters = new Bundle();
				parameters.putString("message", arg0[0]);

				parameters.putString("description", "topic share");
				try {
					facebook.request("me");
					String response = facebook.request("me/feed", parameters, "POST");
					Log.d("Tests", "got response: " + response);
					if (response == null || response.equals("") ||
							response.equals("false")) {
						publishProgress("FaceBook Error");
						//Toast.makeText(getApplicationContext(), "Blank response.",Toast.LENGTH_SHORT).show();
					}
					else {
						publishProgress("Posted on FaceBook");
						//Toast.makeText(getApplicationContext(), "Message posted on FB",Toast.LENGTH_SHORT).show();
					}
				//	finish();
				} catch (Exception e) {
					//Toast.makeText(getApplicationContext(), "failed to post",Toast.LENGTH_SHORT).show();
					e.printStackTrace();
					//finish();
				}
				return null;
			}
			@Override
			protected void onProgressUpdate(String... values) {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), values[0],Toast.LENGTH_SHORT).show();
				//super.onProgressUpdate(values);
			}
		}
		
	




	public  boolean isInputEmpty(){
		if(user_reply.getText().toString().trim().equals("")){
			Toast.makeText(getApplicationContext(), "Please type Reply", Toast.LENGTH_SHORT).show();
			return true;
		}
		else
			return false;
	}

	public  void onCreateDialog(TweetReplys tweet, String message) {
		final String temp = message;
		builder = new AlertDialog.Builder(this);
		// Get the layout inflater
		LayoutInflater inflater = this.getLayoutInflater();

		//builder.setView(inflater.inflate(R.layout.dialog_login, null));
		//final View textEntryView = inflater.inflate(R.layout.dialog_login, null);
		//builder.setView(android.R.layout.);
		builder.setTitle("RETWEET On FaceBook");
		builder.setMessage(temp); 
		
		// Add action buttons
		builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				// sign in the user ...
				Log.e("YES",temp);
				loginToFacebook(temp);
				
			}
		});
		builder .setNegativeButton("NO", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				//.this.getDialog().cancel();
				Log.e("CANCEL", "before Sent.");
			}
		});      
		AlertDialog alert = builder.create();
		alert.show();
		//return   builder.create();
	}
}
