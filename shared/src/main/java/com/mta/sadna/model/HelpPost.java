package com.mta.sadna.model;

import com.mta.sadna.enums.HelpCategory;

public class HelpPost
{
	private long userId;
	private HelpCategory category;
	private String freeText;
	private double latitude;
	private double longitude;
	private long time;
	
	public HelpPost()
	{
		
	}
	
	public HelpPost(long userId, HelpCategory category, String freeText,
			double latitude, double longitude, long time)
	{
		this.userId = userId;
		this.category = category;
		this.freeText = freeText;
		this.latitude = latitude;
		this.longitude = longitude;
		this.time = time;
	}
	
	public long getUserId()
    {
    	return userId;
    }
	public void setUserId(long userId)
    {
    	this.userId = userId;
    }
	public HelpCategory getCategory()
    {
    	return category;
    }
	public void setCategory(HelpCategory category)
    {
    	this.category = category;
    }
	public String getFreeText()
    {
    	return freeText;
    }
	public void setFreeText(String freeText)
    {
    	this.freeText = freeText;
    }
	public double getLatitude()
    {
    	return latitude;
    }
	public void setLatitude(double latitude)
    {
    	this.latitude = latitude;
    }
	public double getLongitude()
    {
    	return longitude;
    }
	public void setLongitude(double longitude)
    {
    	this.longitude = longitude;
    }
	public long getTime()
    {
	    return time;
    }
	public void setTime(long time)
    {
	    this.time = time;
    }	

	@Override
    public int hashCode()
    {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ( (category == null) ? 0 : category.hashCode());
	    result = prime * result + ( (freeText == null) ? 0 : freeText.hashCode());
	    long temp;
	    temp = Double.doubleToLongBits(latitude);
	    result = prime * result + (int) (temp ^ (temp >>> 32));
	    temp = Double.doubleToLongBits(longitude);
	    result = prime * result + (int) (temp ^ (temp >>> 32));
	    result = prime * result + (int) (time ^ (time >>> 32));
	    result = prime * result + (int) (userId ^ (userId >>> 32));
	    return result;
    }

	@Override
    public boolean equals(Object obj)
    {
	    if (this == obj)
	    {
		    return true;
	    }
	    if (obj == null)
	    {
		    return false;
	    }
	    if (! (obj instanceof HelpPost))
	    {
		    return false;
	    }
	    HelpPost other = (HelpPost) obj;
	    if (category != other.category)
	    {
		    return false;
	    }
	    if (freeText == null)
	    {
		    if (other.freeText != null)
		    {
			    return false;
		    }
	    }
	    else if (!freeText.equals(other.freeText))
	    {
		    return false;
	    }
	    if (Double.doubleToLongBits(latitude) != Double.doubleToLongBits(other.latitude))
	    {
		    return false;
	    }
	    if (Double.doubleToLongBits(longitude) != Double.doubleToLongBits(other.longitude))
	    {
		    return false;
	    }
	    if (time != other.time)
	    {
		    return false;
	    }
	    if (userId != other.userId)
	    {
		    return false;
	    }
	    return true;
    }

	@Override
    public String toString()
    {
	    return "HelpPost [userId=" + userId + ", " + (category != null ? "category=" + category + ", " : "") +
	            (freeText != null ? "freeText=" + freeText + ", " : "") + "latitude=" + latitude + ", longitude=" +
	            longitude + ", time=" + time + "]";
    }
}
