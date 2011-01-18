package com.bukkit.authorblues.GroupUsers;

import org.bukkit.block.BlockDamageLevel;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockInteractEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * GroupUsers block listener
 * @author authorblues
 */
public class GroupUsersBlockListener extends BlockListener {
    private final GroupUsers plugin;

    public GroupUsersBlockListener(final GroupUsers plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
    	String p = event.getPlayer().getName().toLowerCase();
    	RestrictedLevel r = plugin.activeUsers.get(p).restrict;
    	if (!r.hasLevel(RestrictedLevel.NORMAL)) event.setBuild(false);
    }

    @Override
    public void onBlockDamage(BlockDamageEvent event) {
    	String p = event.getPlayer().getName().toLowerCase();
    	RestrictedLevel r = plugin.activeUsers.get(p).restrict;
    	if (event.getDamageLevel() == BlockDamageLevel.BROKEN)
    		if (!r.hasLevel(RestrictedLevel.NORMAL)) event.setCancelled(true);
    }

    @Override
    public void onBlockInteract(BlockInteractEvent event) {
    	switch (event.getBlock().getType())
    	{
    		// only watching interactions with the following
	    	case FURNACE: case CHEST: case WORKBENCH: break;
	    	default: return;
    	}
    	
    	if (!event.isPlayer()) return;
    	
    	String p = ((Player) event.getEntity()).getName().toLowerCase();
    	RestrictedLevel r = plugin.activeUsers.get(p).restrict;
    	if (!r.hasLevel(RestrictedLevel.NORMAL)) event.setCancelled(true);
    }
}
