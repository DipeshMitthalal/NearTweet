package com.xpd.xpdtweet;


import java.io.File;
import com.xpd.xpdtweet.Learningservice.ServiceBinder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("HandlerLeak")
public class ImageViewingActivity extends Activity {
	private ImageView imageView;
	
	public String location="";
	ChatMessage image_message;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image);
		int post = getIntent().getExtras().getInt("postid");
		String path = getIntent().getExtras().getString("path");
		Log.i("ServiceHandler", "starting");
		Log.i("MainActivity", "Binding Service");
		//Binding to service
		Intent intent = new Intent(this, Learningservice.class);

		imageView = (ImageView)findViewById(R.id.imageView1);
		TextView time = (TextView)findViewById(R.id.textCreatedAt_i);
		TextView user = (TextView)findViewById(R.id.textUser_i);
		TextView message = (TextView)findViewById(R.id.message_i);
		image_message = ((XPDApplication)getApplicationContext()).listOfPosts.get(post);
		CharSequence relTime=DateUtils.getRelativeTimeSpanString(image_message.getTime());
		time.setText(relTime);
		user.setText(image_message.getUser());
		message.setText(image_message.getMessage());
		/*if (!getApplicationContext().bindService(intent, mConnection,
				Context.BIND_AUTO_CREATE)) {
			Log.i("Main Activity", "Could not bind stupid service!");

		}*/


		File imgFile = new  File(path);
		if(imgFile.exists())
		{
			imageView.setImageURI(Uri.fromFile(imgFile));

		}
		

		Button sendtoall =(Button)findViewById(R.id.button1);
		Button gallery =(Button)findViewById(R.id.gallery);
		Button camera =(Button)findViewById(R.id.camera);
		Button goback=(Button)findViewById(R.id.goback);
		sendtoall.setVisibility(View.GONE);
		gallery.setVisibility(View.GONE);
		camera.setVisibility(View.GONE);
		goback.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onBackPressed();
			}
		});

	}


	/*@SuppressLint("HandlerLeak")
	final Handler handler = new Handler() {
			@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			//Toast.makeText(getApplicationContext(), "You have received new tweet", Toast.LENGTH_SHORT).show();
		}
	};*/

	/*Learningservice mService;
	boolean mBound = false;

	private ServiceConnection mConnection = new ServiceConnection() {
		// Called when the connection with the service is established
		public void onServiceConnected(ComponentName className, IBinder service) {
			// Because we have bound to an explicit
			// service that is running in our own process, we can
			// cast its IBinder to a concrete class and directly access it.
			Log.i("ImageViewingActivity", "onServiceConnected");

			ServiceBinder binder = (ServiceBinder) service;
			mService = binder.getService();
			//mService.service_subscribe(handler);
			mBound = true;
		}

		// Called when the connection with the service disconnects unexpectedly
		public void onServiceDisconnected(ComponentName className) {
			Log.e("Main Activity", "onServiceDisconnected");
			mBound = false;
		}
	};*/


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.image, menu);
		return true;
	}

	/*public static Bitmap decodeSampledBitmapFromFile(String picturePath, int resId,
			int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(picturePath, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(picturePath, options);
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		Log.i("InSampleSize",String.valueOf(options.inSampleSize));
		return inSampleSize;
	}*/

}
