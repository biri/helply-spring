package com.mta.sadna.activities;

import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.facebook.api.Facebook;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mta.sadna.AbstractAsyncActivity;
import com.mta.sadna.R;
import com.mta.sadna.activities.facebook.FacebookWebOAuthActivity;

public class SignInActivity extends AbstractAsyncActivity
{
	private ConnectionRepository connectionRepository;
	protected static final String TAG = SignInActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_activity_layout);
		this.connectionRepository = getApplicationContext().getConnectionRepository();
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		// if connect then start the app
		// if not connect then connect
		if (isConnectedToFacebook())
			startActivity(new Intent(this, MainActivity.class));
		else
			showConnectOption();
	}


	private boolean isConnectedToFacebook()
	{
		return connectionRepository.findPrimaryConnection(Facebook.class) != null;
	}
	
	private void showConnectOption()
	{
		String[] options = { "Connect" };
		ArrayAdapter<String> arrayAdapter =
		        new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options);
		ListView listView = (ListView) this.findViewById(R.id.list_view_menu_items);
		listView.setAdapter(arrayAdapter);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parentView, View childView, int position, long id)
			{
				switch (position)
				{
					case 0:
						startActivity(new Intent(parentView.getContext(), FacebookWebOAuthActivity.class));
						finish();
						break;
					default:
						break;
				}
			}
		});
	}
}
