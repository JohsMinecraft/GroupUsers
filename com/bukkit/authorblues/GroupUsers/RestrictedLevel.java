package com.bukkit.authorblues.GroupUsers;

public enum RestrictedLevel
{
	LIMITED, NORMAL, IGNORE, ADMIN;
	
	static public RestrictedLevel fromHmodInt( int k )
	{
		for ( RestrictedLevel current : values() )
			if ( current.ordinal() == k + 1 ) return current;
		return null;
	}
	
	public int toHmodInt()
	{ return ordinal() - 1; }
	
	public Boolean hasLevel( RestrictedLevel r )
	{ return compareTo(r) >= 0; }
}