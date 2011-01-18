package com.bukkit.authorblues.GroupUsers;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class User
{
	public String name, prefix; public Group group; 
	public RestrictedLevel restrict; public Boolean noCmdRestrict;
	public Set<String> specialCmds = new HashSet<String>();
	
	User(String uline, Map<String, Group> groups)
	{
		String[] split = uline.split(":");
		name = split[0]; group = groups.get(split[1]);
		if (group == null) group = Group.DEFAULT;
		
		int r; try { r = Integer.parseInt(split[2]); }
		catch (Exception e) { r = -1; }
		
		restrict = RestrictedLevel.fromHmodInt(Math.max(r, group.restrict.toHmodInt()));
		prefix = (split.length > 3 && !"".equals(split[3])) ? "§" + split[3] : group.prefix;
		for ( String cmd : (split.length > 4 ? split[4] : "").split(",") ) specialCmds.add(cmd);
		noCmdRestrict = specialCmds.contains("*"); 
	}

	public User(String name, Group group, RestrictedLevel restrict) {
		this.name = name; this.group = group; this.restrict = restrict;
	}
}