package com.mta.sadna;


public interface AsyncActivity
{
	public void showLoadingProgressDialog();

	public void showProgressDialog(CharSequence message);

	public void dismissProgressDialog();
}
