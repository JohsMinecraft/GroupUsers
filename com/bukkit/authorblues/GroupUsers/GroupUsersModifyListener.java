package com.bukkit.authorblues.GroupUsers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;

/**
 * Handle events for all Player related events
 * @author authorblues
 */
public class GroupUsersModifyListener extends PlayerListener {
    private final GroupUsers plugin;

    public GroupUsersModifyListener(GroupUsers instance) {
        plugin = instance;
    }

    @Override
    public void onPlayerCommand(PlayerChatEvent event) {
    	if (event.isCancelled()) return;
    	
    	Player p = event.getPlayer();
    	String[] split = event.getMessage().split(" ");
    	String command = split[0];
    	
    	// fall through, though I shouldn't HAVE to do this! (#54)
    	if (!plugin.playerCanUseCommand(p, command)) return;
    	
    	if ("/modify".equalsIgnoreCase(command) && split.length > 1)
    	{
    		String mplayer = split[1];
    		Map<String, String> params = new HashMap<String, String>();
    		
    		for (int i = 2; i < split.length; ++i) {
    			String[] pair = split[i].split(":");
    			params.put(pair[0], pair[1]);
    		}
    		
    		if (plugin.modifyUser(mplayer, params))
    			p.sendMessage(ChatColor.RED + "Successfully changed player: " + ChatColor.GRAY + mplayer);
    		else p.sendMessage(ChatColor.RED + "No changes made!");
        	event.setCancelled(true);
    	}
    }
}