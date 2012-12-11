package com.mta.sadna.activities.map;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.mta.sadna.AsyncActivity;
import com.mta.sadna.MainApplication;
import com.mta.sadna.activities.dto.PostOverlayItem;
import com.mta.sadna.model.HelpPost;

public class PostsOverlays extends ItemizedOverlay<PostOverlayItem> implements AsyncActivity
{
	private Context context;
	private MapView mapView;
	protected ProgressDialog progressDialog;
	private List<PostOverlayItem> postsOverlays = new ArrayList<PostOverlayItem>();
	protected static final String TAG = PostsOverlays.class.getSimpleName();
	
	public PostsOverlays(Drawable defaultMarker, Context context, MapView mapView)
	{
		super(boundCenterBottom(defaultMarker));
		this.context = context;
		this.mapView = mapView;
	}

	@Override
	protected PostOverlayItem createItem(int index)
	{
		return postsOverlays.get(index);
	}

	@Override
	public int size()
	{
		return postsOverlays.size();
	}

	public void addOverlay(PostOverlayItem overlayitem, Drawable drawable)
	{
		overlayitem.setMarker(boundCenterBottom(drawable));
		this.addOverlay(overlayitem);
	}

	public void addOverlay(PostOverlayItem overlay)
	{
		postsOverlays.add(overlay);
		setLastFocusedIndex(-1);
		populate();
	}
	
	public void removeOverlay(PostOverlayItem overlay)
	{
		postsOverlays.remove(overlay);
		setLastFocusedIndex(-1);
		populate();
	}

	@Override
	protected boolean onTap(int index)
	{
		final PostOverlayItem item = postsOverlays.get(index);
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getSnippet());
		
		//only if ther is a help post attached
		if (item.getHelpPost() != null)
		{
			dialog.setNeutralButton("Accept", new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					new SendAcceptNotificationTask().execute(item.getHelpPost());
					removeOverlay(item);
					mapView.invalidate();
				}
			});
		}
		
		dialog.show();
		return true;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow)
	{
		super.draw(canvas, mapView, false);
	}
	
	protected MainApplication getMainApplication()
	{
		return (MainApplication)this.context.getApplicationContext();
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
			this.progressDialog = new ProgressDialog(this.context);
			this.progressDialog.setIndeterminate(true);
		}

		this.progressDialog.setMessage(message);
		this.progressDialog.show();
	}

	@Override
	public void dismissProgressDialog()
	{
		if (this.progressDialog != null)
		{
			this.progressDialog.dismiss();
		}
	}
	
	/**
	 * Sends the user who posted the an accept messsage
	 * FacebookId - the user who will receive the message facebook id
	 * FirstName + LastName - the user who is coming to help full name
	 * Location -  the user who is coming to help current location
	 */
	private class SendAcceptNotificationTask extends AsyncTask<HelpPost, Void, Boolean>
	{
		@Override
		protected void onPreExecute()
		{
			showLoadingProgressDialog();
		}
		
		@Override
        protected Boolean doInBackground(HelpPost... arg0)
        {
			try
			{
				final String url = getMainApplication().getRestBaseUrl() 
						+ "/sendacceptnotification?" +
						"facebookId=" + arg0[0].getUserId() + "&" +
						"firstName=" + getMainApplication().getData("firstName") + "&" +
						"lastName=" + getMainApplication().getData("lastName") + "&" +
						"latitude=" + 0 + "&" +
						"longitude=" + 0 + "&" +
						"triggerId=" + arg0[0].getTriggerId();

				RestTemplate restTemplate = new RestTemplate();
				restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
				return restTemplate.getForObject(new URI(url), Boolean.class);
			}
			catch (Exception e)
			{
				Log.e(TAG, e.getMessage(), e);
			}
			return false;
        }
		
		@Override
		protected void onPostExecute(Boolean result)
		{
			dismissProgressDialog();
			
			String firstName = getMainApplication().getData("firstName").toString();
			String lastName = getMainApplication().getData("lastName").toString();
			Toast.makeText(context, "Accept message has " +
					"been sent to " + firstName + " " + lastName,
					Toast.LENGTH_LONG).show();
		}
	}
}
