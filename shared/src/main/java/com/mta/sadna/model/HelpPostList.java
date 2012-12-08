package com.mta.sadna.model;

import java.util.List;

public class HelpPostList
{
	private List<HelpPost> helpPosts;
	
	public HelpPostList()
	{
		
	}
	
	public HelpPostList(List<HelpPost> helpPosts)
	{
		this.helpPosts = helpPosts;
	}
	
	public List<HelpPost> getHelpPosts()
    {
	    return helpPosts;
    }

	public void setHelpPosts(List<HelpPost> helpPosts)
    {
	    this.helpPosts = helpPosts;
    }

	@Override
    public int hashCode()
    {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ( (helpPosts == null) ? 0 : helpPosts.hashCode());
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
	    if (! (obj instanceof HelpPostList))
	    {
		    return false;
	    }
	    HelpPostList other = (HelpPostList) obj;
	    if (helpPosts == null)
	    {
		    if (other.helpPosts != null)
		    {
			    return false;
		    }
	    }
	    else if (!helpPosts.equals(other.helpPosts))
	    {
		    return false;
	    }
	    return true;
    }

	@Override
    public String toString()
    {
	    return "HelpPostList [" + (helpPosts != null ? "helpPosts=" + helpPosts : "") + "]";
    }
	
	
}
