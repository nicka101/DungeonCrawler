package com.nicka101.DungeonCrawler;

import org.bukkit.World;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Created by Nick on 22/03/2015.
 */
@SuppressWarnings("unused")
public class DungeonEventHandler implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobDeath(EntityDeathEvent event){
        if(event.getEntity().getWorld().getEnvironment() != World.Environment.NORMAL) return;
        if(event.getEntity() instanceof Skeleton && ((Skeleton) event.getEntity()).getSkeletonType() == Skeleton.SkeletonType.WITHER
                || event.getEntity() instanceof Blaze
                || event.getEntity() instanceof Creeper
                || event.getEntity() instanceof PigZombie){
            event.getDrops().clear();
        }
    }
}
