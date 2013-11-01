package com.xpd.xpdtweet;

import java.util.ArrayList;
import com.xpd.xpdtweet.Learningservice.ServiceBinder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.ListView;
import android.widget.TextView;


@SuppressLint("HandlerLeak")
public class PollReply extends Activity{
	TextView poll_created_by, poll_message,poll_creation_time;
	int PollID;
	ChatMessage poll;
	ArrayList<String> optionlist;
	ArrayList<Integer> results;
	ArrayAdapter<String> adapter;
	ArrayAdapter<Integer> resultAdapter;
	Button vote;
	Button goBack;
	ListView optionView;
	ListView resultView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pollreply);
		PollID = getIntent().getExtras().getInt("postid");
		poll	 = ((XPDApplication)getApplicationContext()).listOfPosts.get(PollID);

		poll_created_by = (TextView)findViewById(R.id.poll_created_by);
		poll_created_by.setText(poll.getUser());

		poll_message =(TextView)findViewById(R.id.poll_message);
		poll_message.setText(poll.message);

		poll_creation_time=(TextView)findViewById(R.id.poll_creation_time);
		CharSequence relTime=DateUtils.getRelativeTimeSpanString(poll.getTime());
		poll_creation_time.setText(relTime);

		vote = (Button)findViewById(R.id.vote);
		goBack = (Button)findViewById(R.id.back);
		optionView = (ListView)findViewById(R.id.poll_optionsview);
		resultView = (ListView)findViewById(R.id.poll_resultview);
		optionlist = ((XPDApplication)getApplicationContext()).polloptions.get(poll.getPostID());
		results = ((XPDApplication)getApplicationContext()).pollreplys.get(poll.getPostID());
		if (poll.getIsVoted()) {  // if isVoted: doesn't show the radio button and Vote button.
			adapter=new ArrayAdapter<String>(this, R.layout.list_item, optionlist);
			vote.setVisibility(View.GONE);
			optionView.setClickable(false);
		}else {
			adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, optionlist);
			//resultView.setVisibility(View.GONE);
		}
		optionView.setAdapter(adapter);
		resultAdapter=new ArrayAdapter<Integer>(this, R.layout.list_item_2, results);
		resultView.setAdapter(resultAdapter);
		adapter.notifyDataSetChanged();
		resultAdapter.notifyDataSetChanged();



		//Binding with service
		Log.i("MainActivity", "Binding Service");
		//Service Binding
		Intent intent = new Intent(this, Learningservice.class);
		if (!getApplicationContext().bindService(intent, mConnection,
				Context.BIND_AUTO_CREATE)) {
			Log.i("Poll Activity", "Could not bind stupid service!");
		}
		
		//vote reply
		vote.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			//	showToast("Back button clicked!");
				int selection=optionView.getCheckedItemPosition();
				poll.setIsVoted(true);
				mService.sendMessage(new ChatMessage(ChatMessage.POLL_REPLY,poll.getPostID(),selection));
				//Toast.makeText(getApplicationContext(), String.valueOf(selection),Toast.LENGTH_SHORT).show();
				onBackPressed();
			}
		});
		
		//Go Back
		goBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
				
	}
		Learningservice mService;
	boolean mBound = false;
	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
		}
	};
	ServiceConnection mConnection = new ServiceConnection() {
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

}
