package com.xpd.xpdtweet;

import android.content.Intent;
import android.os.Bundle;

import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class Preferences extends PreferenceActivity {
	/* @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	       addPreferencesFromResource(R.xml.userpreferences);
	    }  */
	@Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.userpreferences);
          
        }
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		data.putExtra("username", "value");
		super.onActivityResult(requestCode, resultCode, data);
	}
}
