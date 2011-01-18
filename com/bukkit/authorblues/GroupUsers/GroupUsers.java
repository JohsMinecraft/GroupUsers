package com.bukkit.authorblues.GroupUsers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

/**
 * GroupUsers for Bukkit
 *
 * @author authorblues
 */
public class GroupUsers extends JavaPlugin {
	final String usersFile = "users.txt";
	final String groupsFile = "groups.txt";
	
	final Logger logger = Logger.getLogger("Minecraft");
    private final GroupUsersBlockListener blockListener = new GroupUsersBlockListener(this);
    private final GroupUsersPlayerListener playerListener = new GroupUsersPlayerListener(this);
    private final GroupUsersModifyListener modifyListener = new GroupUsersModifyListener(this);
    
    Map<String, Group> groups;
    Map<String, User> users;
    Map<String, User> activeUsers = new HashMap<String, User>();
    private Group defaultGroup;

    public GroupUsers(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin,
            ClassLoader cLoader)
    {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);
        
        try
        {
	        if (new File(groupsFile).createNewFile())
	        {
	        	BufferedWriter gfdw = new BufferedWriter(new FileWriter(groupsFile));
	        	gfdw.write("# GROUPNAME:COLOUR/PREFIX:COMMANDS:INHERITEDGROUPS:ADMIN/UNRESTRICTED");
	        	gfdw.newLine(); gfdw.close();
	        }
	        
	        if (new File(usersFile).createNewFile())
	        {
	        	BufferedWriter ufdw = new BufferedWriter(new FileWriter(usersFile));
	        	ufdw.write("# USERNAME:GROUPS:ADMIN/UNRESTRICTED:COLOUR/PREFIX:COMMANDS:IPs");
	        	ufdw.newLine(); ufdw.close();
	        }
	    }
	    catch (IOException e) {  };
        reloadUsersGroups();
    }
    
    Boolean reloadUsersGroups ()
    {
    	defaultGroup = Group.DEFAULT;
        try
        {
	        String line;
	        
	        groups = new HashMap<String, Group>();
	    	BufferedReader gf = new BufferedReader(new FileReader(groupsFile));
	        while ((line = gf.readLine()) != null)
	        {
	        	if (line.startsWith("#")) continue;
	        	if (line.trim().split(":").length < 3) continue;
	        	
	        	Group g = new Group(line);
	        	groups.put(g.name.toLowerCase(), g);
	        }
	        gf.close();

	        users = new HashMap<String, User>();
	        BufferedReader uf = new BufferedReader(new FileReader(usersFile));
	        while ((line = uf.readLine()) != null)
	        {
	        	if (line.startsWith("#")) continue;
	        	if (line.trim().split(":").length < 2) continue;
	        	
	        	User u = new User(line, groups);
	        	users.put(u.name.toLowerCase(), u);
	        }
	        uf.close();
	        
	        Set<String> inheritDone = new HashSet<String>();
	        for ( String key : groups.keySet() ) handleGroup(key, inheritDone);
        }
        catch (Exception e) { return false; }
        return true;
    }
    
    private void handleGroup ( String cgroup, Set<String> cmp )
    {
    	cgroup = cgroup.toLowerCase();
    	if (cmp.contains(cgroup)) return;
    	Group g = groups.get(cgroup); if (g == null) return;
    	
    	cmp.add(cgroup);
    	for ( String inh : g.inheritFrom )
    	{
    		if (inh.equalsIgnoreCase(cgroup)) defaultGroup = g;
    		if (!cmp.contains(inh)) handleGroup(inh, cmp);
    		Group ig = groups.get(inh); if (ig != null)
    			g.allowedCommands.addAll(ig.allowedCommands);
    	}
    }
    
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        PluginDescriptionFile pdfFile = this.getDescription();

        pm.registerEvent(Event.Type.PLAYER_COMMAND, modifyListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_COMMAND, playerListener, Priority.Highest, this);
        
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);

        pm.registerEvent(Event.Type.BLOCK_PLACED, blockListener, Priority.Highest, this);
        pm.registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, Priority.Highest, this);
        pm.registerEvent(Event.Type.BLOCK_INTERACT, blockListener, Priority.Highest, this);
        
        logger.log(Level.INFO, "[" + pdfFile.getName() + "] version [" 
        		+ pdfFile.getVersion() + "] is loaded!");
    }
    
    public void onDisable() {
    }
    
    void addNewDefaultUser( String name )
    { activeUsers.put(name, new User(name, defaultGroup, RestrictedLevel.LIMITED)); }
    
    Boolean modifyUser( String name, Map<String, String> params )
    {
    	Boolean changed = false;
    	String oname = new String(name); name = name.toLowerCase();
    	
    	if (!users.containsKey(name))
    		users.put(name, new User(name + ":default", groups));
    	User user = users.get(name);
    	if (activeUsers.containsKey(name)) activeUsers.put(name, user);
    	String gname = user.group.name;
    	
    	for ( String k : params.keySet() )
    	{
    		String v = params.get(k);
    		if (k.startsWith("g")) {
    			Boolean changedPrefix = !(user.prefix.equals(user.group.prefix));
    			Boolean changedRestrict = !(user.restrict == user.group.restrict);
    			
    			user.group = groups.get(v.toLowerCase()); gname = v;
    			if (user.group == null) user.group = Group.DEFAULT;
    			
    			if (!changedRestrict) user.restrict = user.group.restrict;
    			if (!changedPrefix) user.prefix = user.group.prefix;
    			changed = true;
    		}
    		else if (k.startsWith("p")) {
    			user.prefix = "§" + v;
    			changed = true;
    		}
    		else if (k.startsWith("c")) {
    			user.specialCmds.clear();
    			for ( String cmd : v.split(",") )
    				user.specialCmds.add(cmd);
    			user.noCmdRestrict = user.specialCmds.contains("*");
    			changed = true;
    		}
    		
    		Boolean b = Boolean.valueOf(v);
    		if (k.startsWith("a")) {
    			user.restrict = b ? RestrictedLevel.ADMIN : RestrictedLevel.NORMAL;
    			changed = true;
    		}
    		else if (k.startsWith("i")) {
    			user.restrict = b ? RestrictedLevel.IGNORE : RestrictedLevel.NORMAL;
    			changed = true;
    		}
    		else if (k.startsWith("m")) {
    			user.restrict = !b ? RestrictedLevel.LIMITED : RestrictedLevel.NORMAL;
    			changed = true;
    		}
    	}
    	
    	// REWRITE FILE
    	try
    	{
	    	BufferedReader br = new BufferedReader(new FileReader(usersFile));
	    	String line; List<String> lines = new ArrayList<String>();
	    	
	    	String cstr = "";
	    	if (user.specialCmds.size() > 0)
	    	{
	    		for ( String cmd : user.specialCmds ) cstr += ", " + cmd;
	    		cstr = cstr.substring(2);
	    	}
	    	
	    	String rline = oname + ":" + gname;
			rline += ":" + (user.restrict == user.group.restrict ? "" : (user.restrict.toHmodInt()));
			rline += ":" + (user.prefix.equals(user.group.prefix) ? "" : user.prefix);
			rline += ":" + cstr;
			while (rline.endsWith(":")) rline = rline.substring(0, rline.length() - 1);
			
	    	Boolean haveWritten = false;
	    	while ((line = br.readLine()) != null)
	    	{
	    		if (!haveWritten && line.toLowerCase().startsWith(name + ":"))
	    		{ line = rline; haveWritten = true; }
	    		lines.add(line);
	    	}
	    	
	    	if (!haveWritten) lines.add(rline);
	    	br.close();

	    	BufferedWriter bw = new BufferedWriter(new FileWriter(usersFile));
	    	for ( String ln : lines ) { bw.write(ln); bw.newLine(); }
	    	bw.close();
    	}
    	catch (Exception e) {  };
    	return changed;
    }
    
    // *****************************************************************************************
    // ****************************************** API ******************************************
    // *****************************************************************************************
    
    public List<String> getGroups()
    {
    	List<String> gs = new ArrayList<String>();
    	for ( Group g : groups.values() ) gs.add(g.name);
    	return gs;
    }
    
    public Boolean playerCanUseCommand(Player player, String command)
    {
    	String p = player.getName().toLowerCase();
    	if (!activeUsers.containsKey(p)) return false;
    	Group g = activeUsers.get(p).group;
    	
    	Set<String> s_cmds = activeUsers.get(p).specialCmds;
    	Set<String> g_cmds = g.allowedCommands;
    	
    	if (activeUsers.get(p).noCmdRestrict || g.noCmdRestrict) return true;
    	if (activeUsers.get(p).restrict.hasLevel(RestrictedLevel.IGNORE)) return true;
    	if (s_cmds.contains(command) || g_cmds.contains(command)) return true;
    	return false;
    }
    
    public Boolean isInGroup(Player player, String group)
    {
    	String p = player.getName().toLowerCase();
    	try {
	    	if (activeUsers.containsKey(p))
	    		return group.equalsIgnoreCase(activeUsers.get(p).group.name);
	    	if (users.containsKey(p))
	    		return group.equalsIgnoreCase(users.get(p).group.name);
    	} catch (Exception e) {  };
    	return false;
    }
}
