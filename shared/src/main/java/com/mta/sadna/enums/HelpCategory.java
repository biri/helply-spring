package com.mta.sadna.enums;

public enum HelpCategory
{
	stuck_with_car("Stuck with car"), 
	drunk_cant_drive("Drunk can't drive"), 
	need_ride("Need Ride");

	private String name;

	HelpCategory(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return this.name;
	}
}
