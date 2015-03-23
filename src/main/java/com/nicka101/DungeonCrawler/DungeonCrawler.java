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
    protected static HashMap<ChunkCoords, Integer> dungeonMap = new HashMap<>();

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

    protected ChunkCoords adjacentToDungeonChunk(ChunkCoords c){
        for(ChunkCoords chunk : dungeonMap.keySet()){
            if((chunk.x == c.x && (chunk.z == c.z + 1 || chunk.z == c.z - 1)) ||
                    (chunk.z == c.z && (chunk.x == c.x + 1 || chunk.x == c.x - 1))){
                return chunk;
            }
        }
        return null;
    }

    protected void addDungeonChunk(Chunk c, int height){
        dungeonMap.put(new ChunkCoords(c), height);
    }

    protected int getDungeonHeight(ChunkCoords self){
        return dungeonMap.getOrDefault(adjacentToDungeonChunk(self), 45);
    }
}
