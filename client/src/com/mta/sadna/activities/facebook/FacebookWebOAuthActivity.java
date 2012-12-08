package com.mta.sadna.activities.facebook;

import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.DuplicateConnectionException;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.GrantType;
import org.springframework.social.oauth2.OAuth2Parameters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.mta.sadna.AbstractWebViewActivity;
import com.mta.sadna.activities.SignInActivity;

public class FacebookWebOAuthActivity extends AbstractWebViewActivity
{
	private ConnectionRepository connectionRepository;
	private FacebookConnectionFactory connectionFactory;
	private static final String TAG = FacebookWebOAuthActivity.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		CookieManager.getInstance().removeAllCookie();
		getWebView().getSettings().setJavaScriptEnabled(true);
		getWebView().setWebViewClient(new FacebookOAuthWebViewClient());
		this.connectionRepository = getApplicationContext().getConnectionRepository();
		this.connectionFactory = getApplicationContext().getFacebookConnectionFactory();
	}

	@Override
	public void onStart()
	{
		super.onStart();

		// display the Facebook authorization page
		getWebView().loadUrl(getAuthorizeUrl());
	}

	private String getAuthorizeUrl()
	{
		String scope = getApplicationContext().getFromProperties("facebook_scope");
		String redirectUri = getApplicationContext().getFromProperties("facebook_oauth_callback_url");
		
		/*
		 * Generate the Facebook authorization 
		 * url to be used in the browser or
		 * web view the display=touch parameter
		 * requests the mobile formatted version 
		 * of the Facebook authorization page
		 */
		OAuth2Parameters params = new OAuth2Parameters();
		params.setRedirectUri(redirectUri);
		params.setScope(scope);
		params.add("display", "touch");
		return this.connectionFactory.getOAuthOperations()
				.buildAuthorizeUrl(GrantType.IMPLICIT_GRANT, params);
	}

	/**
	 * Here in order to get the response from Facebook
	 * after log in. to get the access token
	 */
	private class FacebookOAuthWebViewClient extends WebViewClient
	{
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon)
		{
			Uri uri = Uri.parse(url);
			Log.i(TAG, url);

			//The access token is returned in
			//the URI fragment of the URL.
			final String uriFragment = uri.getFragment();

			// confirm we have the fragment, 
			//and it has an access_token parameter
			if (uriFragment != null && uriFragment.startsWith("access_token="))
			{
				Thread connectTrd = new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							//get the access token from the fragment
							String[] params = uriFragment.split("&");
							String[] accessTokenParam = params[0].split("=");
							String accessToken = accessTokenParam[1];
							Log.i(TAG, "access_token=" + accessToken);
							
							// create the connection and persist it to the repository
							AccessGrant accessGrant = new AccessGrant(accessToken);
							Connection<Facebook> connection = connectionFactory.createConnection(accessGrant);
							try
							{
								getApplicationContext().addData("accessToken", accessToken);
								connectionRepository.addConnection(connection);
							}
							catch (DuplicateConnectionException e)
							{
								Log.e(TAG, "failed to add Facebook connection." +
										" err-" + e.getMessage());
							}
						}
						catch (Exception e)
						{
							Log.e(TAG, "failed to get access token from Facebook." +
									" err-" + e.getMessage());
						}
						goBackToSignInPage();
					}
				});
				connectTrd.start();
			}

			if (uri.getQueryParameter("error") != null)
			{
				CharSequence errorReason = uri.
						getQueryParameter("error_description").replace("+", " ");
				Toast.makeText(getApplicationContext(), 
						errorReason, Toast.LENGTH_LONG).show();
				goBackToSignInPage();
			}
		}
		
		/**
		 * Redirects back to SignIn page
		 * If we logged in then it will redirect
		 * us to the main page, if not we will stay
		 * at the login page
		 */
		private void goBackToSignInPage()
		{
			startActivity(new Intent(FacebookWebOAuthActivity.this,
					SignInActivity.class));
			finish();
		}
	}
}
