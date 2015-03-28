package com.nicka101.DungeonCrawler;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Created by Nick on 22/03/2015.
 */
@SuppressWarnings("unused")
public class DungeonCrawler extends JavaPlugin {

    protected static DungeonCrawler Instance;
    private static Logger logger = Bukkit.getLogger();
    protected static HashMap<Chunk, Integer> dungeonMap = new HashMap<>();

    private PluginDescriptionFile pdfFile;

    public DungeonCrawler(){
        if(Instance != null) throw new RuntimeException("Cannot instantiate this plugin more than once");
        Instance = this;
    }

    public void onEnable(){
        pdfFile = this.getDescription();
        for(World world : this.getServer().getWorlds()){
            if(world.getEnvironment() != World.Environment.NORMAL) return;
            world.getPopulators().add(new ChallengingDungeonPopulator());
            logMsg(String.format("Registered our Dungeon Populator with the world named %s as it's environment type is set to Normal", world.getName()));
        }
        this.getServer().getPluginManager().registerEvents(new DungeonEventHandler(), this);
        logMsg(String.format("Version %s has been enabled", pdfFile.getVersion()));
    }

    public void onDisable(){
        logMsg(String.format("Version %s has been disabled", pdfFile.getVersion()));
    }

    protected static void logPluginMsg(String message){
        Instance.logMsg(message);
    }

    protected void logMsg(String message){
        logger.info(String.format("[%s] %s", pdfFile.getName(), message));
    }

    protected Chunk adjacentToDungeonChunk(Chunk c){
        int cx = c.getX();
        int cz = c.getZ();
        for(Chunk chunk : dungeonMap.keySet()){
            int x = chunk.getX();
            int z = chunk.getZ();
            if((x == cx && (z == cz + 1 || z == cz - 1)) ||
                    (z == cz && (x == cx + 1 || x == cx - 1))){
                return chunk;
            }
        }
        return null;
    }

    protected void addDungeonChunk(Chunk c, int height){
        dungeonMap.put(c, height);
    }

    protected int getDungeonHeight(Chunk self){
        return dungeonMap.getOrDefault(adjacentToDungeonChunk(self), 45);
    }
}
