package com.mta.sadna.activities.dto;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;
import com.mta.sadna.model.HelpPost;

public class PostOverlayItem extends OverlayItem
{
	private HelpPost helpPost;
	
	public PostOverlayItem(GeoPoint point, String subject, String text)
    {
	    super(point, subject, text);
    }
	
	public PostOverlayItem(GeoPoint point, String subject,
			String text, HelpPost helpPost)
    {
	    this(point, subject, text);
	    this.helpPost = helpPost;
    }

	public HelpPost getHelpPost()
    {
	    return helpPost;
    }

	public void setHelpPost(HelpPost helpPost)
    {
	    this.helpPost = helpPost;
    }
}
