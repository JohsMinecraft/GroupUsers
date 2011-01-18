package com.bukkit.authorblues.GroupUsers;

import java.util.*;

public class Group 
{
	public String name, prefix; public Boolean noCmdRestrict;
	public Set<String> inheritFrom = new HashSet<String>();
	public Set<String> allowedCommands = new HashSet<String>();
	public RestrictedLevel restrict;
	
	public Group(String gline)
	{
		String[] split = gline.split(":");
		name = split[0]; prefix = split[1];
		
		if (!"".equals(prefix))
			prefix = "§" + prefix;
		
		for ( String cmd : split[2].split(",") )
			allowedCommands.add(cmd);
		
		if (split.length > 3) 
			for ( String inh : split[3].split(",") ) 
				inheritFrom.add(inh);
		
		int v; try { v = split.length > 4 ? Integer.parseInt(split[4]) : 0; }
		catch (Exception e) { v = 0; }
		
		restrict = split.length < 5 ? RestrictedLevel.NORMAL :
				RestrictedLevel.fromHmodInt(v);
		noCmdRestrict = allowedCommands.contains("*");
	}
	
	public final static Group DEFAULT = new Group("default*:::default*");
}
