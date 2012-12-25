package com.mta.sadna.activities;

import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.facebook.api.Facebook;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

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
		setContentView(R.layout.sign_in_layout);

		//set the lables special font
		setLabelsFont();
		
		//bind the facebook connect button
		bindFacebookConnectBtn();
		
		this.connectionRepository = getApplicationContext().getConnectionRepository();
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		// if connected then start the app
		if (isConnectedToFacebook())
			startActivity(new Intent(this, PostsMapActivity.class));
	}
	
	private void setLabelsFont()
    {
		Typeface font = Typeface.createFromAsset(getAssets(), "font/rooney.ttf");
		
		TextView bigLabel = (TextView)findViewById(R.id.bigLabel);
		bigLabel.setTypeface(font);
		
		TextView smallLabel = (TextView)findViewById(R.id.smallLabel);
		smallLabel.setTypeface(font);
    }
	
	private void bindFacebookConnectBtn()
    {
		ImageButton facebookConneButton = (ImageButton)findViewById(R.id.signInBtn);
		facebookConneButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(v.getContext(), FacebookWebOAuthActivity.class));
				finish();
			}
		});
    }

	private boolean isConnectedToFacebook()
	{
		return connectionRepository.findPrimaryConnection(Facebook.class) != null;
	}
}