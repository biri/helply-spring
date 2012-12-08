package com.mta.sadna.activities.map;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class PostsOverlays extends ItemizedOverlay<OverlayItem>
{
	private Context context;
	private List<OverlayItem> postsOverlays = new ArrayList<OverlayItem>();

	public PostsOverlays(Drawable defaultMarker, Context context)
	{
		super(boundCenterBottom(defaultMarker));
		this.context = context;
	}

	@Override
	protected OverlayItem createItem(int index)
	{
		return postsOverlays.get(index);
	}

	@Override
	public int size()
	{
		return postsOverlays.size();
	}

	public void addOverlay(OverlayItem overlayitem, Drawable drawable)
	{
		overlayitem.setMarker(boundCenterBottom(drawable));
		this.addOverlay(overlayitem);
	}

	public void addOverlay(OverlayItem overlay)
	{
		postsOverlays.add(overlay);
		populate();
	}

	@Override
	protected boolean onTap(int index)
	{
		OverlayItem item = postsOverlays.get(index);
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getSnippet());
		dialog.show();
		return true;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow)
	{
		super.draw(canvas, mapView, false);
	}
}
