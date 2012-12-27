package com.mta.sadna.model;

public class User
{
	private String facebookId;
	private String facebookUserName;
	private String firstName;
	private String lastName;
	private String accessToken;
	private String gcmRegId;
	
	public String getFacebookId()
	{
		return facebookId;
	}
	public void setFacebookId(String facebookId)
	{
		this.facebookId = facebookId;
	}
	public String getFacebookUserName()
	{
		return facebookUserName;
	}
	public void setFacebookUserName(String facebookUserName)
	{
		this.facebookUserName = facebookUserName;
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
	public String getAccessToken()
    {
	    return accessToken;
    }
	public void setAccessToken(String accessToken)
    {
	    this.accessToken = accessToken;
    }
	public String getGcmRegId()
    {
	    return gcmRegId;
    }
	public void setGcmRegId(String gcmRegId)
    {
	    this.gcmRegId = gcmRegId;
    }
	
	@Override
    public int hashCode()
    {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ( (accessToken == null) ? 0 : accessToken.hashCode());
	    result = prime * result + ( (facebookId == null) ? 0 : facebookId.hashCode());
	    result = prime * result + ( (facebookUserName == null) ? 0 : facebookUserName.hashCode());
	    result = prime * result + ( (firstName == null) ? 0 : firstName.hashCode());
	    result = prime * result + ( (gcmRegId == null) ? 0 : gcmRegId.hashCode());
	    result = prime * result + ( (lastName == null) ? 0 : lastName.hashCode());
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
	    if (! (obj instanceof User))
	    {
		    return false;
	    }
	    User other = (User) obj;
	    if (accessToken == null)
	    {
		    if (other.accessToken != null)
		    {
			    return false;
		    }
	    }
	    else if (!accessToken.equals(other.accessToken))
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
	    if (facebookUserName == null)
	    {
		    if (other.facebookUserName != null)
		    {
			    return false;
		    }
	    }
	    else if (!facebookUserName.equals(other.facebookUserName))
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
	    if (gcmRegId == null)
	    {
		    if (other.gcmRegId != null)
		    {
			    return false;
		    }
	    }
	    else if (!gcmRegId.equals(other.gcmRegId))
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
	    return true;
    }
	
	
	@Override
    public String toString()
    {
	    return "User [" + (facebookId != null ? "facebookId=" + facebookId + ", " : "") +
	            (facebookUserName != null ? "facebookUserName=" + facebookUserName + ", " : "") +
	            (firstName != null ? "firstName=" + firstName + ", " : "") +
	            (lastName != null ? "lastName=" + lastName + ", " : "") +
	            (accessToken != null ? "accessToken=" + accessToken + ", " : "") +
	            (gcmRegId != null ? "gcmRegId=" + gcmRegId + ", " : "") + "]";
    }
}
