package com.nicka101.DungeonCrawler;

import org.bukkit.Chunk;

/**
 * Created by Nick on 22/03/2015.
 */
public class ChunkCoords {

    public final int x;
    public final int z;

    public ChunkCoords(int x, int z){
        this.x = x;
        this.z = z;
    }

    public ChunkCoords(Chunk c){
        this(c.getX(), c.getZ());
    }
}
