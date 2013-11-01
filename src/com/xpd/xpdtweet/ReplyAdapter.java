package com.xpd.xpdtweet;

import java.util.ArrayList;

import android.content.Context;
import android.provider.Settings.Secure;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
public class ReplyAdapter extends ArrayAdapter<ChatMessage> {
	// declaring our ArrayList of posts of type Chatmessage
	private ArrayList<ChatMessage> posts;
	private Context mContext;
	/* here we must override the constructor for ArrayAdapter
	 * the only variable we care about now is ArrayList<ChatMessage> objects,
	 * because it is the list of objects we want to display.
	 */
	public ReplyAdapter(Context context, int textViewResourceId, ArrayList<ChatMessage> posts) {
		super(context, textViewResourceId, posts);
		this.posts = posts;
		this.mContext = context;
	}

	/*
	 * we are overriding the getView method here - this is what defines how each
	 * list of posts will look.
	 */
	public View getView(int position, View convertView, ViewGroup parent){

		// assign the view we are converting to a local variable
		View v = convertView;

		// first check to see if the view is null. if so, we have to inflate it.
		// to inflate it basically means to render, or show, the view.
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.row, null);
		}

		/*
		 * Recall that the variable position is sent in as an argument to this method.
		 * The variable simply refers to the position of the current object in the list. (The ArrayAdapter
		 * iterates through the list we sent it)
		 * 
		 * Therefore, i refers to the current Item object.
		 */
		ChatMessage i = posts.get(position);
		String android_id = Secure.getString(mContext.getContentResolver(),Secure.ANDROID_ID);
		if (i != null) {

			// This is how you obtain a reference to the TextViews.
			// These TextViews are created in the XML files we defined.

			TextView user = (TextView) v.findViewById(R.id.textUser);
			TextView createdAt = (TextView) v.findViewById(R.id.textCreatedAt);
			TextView text = (TextView) v.findViewById(R.id.textText);
			TextView type =  (TextView) v.findViewById(R.id.textType);
			// check to see if each individual textview is null.
			// if not, assign some text!
			if (user != null){
				user.setText(i.getUser());
			}

			if (createdAt != null){
				//long timestamp = i.getTime(); 
				CharSequence relTime=DateUtils.getRelativeTimeSpanString(i.getTime());
				//CharSequence relTime = DateUtils.getRelativeTimeSpanString(convertView.getContext(), i.getTime()); 
				createdAt.setText(relTime);
			}

			if (text != null){
				text.setText(i.getMessage());
			}

			if(type != null){
				if(i.getType() == ChatMessage.IMAGE){
					type.setText("IMAGE");
				}
				if(i.getType() == ChatMessage.POLL){
					type.setText("POLL");
				}
				if(i.getType() == ChatMessage.TWEET){
					type.setText("TWEET");
				}
				if(i.getType() == ChatMessage.TWEET_REPLY){
					type.setText("Public Reply");
				}
				if(i.getType() == ChatMessage.TWEET_PERSONAL_REPLY){
					type.setText("Private Message");
				}
			}
		}
		// the view must be returned to our activity
		return v;
	}

}
