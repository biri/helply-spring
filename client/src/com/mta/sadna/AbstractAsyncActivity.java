package com.mta.sadna;

import android.app.Activity;
import android.app.ProgressDialog;

public abstract class AbstractAsyncActivity extends Activity implements AsyncActivity
{
	private boolean destroyed = false;
	private ProgressDialog progressDialog;
	

	public MainApplication getApplicationContext()
	{
		return (MainApplication) super.getApplicationContext();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		this.destroyed = true;
	}

	@Override
	public void showLoadingProgressDialog()
	{
		this.showProgressDialog("Loading. Please wait...");
	}

	@Override
	public void showProgressDialog(CharSequence message)
	{
		if (this.progressDialog == null)
		{
			this.progressDialog = new ProgressDialog(this);
			this.progressDialog.setIndeterminate(true);
		}

		this.progressDialog.setMessage(message);
		this.progressDialog.show();
	}

	@Override
	public void dismissProgressDialog()
	{
		if (this.progressDialog != null && !this.destroyed)
		{
			this.progressDialog.dismiss();
		}
	}

}
