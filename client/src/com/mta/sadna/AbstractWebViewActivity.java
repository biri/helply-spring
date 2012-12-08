package com.mta.sadna;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public abstract class AbstractWebViewActivity extends AbstractAsyncActivity
{
	private WebView webView;
	private Activity activity;
	protected static final String TAG = AbstractWebViewActivity.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
		this.activity = this;
		initWebView();
	}
	
	protected void initWebView()
	{
		this.webView = new WebView(this);
		setContentView(webView);
		webView.setWebChromeClient(new WebChromeClient()
		{
			public void onProgressChanged(WebView view, int progress)
			{
				activity.setTitle("Loading...");
				activity.setProgress(progress * 100);
				if (progress == 100)
				{
					activity.setTitle(R.string.app_name);
				}
			}
		});
	}

	protected WebView getWebView()
	{
		return this.webView;
	}
}
