package com.mta.sadna.model;

import java.io.Serializable;

import com.mta.sadna.enums.HelpCategory;

public class NotificationEntity implements Serializable
{
    private static final long serialVersionUID = -987020619202424258L;
    
	private String facebookId;
	private String firstName;
	private String lastName;
	private String message;
	private HelpCategory category;
	private double latitude;
	private double longitude;
	private String triggerId;
	
	public String getFacebookId()
	{
		return facebookId;
	}
	public void setFacebookId(String facebookId)
	{
		this.facebookId = facebookId;
	}
	public String getFirstName()
	{
		return firstName;
	}
	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}
	public String getLastName()
	{
		return lastName;
	}
	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}
	public String getMessage()
	{
		return message;
	}
	public void setMessage(String message)
	{
		this.message = message;
	}
	public HelpCategory getCategory()
	{
		return category;
	}
	public void setCategory(HelpCategory category)
	{
		this.category = category;
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
	public String getTriggerId()
	{
		return triggerId;
	}
	public void setTriggerId(String triggerId)
	{
		this.triggerId = triggerId;
	}
	
	@Override
    public int hashCode()
    {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ( (category == null) ? 0 : category.hashCode());
	    result = prime * result + ( (facebookId == null) ? 0 : facebookId.hashCode());
	    result = prime * result + ( (firstName == null) ? 0 : firstName.hashCode());
	    result = prime * result + ( (lastName == null) ? 0 : lastName.hashCode());
	    long temp;
	    temp = Double.doubleToLongBits(latitude);
	    result = prime * result + (int) (temp ^ (temp >>> 32));
	    temp = Double.doubleToLongBits(longitude);
	    result = prime * result + (int) (temp ^ (temp >>> 32));
	    result = prime * result + ( (message == null) ? 0 : message.hashCode());
	    result = prime * result + ( (triggerId == null) ? 0 : triggerId.hashCode());
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
	    if (! (obj instanceof NotificationEntity))
	    {
		    return false;
	    }
	    NotificationEntity other = (NotificationEntity) obj;
	    if (category != other.category)
	    {
		    return false;
	    }
	    if (facebookId == null)
	    {
		    if (other.facebookId != null)
		    {
			    return false;
		    }
	    }
	    else if (!facebookId.equals(other.facebookId))
	    {
		    return false;
	    }
	    if (firstName == null)
	    {
		    if (other.firstName != null)
		    {
			    return false;
		    }
	    }
	    else if (!firstName.equals(other.firstName))
	    {
		    return false;
	    }
	    if (lastName == null)
	    {
		    if (other.lastName != null)
		    {
			    return false;
		    }
	    }
	    else if (!lastName.equals(other.lastName))
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
	    if (message == null)
	    {
		    if (other.message != null)
		    {
			    return false;
		    }
	    }
	    else if (!message.equals(other.message))
	    {
		    return false;
	    }
	    if (triggerId == null)
	    {
		    if (other.triggerId != null)
		    {
			    return false;
		    }
	    }
	    else if (!triggerId.equals(other.triggerId))
	    {
		    return false;
	    }
	    return true;
    }
	
	@Override
    public String toString()
    {
	    return "NotificationEntity [" + (facebookId != null ? "facebookId=" + facebookId + ", " : "") +
	            (firstName != null ? "firstName=" + firstName + ", " : "") +
	            (lastName != null ? "lastName=" + lastName + ", " : "") +
	            (message != null ? "message=" + message + ", " : "") +
	            (category != null ? "category=" + category + ", " : "") + "latitude=" + latitude + ", longitude=" +
	            longitude + ", " + (triggerId != null ? "triggerId=" + triggerId : "") + "]";
    }
}
