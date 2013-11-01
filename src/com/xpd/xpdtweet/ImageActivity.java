package com.xpd.xpdtweet;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import com.xpd.xpdtweet.Learningservice.ServiceBinder;

import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.text.InputFilter.LengthFilter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;

public class ImageActivity extends Activity {
	private static final int CAMERA_RESULT = 2;
	
	private ImageView imageView;
		private static int RESULT_LOAD_IMAGE = 1;
		private String picturePath="";
	public String location="";
	TextView user, message, time;
	static Uri capturedImageUri=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image);
		Log.i("ServiceHandler", "starting");
		Log.i("MainActivity", "Binding Service");
		//Binding to service
		Intent intent = new Intent(this, Learningservice.class);

		imageView = (ImageView)findViewById(R.id.imageView1);
		time = (TextView)findViewById(R.id.textCreatedAt_i);
		user = (TextView)findViewById(R.id.textUser_i);
		message = (TextView)findViewById(R.id.message_i);
		user.setText(((XPDApplication)getApplicationContext()).getUserName());
		String mess = getIntent().getExtras().getString("message");
		if (mess.trim().isEmpty()||mess.equals(null)){
			mess="No message";
		}
		message.setText(mess);
		time.setText("Right Now");
		if (!getApplicationContext().bindService(intent, mConnection,
				Context.BIND_AUTO_CREATE)) {
			Log.i("Main Activity", "Could not bind stupid service!");

		}

//Handling Send Image
		Button send_Image =(Button)findViewById(R.id.button1);
		send_Image.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if((picturePath==null)|| picturePath.trim().isEmpty()){
				Toast.makeText(getApplicationContext(), "Please select Image", Toast.LENGTH_SHORT).show();
				}else{
				sendImage();
				Toast.makeText(getApplicationContext(), "Image is being processed and will be sent soon", Toast.LENGTH_SHORT).show();
				onBackPressed();}
				
			}
		});	
		
		Button goback =(Button)findViewById(R.id.goback);
		goback.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onBackPressed();
			}
		});

	}
	public void selectImagefromgallery(View v){
		selectImage(v);
	}
	//image taking from camera
	public void fromcamera(View v){
		Calendar cal = Calendar.getInstance();
		File file = new File(Environment.getExternalStorageDirectory(),  ("dip"+cal.getTimeInMillis()+".jpg"));
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			file.delete();
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	
		capturedImageUri = Uri.fromFile(file);
		Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		i.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
		startActivityForResult(i, CAMERA_RESULT);
	}


	private AsyncTask<String, Void, String> task;
	void sendImage(){
		task = new AsyncTask<String, Void, String>() {
			@Override
			protected String doInBackground(String... arg0) {
				byte a[]= null;
				mService.send_image(new ChatMessage(ChatMessage.IMAGE,a,picturePath,message.getText().toString(),user.getText().toString()));
				return null;
			}
		};
		task.execute();
	}



	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.image, menu);
		return true;
	}
	public void selectImage(View v){
		Log.e("GUI","Select Picture click!");
		Intent i = new Intent(
				Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, RESULT_LOAD_IMAGE);
	}
	public static Bitmap decodeSampledBitmapFromFile(String picturePath, int resId,
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
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			picturePath = cursor.getString(columnIndex);
			cursor.close();
			Log.i("gallery", picturePath);
			Bitmap bitmap = decodeSampledBitmapFromFile(picturePath, R.id.imageView1, imageView.getWidth(), imageView.getHeight());

			imageView.requestLayout();
			imageView.setImageBitmap(bitmap);				
		}
		if(requestCode == CAMERA_RESULT ) {
			Log.i("camera", "activity result");
			Log.i("camera", capturedImageUri.toString());
			picturePath = capturedImageUri.toString().substring(6);
						
			String photopath = picturePath;
		    Bitmap bmp = BitmapFactory.decodeFile(photopath);

		    Matrix matrix = new Matrix();
		    matrix.postRotate(90);
		    bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

		    FileOutputStream fOut;
		    try {
		        fOut = new FileOutputStream(picturePath);
		        bmp.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
		        fOut.flush();
		        fOut.close();

		    } catch (FileNotFoundException e1) {
		        // TODO Auto-generated catch block
		        e1.printStackTrace();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }

		
			
			
			
			
		
			Bitmap bitmap = decodeSampledBitmapFromFile(picturePath, R.id.imageView1, imageView.getWidth(), imageView.getHeight());

			imageView.requestLayout();
			imageView.setImageBitmap(bitmap);
			
		}

}

	public static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
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
	}
	
	
}
