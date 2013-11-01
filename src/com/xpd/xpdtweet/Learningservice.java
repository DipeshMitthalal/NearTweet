package com.xpd.xpdtweet;



import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pBroadcast;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pDevice;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pDeviceList;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pInfo;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pManager;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pManager.Channel;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pManager.GroupInfoListener;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pManager.PeerListListener;
import pt.utl.ist.cmov.wifidirect.service.SimWifiP2pService;
import pt.utl.ist.cmov.wifidirect.sockets.SimWifiP2pSocket;
import pt.utl.ist.cmov.wifidirect.sockets.SimWifiP2pSocketManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.*;
import android.graphics.Bitmap.CompressFormat;
import android.os.*;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.*;
import android.widget.Toast;


public class Learningservice extends Service implements 
PeerListListener, GroupInfoListener  {
	private static HashSet<ObjectOutputStream> writers = new HashSet<ObjectOutputStream>();
	public ArrayList<ChatMessage> pending=new ArrayList<ChatMessage>();
	private Set<String> connectedClients = new TreeSet<String>();
	private static String TAG = "XPD_SERVICE";
	private SimWifiP2pManager mManager = null;
	private Channel mChannel = null;
	private Messenger mService = null;
	private boolean mBound = false;
	static Bitmap	image;
	public static String username="NOTLOGGED";
	private boolean runFlag = false;
	public static final int WHOISIN = 0, MESSAGE = 1, LOGOUT = 2, LOGIN = 3, IMAGE = 4, SYSTEM =5; 
	public static final int LOGINSUCCESFUL =6, POLL =7, POLL_REPLY = 8, TWEET = 9, TWEET_REPLY =10;
	public static final int TWEET_PERSONAL_REPLY=11;
	public static String SERVERIP = "10.0.2.2"; //your computer IP address
	public static int SERVERPORT = 6677;
	int sizeofmHomeGroups = 0;
	String Androidid;
	Socket socket;
	PrintWriter out;
	BufferedReader in;
	ChatMessage cm;
	private ObjectInputStream Oin;		// to read from the socket
	private ObjectOutputStream Oout;   // to write to the socket 
	public ArrayList<ChatMessage> listOfPosts;
	public Map<String, ArrayList<ChatMessage>> replys;
	public Map<String, ArrayList<ChatMessage>> replys_owner;
	public Map<String, ArrayList<String>> polloptions;
	public Map<String, ArrayList<Integer>> pollreplys;
	public ServerSocket mSrvSocket = null;
	//public Socket mCliSocket = null;
	public  SimWifiP2pSocket mCliSocket = null;
	public Boolean ISGO = false;
	public Boolean needSync = false;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		//run();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		//	username = preferences.getString("userName", "");
		SERVERIP = preferences.getString("ipAddress", "10.0.2.2");
		SERVERPORT = Integer.parseInt(preferences.getString("portNumber","6677"));
		Androidid=((XPDApplication)getApplicationContext()).android_id;
		listOfPosts=((XPDApplication)getApplicationContext()).listOfPosts;
		replys = ((XPDApplication)getApplicationContext()).replys;
		username = ((XPDApplication)getApplicationContext()).getUserName();
		polloptions= ((XPDApplication)getApplicationContext()).polloptions;
		pollreplys = ((XPDApplication)getApplicationContext()).pollreplys;
		replys_owner = ((XPDApplication)getApplicationContext()).personalreplys;
		// initialize the WDSim API
		SimWifiP2pSocketManager.Init(getApplicationContext());

		// register broadcast receiver
		IntentFilter filter = new IntentFilter();
		filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
		filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
		filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
		filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
		SimWifiP2pBroadcastReceiver receiver = new SimWifiP2pBroadcastReceiver(this);
		registerReceiver(receiver, filter);
	}



	private ServiceConnection mConnection = new ServiceConnection() {
		// callbacks for service binding, passed to bindService()

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);
			mManager = new SimWifiP2pManager(mService);
			mChannel = mManager.initialize(getApplication(), getMainLooper(), null);
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mService = null;
			mManager = null;
			mChannel = null;
			mBound = false;
		}
	};

	/*public void onStopService() {
		// TODO Auto-generated method stub
		mRun = false;
	}*/



	//sending tweets to network
	public void sendMessage(ChatMessage msg) {
		// TODO Auto-generated method stub
		if ((msg.type == TWEET)||(msg.type==TWEET_REPLY)||msg.type == TWEET_PERSONAL_REPLY||msg.type==POLL||msg.type==POLL_REPLY||msg.type==ChatMessage.IMAGE) {
			if(writers.size()!= 0){
				write(msg);
				processInputMessage(msg);
			}
			else
			{
				processInputMessage(msg);
				pending.add(msg);
				Toast.makeText(getApplicationContext(), "Your Tweet will be delivered when network becomes available", Toast.LENGTH_SHORT).show();
			}

		}



	}

	public void sendPendingTweets(){

		if(writers.size()!= 0){	
			Iterator<ChatMessage> i = pending.iterator();
			while (i.hasNext()) {
				ChatMessage ls = i.next();
				write(ls);
				//	processInputMessage(ls);
				//... all your other code which uses ls...
				i.remove();

			}       		
		}
		else
		{
			Toast.makeText(getApplicationContext(), "Your Tweet will be delivered when network becomes available", Toast.LENGTH_SHORT).show();
		}

	}

	public void send_image(ChatMessage image_object){
		Bitmap bitmap = BitmapFactory.decodeFile(image_object.getPath());
		ByteArrayOutputStream blob = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 0 /*ignored for PNG*/, blob);
		byte[] bitmapdata = blob.toByteArray();
		image_object.setImage(bitmapdata);
		Log.d("SERVICE","Sending Image");
		sendMessage(image_object);
		bitmap.recycle();
	}

	public void sav1(ChatMessage imageObject){
		Calendar cal = Calendar.getInstance();
		File photo=new File(Environment.getExternalStorageDirectory(), ("IMG_"+cal.getTimeInMillis()+".jpg"));

		if (photo.exists()) {
			photo.delete();
		}

		try {
			FileOutputStream fos=new FileOutputStream(photo.getPath());

			try {
				Log.i("IMAGE SAVE", "1");
				fos.write(imageObject.getImage());
				Log.i("IMAGE SAVE", "2");
				fos.close();
				MediaStore.Images.Media.insertImage(getContentResolver(),photo.getAbsolutePath(),photo.getName(),"New Image Recieved");
				imageObject.setPath(photo.getAbsolutePath());
				imageObject.setImage(null);
				((XPDApplication)getApplicationContext()).listOfPosts.add(imageObject);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{

		}
	}



	public void processInputMessage(ChatMessage message){
		if (message != null	) {

			if(message.getType()==ChatMessage.IMAGE){
				sav1(message);
				Log.i("IMAGE SAVE", "SAVED");	
			}
			if(message.getType()==ChatMessage.TWEET){

				((XPDApplication)getApplicationContext()).listOfPosts.add(message);
				ArrayList<ChatMessage> rep = new ArrayList<ChatMessage>();
				ArrayList<ChatMessage> rep_owner = new ArrayList<ChatMessage>();
				replys.put(message.getPostID(), rep);
				replys_owner.put(message.getPostID(), rep_owner);
			}
			if(message.getType()==ChatMessage.TWEET_REPLY){
				ArrayList<ChatMessage> rep= replys.get(message.getPostID());
				if(rep != null)
					rep.add(message);

			}

			if(message.getType()==ChatMessage.TWEET_PERSONAL_REPLY){
				Log.d("SERVICE:PR:FROM", message.getToAndroidID());
				Log.d("SERVICE:PR:TO", Androidid);
				if(message.getToAndroidID().equals(Androidid)){
					ArrayList<ChatMessage> rep= replys.get(message.getPostID());
					if(rep != null)
						rep.add(message);
				}
				ArrayList<ChatMessage> rep_owner = replys_owner.get(message.getPostID());
				rep_owner.add(message);
			}
			if(message.getType()==ChatMessage.POLL){
				//when you get new poll store ooptions and initialize results to zero
				String[] options=message.getPolloptions(); 
				Log.i("Service-options", String.valueOf(options.length));
				((XPDApplication)getApplicationContext()).listOfPosts.add(message);
				ArrayList<String> polloptions_list= new ArrayList<String>();
				ArrayList<Integer> pollresults= new ArrayList<Integer>();
				Integer[] results=new Integer[options.length];

				for(int i=0;i<results.length;i++)
					results[i] = 0;

				//converting array to list and store it in application context list
				Log.i("Service-result", String.valueOf(results.length));
				Collections.addAll(polloptions_list,options);
				Collections.addAll(pollresults,results);
				polloptions.put(message.getPostID(),polloptions_list);
				pollreplys.put(message.getPostID(), pollresults);
			}

			if(message.getType()==ChatMessage.POLL_REPLY){


				ArrayList<Integer> result=	((XPDApplication)getApplicationContext()).pollreplys.get(message.getPostID());
				if(result != null){
					Integer  temp =result.get(message.getSelection());
					result.set(message.getSelection(), temp+1);
				}
			}

			if(message.getType()==ChatMessage.SYNC){
				if(ISGO){
					//clientrequestedsync();
					Log.d("TAG", "you are owner send ur data");
				}
			}
			if(message.getType()==ChatMessage.SYNC_REPLY){
				mergeData(message);
			}

			for (Handler l : listeneres) {
				Log.i("Service", "Notifying Listener...");
				l.sendMessage( l.obtainMessage() );
			}
		}

		else{
			Log.e("INPUT","NULL");
		}
		message = null;
	}
	private void mergeData(ChatMessage message2) {
		// TODO Auto-generated method stub

		if(needSync){
			Log.d(TAG, "you have got sync reply");
			Map<String, ArrayList<ChatMessage>> replys_new = new HashMap<String, ArrayList<ChatMessage>>();
			Map<String, ArrayList<ChatMessage>> personal_replys_new = new HashMap<String, ArrayList<ChatMessage>>();
			Map<String, ArrayList<Integer>> pollreplys_new = new HashMap<String, ArrayList<Integer>>();
			replys_new = message2.getListofReplys();
			pollreplys_new = message2.getPollreplys();
			personal_replys_new = message2.getPersonal_replys();
			//Removing Duplicates;
			for (String postID : replys_new.keySet()) {
				//step1:iterating thru new reply map
				//now you have a post id and its replys from GO
				if (replys.containsKey(postID)){
					ArrayList<ChatMessage> my_replys = replys.get(postID);
					ArrayList<ChatMessage> go_replys = replys_new.get(postID);
					ArrayList<ChatMessage> go_replys_personal = personal_replys_new.get(postID);
					//remove duplicates from 
					System.out.println("size of my_reply"+my_replys.size());
					System.out.println("size of go_reply"+go_replys.size());
					System.out.println("size of go_reply_personal"+go_replys_personal.size());
					go_replys.removeAll(my_replys);
					go_replys_personal.removeAll(my_replys);
					System.out.println("after removing dulicates");
					System.out.println("size of my_reply"+my_replys.size());
					System.out.println("size of go_reply"+go_replys.size());
					my_replys.addAll(go_replys);
					go_replys_personal.removeAll(my_replys);
					my_replys.addAll(go_replys_personal);
					System.out.println("after add all");
					System.out.println("size of my_reply"+my_replys.size());
					System.out.println("size of go_reply"+go_replys.size());					
					Iterator<ChatMessage> i = my_replys.iterator();
					while (i.hasNext()) {
						boolean remove = false;
						ChatMessage cm = i.next();
						if((cm.getType()==ChatMessage.TWEET_PERSONAL_REPLY) 
								&& 
								(!cm.getToAndroidID().equals(((XPDApplication)getApplicationContext()).android_id)))
						{
							remove = true;
						}
						if(remove)
							i.remove();
					}
				}
			}
			
			for(String pollID:pollreplys_new.keySet()){
				if (pollreplys.containsKey(pollID)){			
					pollreplys.put(pollID, pollreplys_new.get(pollID));
				}
			}

			//



			Log.d(TAG,"SYNC DATA:"+ replys);
		}
		needSync = false;
		if(pending.size()>=1){
			//Toast.makeText(getApplicationContext(), "sendingpendingtweets", Toast.LENGTH_SHORT).show();
			sendPendingTweets();
			Log.d(TAG,"sending pending tweets");
		}
	}



	//END
	// Binder given to clients
	private final IBinder mBinder = new ServiceBinder();
	public class ServiceBinder extends Binder {
		public Learningservice getService() {
			Log.i("Service", "Returning Service");
			// Return this instance of LocalService so clients can call public methods
			return Learningservice.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i("SERVICE", "onBind returning IBinder");
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.i("SERVICE", "onUnbind returning false");
		return false;
	}
	private List<Handler> listeneres = new ArrayList<Handler>();

	public void service_subscribe(Handler listener) {
		listeneres.add(listener);
	}

	public void service_unsubscribe(Handler listener) {
		listeneres.remove(listener);
	}

	public void getgroupinfo(){
		mManager.requestGroupInfo(mChannel, (GroupInfoListener) Learningservice.this);

	}
	@Override
	public void onGroupInfoAvailable(SimWifiP2pDeviceList devices, 
			SimWifiP2pInfo groupInfo) {

		TreeMap<String,ArrayList<String>> mGroups;
		Set<String> mHomeGroups;
		mGroups =  groupInfo.getmGroups();
		mHomeGroups = groupInfo.getmHomeGroups();
		sizeofmHomeGroups = mHomeGroups.size();
		Log.d(TAG, "Groups:" + mGroups.toString());
		Log.d(TAG, "HOMEGroups:" + mHomeGroups.toString());
		Log.d(TAG, "GroupOwner:" + groupInfo.askIsGO());
		Log.d("CLIENT", ""+groupInfo.askIsClient());
		Log.d("SERVER", ""+groupInfo.askIsGO());
		Log.d("DEVICES",groupInfo.getDevicesInNetwork().toString());

		if(groupInfo.askIsClient()){
			ISGO = false;
			//if i am a client, i will connect to group owner
			for(String i : mHomeGroups){
				SimWifiP2pDevice d =  devices.getByName(i);
				String GO_IP= d.getVirtIp();
				int GO_PORT = d.getVirtPort();
				Log.d("virtualIP",d.getVirtIp()+d.getVirtPort());
				Log.d("REALIP",d.getRealIp()+d.getRealPort());

				Log.d("CLIENT", "connecting:"+i);
				if(!connectedClients.contains(i)){
					Log.d("CLIENT", "connecting to:"+i);
					multipleGOconnect g = new multipleGOconnect(GO_IP, GO_PORT, i, this, writers, connectedClients);
					g.start();
					//once i connect  i synchronize and then i send pending tweets
					while (writers.size()==0){

					}
					Log.d(TAG,"SIZE OF WRITER"+writers.size());
					//sync();

				}
				else
				{
					Log.d("CLIENT", "already connected to:"+i);
				}


			}

		}
		if(groupInfo.askIsGO()){
			ISGO = true;
			//i am group owner and i have to listen to ppl and broadcast including me
			String owner_name = groupInfo.getDeviceName();
			SimWifiP2pDevice d =  devices.getByName(owner_name);
			String GO_IP= d.getVirtIp();
			int GO_PORT = d.getVirtPort();
			if(!connectedClients.contains(owner_name)){
				multipleGOconnect g = new multipleGOconnect(GO_IP, GO_PORT, owner_name, this, writers,connectedClients);
				g.start();
				Log.d("SERVER", "staring own client");
			}
			else{
				if(connectedClients.size()==1 && connectedClients.contains(owner_name) && groupInfo.getDevicesInNetwork().isEmpty()){
					ISGO = false;
					for (ObjectOutputStream writer : writers) {
						try {
							writer.close();
							Log.d(TAG, "you are only alone.we dont need you");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//connectedClients.remove(owner_name);

					}
				}
				else
					Log.d("SERVER", "own client already open");
			}
		}

	}

	@Override
	public void onPeersAvailable(SimWifiP2pDeviceList peers) {
		// TODO Auto-generated method stub

	}


	/*
	 * Classes implementing chat message exchange
	 */
	void IncommingCommTask(){
		new Thread() {
			public void run() {
				Log.d(TAG, "IncommingCommTask started (" + this.hashCode() + ").");

				try {
					/*mSrvSocket = new SimWifiP2pSocketServer(
							Integer.parseInt(getString(R.string.port)));*/
					mSrvSocket =  new ServerSocket(10001);
				} catch (IOException e) {
					e.printStackTrace();
				}

				int id = 0;
				try {
					while(true){
						Log.d("accetp", "accepting conection");
						Socket sock = mSrvSocket.accept();
						ClientServiceThread cliThread = new ClientServiceThread(sock, id++,Learningservice.this);
						cliThread.start();
						Log.d("accetp", "finished-accepting conection");
					}

				} catch (IOException e) {
					Log.d("Error accepting socket:", e.getMessage());
					//e.printStackTrace();
				}
			}}.start();
	}


	public void socket_closing(SimWifiP2pSocket s){
		if (!s.isClosed()) {
			try {
				s.close();
				Log.d(TAG, "closing socket");
				Oout = null;
				Oin = null;
			}
			catch (Exception e) {
				Log.d("Error closing socket:", e.getMessage());
			}
		}
		else
		{
			Log.d(TAG, "closing socket:Already closed");
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if (!runFlag) {
			this.runFlag = true;

			// ((XPDApplication) super.getApplication()).setServiceRunning(true);
			//bind service
			bindService();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void bindService() {
		// TODO Auto-generated method stub
		if(!mBound){
			((XPDApplication)getApplicationContext()).setServiceRunning(true);
			Intent intent = new Intent(getApplicationContext(), SimWifiP2pService.class);
			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			mBound = true;
			IncommingCommTask();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
		this.runFlag = false;
		((XPDApplication)getApplicationContext()).setServiceRunning(false);
		//  ((XPDApplication) super.getApplication()).setServiceRunning(false);

		Log.i("SERVICE", "Service Stopped.");
		socketgarbagecollection();
		this.stopSelf();
	}

	private void socketgarbagecollection() {
		// TODO Auto-generated method stub
		if (mSrvSocket != null) {
			try {
				mSrvSocket.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		mSrvSocket = null;
	}

	private void synchronize_data() {
		// TODO Auto-generated method stub
		Map<String, ArrayList<ChatMessage>> listofReplys_temp = null;
		Map<String, ArrayList<ChatMessage>> personalReplys_temp = null;
		Map<String, ArrayList<Integer>> pollreplys_temp = null;
		ChatMessage syn = new ChatMessage(ChatMessage.SYNC, listofReplys_temp, personalReplys_temp,pollreplys_temp);
		Log.d(TAG, "1:sending SYN message");
		if(writers.size()!= 0){
			write(syn);
			Log.d(TAG, "2:sending SYN message");
		}
		needSync = true;
	}

	//end

	synchronized void write(ChatMessage msg){
		for (ObjectOutputStream writer : writers) {
			try {
				writer.writeObject(msg);
				writer.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


		}
	}

	/*public void clientrequestedsync() {
		//send your data
		// TODO Auto-generated method stub
		//Toast.makeText(getApplicationContext(), "i send sync update to client", Toast.LENGTH_SHORT).show();
		ChatMessage synr = new ChatMessage(ChatMessage.SYNC_REPLY,replys,pollreplys); 
		Log.d(TAG,"i am owner hence i send data");
		write(synr);
	}*/

	public ChatMessage clientrequestedsync() {
		//send your data
		// TODO Auto-generated method stub
		//Toast.makeText(getApplicationContext(), "i send sync update to client", Toast.LENGTH_SHORT).show();
		ChatMessage synr = new ChatMessage(ChatMessage.SYNC_REPLY,replys,replys_owner,pollreplys); 
		Log.d(TAG,"i am owner hence i send data");

		return synr;
		//write(synr);
	}

	public void sync() {
		// TODO Auto-generated method stub
		if((sizeofmHomeGroups==1)&&((replys.size()>0)||polloptions.size()>0||pollreplys.size()>0)){
			Log.d(TAG,"calling sync data");
			synchronize_data();
		}else{
		if(pending.size()>=1){
			//Toast.makeText(getApplicationContext(), "sendingpendingtweets", Toast.LENGTH_SHORT).show();
			sendPendingTweets();
			Log.d(TAG,"sent pending tweets");
		}}
	}
}


