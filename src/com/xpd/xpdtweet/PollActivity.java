package com.xpd.xpdtweet;

import java.util.ArrayList;
import com.xpd.xpdtweet.Learningservice.ServiceBinder;
import android.app.Activity;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class PollActivity extends ListActivity {
	//Variables related to service
	Learningservice mService;
	boolean mBound = false;
	ArrayList<String> pollOptionList = new ArrayList<String>();
	ArrayAdapter<String> adapter;	
    String userName,question;
    TextView username,Question;
    EditText polloption;
    Button addPollOptions,b_create_poll;
    int counter=0;
    ListView lv;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pollactivity);
		
		userName = ((XPDApplication)getApplicationContext()).getUserName();
		question = getIntent().getExtras().getString("question");
		Question = (TextView)findViewById(R.id.question);
		username =(TextView)findViewById(R.id.textUser_p);
		Question.setText(question);
		username.setText(userName);
		polloption=(EditText)findViewById(R.id.editText_pollOptions);
		final String android_id = Secure.getString(getApplicationContext().getContentResolver(),
				Secure.ANDROID_ID);
		//lv=(ListView)findViewById(R.id.list_poll_options);
		adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pollOptionList);
		setListAdapter(adapter);
	    adapter.notifyDataSetChanged();
		
	  //Binding with service
	  		Log.i("MainActivity", "Binding Service");
		//Service Binding
		Intent intent = new Intent(this, Learningservice.class);
		if (!getApplicationContext().bindService(intent, mConnection,
				Context.BIND_AUTO_CREATE)) {
			Log.i("Poll Activity", "Could not bind stupid service!");
		}

//Buttons
		addPollOptions = (Button)findViewById(R.id.addPollOptions);
		b_create_poll = (Button)findViewById(R.id.b_create_poll);
		
		//Add options button configuration
		addPollOptions.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String pollOption = polloption.getText().toString();
				if(!pollOption.equals("")){
					counter++;
					pollOptionList.add(pollOption);
					adapter.notifyDataSetChanged();
					polloption.setText("");
//					showToast("Add poll button Clicked!");	
				}
				
			}
		});
		
		
		//createpoll button
		b_create_poll.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String [] options = new String[pollOptionList.size()];
				pollOptionList.toArray(options);
				Log.i("POLLC",String.valueOf(options.length));
				mService.sendMessage(new ChatMessage(ChatMessage.POLL,question,userName,android_id,options));
				//public ChatMessage(int type, String message, String user, String fromandroidID, String[] polloptions) {
				
				onBackPressed();
			}
		});
		
		//on click of item(options) in screen
		getListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
            	String option =(String) parent.getItemAtPosition(position);
            	pollOptionList.remove(option);
            	adapter.notifyDataSetChanged();
				//editText.setText("");
				counter--;
            }
        });

	}

	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			//	adpater.notifyDataSetChanged();

			/*
			 * if(msg.what==UPDATE_IMAGE){
			 * images.get(msg.arg1).setImageBitmap((Bitmap) msg.obj); }
			 */

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
