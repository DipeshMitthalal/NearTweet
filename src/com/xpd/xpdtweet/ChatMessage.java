package com.xpd.xpdtweet;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;
/*
 * This class defines the different type of messages that will be exchanged between the
 * Clients and the Server. 
 * When talking from a Android Client to a Java Server a lot easier to pass Java objects, no 
 * need to count bytes or to wait for a line feed at the end of the frame
 */
public class ChatMessage implements Serializable {

	protected static final long serialVersionUID = 1112122200L;

	// The different types of message sent by the Client
	// WHOISIN to receive the list of the users connected
	// MESSAGE an ordinary message
	// LOGOUT to disconnect from the Server

	public static final int WHOISIN = 0, MESSAGE = 1, LOGOUT = 2, LOGIN = 3, IMAGE = 4, SYSTEM =5; 
	public static final int LOGINSUCCESFUL =6, POLL =7, POLL_REPLY = 8, TWEET = 9, TWEET_REPLY =10;
	public static final int TWEET_PERSONAL_REPLY=11, SYNC = 12, SYNC_REPLY =13;
	protected int type;
	protected String message;
	protected String user;
	protected String to;
	String postID, path;
	Timestamp timestamp;
	Long time;
	byte[] image;
	String androidID;
	String toAndroidID;
	String[] polloptions;
	Boolean isVoted;
	int selection;
	public Map<String, ArrayList<ChatMessage>> listofReplys;
	public Map<String, ArrayList<String>> polloption_list;
	public Map<String, ArrayList<Integer>> pollreplys;
	public Map<String, ArrayList<ChatMessage>>personal_replys;
	//this is for tweet creation
	public ChatMessage(int type, String message, String user, String androidID) {
		this.type = type;
		this.message = message;
		this.user = user;
		this.time = getTimestamp();
		this.postID =time.toString()+androidID;
		this.androidID = androidID;

	}
	//this is for tweet reply
	public ChatMessage(int type, String message, String user, String androidID, String postID) {
		this.type = type;
		this.message = message;
		this.user = user;
		this.time = getTimestamp();
		this.postID =postID;
		this.androidID = androidID;

	}

	//this is for tweet -personal reply
	public ChatMessage(int type, String message, String user, String fromandroidID, String postID, String toAndroidID) {
		this.type = type;
		this.message = message;
		this.user = user;
		this.time = getTimestamp();
		this.postID =postID;
		this.androidID = fromandroidID;
		this.toAndroidID = toAndroidID;

	}
	//poll creation
	public ChatMessage(int type, String message, String user, String fromandroidID, String[] polloptions) {
		this.type = type;
		this.message = message;
		this.user = user;
		this.time = getTimestamp();
		this.postID =this.time+user;
		this.androidID = fromandroidID;
		this.polloptions = polloptions;
		Log.i("CHATMESSAGE",String.valueOf(polloptions.length));
		this.isVoted=false;

	}
	//picture
	public ChatMessage(int type, byte[] image, String path, String message,String user) {
		this.type = type;
		this.image = image;
		this.path = path;
		this.message = message;
		this.user = user;
		this.time = getTimestamp();
	}


	//poll reply
	public ChatMessage(int type, String postID, int selection) {
		// TODO Auto-generated constructor stub
		this.type = type;
		this.postID=postID;
		this.selection=selection;
	}
	//synchronize if needed
	public ChatMessage (int type, Map<String, ArrayList<ChatMessage>> listofReplys,
			 Map<String, ArrayList<ChatMessage>> personal_replys,Map<String, ArrayList<Integer>> pollreplys){

		this.type = type;
		this.listofReplys = listofReplys;
		this.personal_replys = personal_replys;
		this.pollreplys = pollreplys;
		System.out.println("SYN OBJECT CREATED");
	}
	// getters
	public int getType() {
		return type;
	}
	public String getMessage() {
		return message;
	}

	public String getUser() {
		return user;
	}
	public byte[] getImage() {
		return image;
	}
	public Long getTimestamp() {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		long tsTime1 = timestamp.getTime();
		return tsTime1;
	}
	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public String getPostID() {
		return postID;
	}

	public void setPostID(String postID) {
		this.postID = postID;
	}

	public String getAndroidID() {
		return androidID;
	}

	public void setAndroidID(String androidID) {
		this.androidID = androidID;
	}
	public String getToAndroidID() {
		return toAndroidID;
	}
	public void setToAndroidID(String toAndroidID) {
		this.toAndroidID = toAndroidID;
	}
	public String[] getPolloptions() {
		return polloptions;
	}
	public void setPolloptions(String[] polloptions) {
		this.polloptions = polloptions;
	}
	public Boolean getIsVoted() {
		return isVoted;
	}
	public void setIsVoted(Boolean isVoted) {
		this.isVoted = isVoted;
	}
	public int getSelection() {
		return selection;
	}
	public void setSelection(int selection) {
		this.selection = selection;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public void setImage(byte[] image) {
		this.image = image;
	}
	public Map<String, ArrayList<ChatMessage>> getListofReplys() {
		return listofReplys;
	}
	public void setListofReplys(Map<String, ArrayList<ChatMessage>> listofReplys) {
		this.listofReplys = listofReplys;
	}

	public Map<String, ArrayList<Integer>> getPollreplys() {
		return pollreplys;
	}
	public void setPollreplys(Map<String, ArrayList<Integer>> pollreplys) {
		this.pollreplys = pollreplys;
	}
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		
			ChatMessage temp = (ChatMessage) obj;
			System.out.println("this"+this.getMessage());
			System.out.println("obj"+temp.getMessage());
			if((this.getMessage().equals(temp.getMessage()))){
				System.out.println("2:it is equal");
				return true;
			}
		
		return false;
	}
	@Override
	public int hashCode() {
		System.out.println("hash checked");
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((postID == null) ? 0 : postID.hashCode());
		result = (int) (prime * result + time);
		result = prime * result
				+ ((time == null) ? 0 : message.hashCode());
		return result;
	}
	public Map<String, ArrayList<ChatMessage>> getPersonal_replys() {
		return personal_replys;
	}
	public void setPersonal_replys(
			Map<String, ArrayList<ChatMessage>> personal_replys) {
		this.personal_replys = personal_replys;
	}





}
