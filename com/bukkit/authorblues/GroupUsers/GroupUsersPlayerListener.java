package com.bukkit.authorblues.GroupUsers;

import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;

/**
 * Handle events for all Player related events
 * @author authorblues
 */
public class GroupUsersPlayerListener extends PlayerListener {
    private final GroupUsers plugin;

    public GroupUsersPlayerListener(GroupUsers instance) {
        plugin = instance;
    }

    @Override
    public void onPlayerChat(PlayerChatEvent event) {
    	String p = event.getPlayer().getName().toLowerCase();
    	if (!plugin.activeUsers.containsKey(p)) return;
    	
    	String prefix = plugin.activeUsers.get(p).prefix;
    	if (prefix == null) return;
    	
    	event.setFormat(event.getFormat().replace(
    			"%1$s", prefix + "%1$s" + ChatColor.WHITE));
    }

    @Override
    public void onPlayerCommand(PlayerChatEvent event) {
    	String[] split = event.getMessage().split(" ");
    	String command = split[0];
    	
    	if (!plugin.playerCanUseCommand(event.getPlayer(), command))
    		event.setCancelled(true);
    }

    @Override
    public void onPlayerJoin(PlayerEvent event)
    {
    	String p = event.getPlayer().getName().toLowerCase();
    	if (!plugin.users.containsKey(p)) plugin.addNewDefaultUser(p);
    	else plugin.activeUsers.put(p, plugin.users.get(p));
    		
    }

    @Override
    public void onPlayerQuit(PlayerEvent event)
    {
    	String p = event.getPlayer().getName().toLowerCase();
    	plugin.activeUsers.remove(p);
    }
}
