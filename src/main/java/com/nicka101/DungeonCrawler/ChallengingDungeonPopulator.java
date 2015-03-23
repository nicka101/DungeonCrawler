package com.nicka101.DungeonCrawler;

import net.minecraft.server.v1_8_R2.NBTTagCompound;
import net.minecraft.server.v1_8_R2.NBTTagList;
import net.minecraft.server.v1_8_R2.TileEntityMobSpawner;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R2.block.CraftCreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.noise.SimplexNoiseGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.WeakHashMap;

/**
 * Created by Nick on 22/03/2015.
 */
public class ChallengingDungeonPopulator extends BlockPopulator {

    //private WeakHashMap<World, SimplexOctaveGenerator> noiseGenerators = new WeakHashMap<>();

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        ChunkCoords self = new ChunkCoords(chunk);
        ChunkCoords adjacent = DungeonCrawler.Instance.adjacentToDungeonChunk(self);

        if(adjacent != null && random.nextInt(20) > 12){
            generateDungeon(random, chunk, world.getChunkAt(adjacent.x, adjacent.z), self);
            return;
        }
        if(random.nextInt(1000) < 10){
            generateDungeon(random, chunk, null, self);
        }
        /*SimplexOctaveGenerator generator;
        if(noiseGenerators.containsKey(world)){
            generator = noiseGenerators.get(world);
        } else {
            generator = new SimplexOctaveGenerator(world, 8);
            noiseGenerators.put(world, generator);
        }
        double noise = generator.noise(self.x, self.z, 2, 5);
        if(noise < -0.945 || (adjacent != null && Math.abs(noise) < 0.07)){
            boolean generateLower = random.nextInt(20) == 8;
            int dungeonHeight = DungeonCrawler.Instance.getDungeonHeight(self);
            generateDungeonPart(chunk, random, dungeonHeight, adjacent == null ? null : world.getChunkAt(adjacent.x, adjacent.z));
            if(generateLower){
                generatePathToNextFloor(chunk, random, dungeonHeight);
                generateDungeonPart(chunk, random, dungeonHeight - 15, null);
            }
        }*/
    }

    private void generateDungeon(Random random, Chunk thisChunk, Chunk adjacentChunk, ChunkCoords self){
        int generateLowerChance = random.nextInt(20);
        boolean generateLower =  generateLowerChance > 8 && generateLowerChance < 12;
        int dungeonHeight = DungeonCrawler.Instance.getDungeonHeight(self);
        generateDungeonPart(thisChunk, random, dungeonHeight, adjacentChunk == null ? null : adjacentChunk);
        if(generateLower){
            generateDungeonPart(thisChunk, random, dungeonHeight - 15, null);
            generatePathToNextFloor(thisChunk, random, dungeonHeight);
        }
    }

    private void generateDungeonPart(Chunk c, Random random, int height, Chunk adjacent){
        int exemptX = -1, exemptZ = -1;
        if(adjacent != null){
            int xDiff = c.getX() - adjacent.getX();
            int zDiff = c.getZ() - adjacent.getZ();
            exemptX = xDiff < 0 ? 15 : xDiff > 0 ? 0 : -1;
            exemptZ = zDiff < 0 ? 15 : zDiff > 0 ? 0 : -1;
            if(exemptX != -1){
                for(int y = height + 1; y < height + 6; y++) {
                    for (int z = 0; z < 15; z++) {
                        adjacent.getBlock(exemptX == 15 ? 0 : 15, y, z).setType(Material.AIR);
                    }
                }
            }
            if(exemptZ != -1){
                for(int y = height + 1; y < height + 6; y++) {
                    for (int x = 0; x < 15; x++) {
                        adjacent.getBlock(x, y, exemptZ == 15 ? 0 : 15).setType(Material.AIR);
                    }
                }
            }
        }
        int spawnerHardCap = 2;
        int chestHardCap = 1;
        for(int x = 0; x < 16; x++) {
            for(int y = 0; y < 7; y++){
                for(int z = 0; z < 16; z++){
                    Block b = c.getBlock(x, height + y, z);
                    if(y == 0 || y == 6
                            || (x == 0 && x != exemptX)
                            || (x == 15 && x != exemptX)
                            || (z == 0 && z != exemptZ)
                            || (z == 15 && z != exemptZ)) generateWallBlock(random, b, y == 0);
                    else {
                        if(y == 1 && chestHardCap > 0&& random.nextInt(784) < 1) {
                            chestHardCap--;
                            generateChest(random, b);
                        }
                        else if(spawnerHardCap > 0 && random.nextInt(980) < 2) {
                            spawnerHardCap--;
                            generateSpawner(random, b);
                        }
                        else b.setType(Material.AIR);
                    }
                }
            }
        }
        DungeonCrawler.Instance.addDungeonChunk(c, height);
    }

    private void generatePathToNextFloor(Chunk c, Random random, int height){
        for(int x = 5; x < 8; x++){
            for(int y = height - 9; y <= height; y++){
                for(int z = 5; z < 8; z++){
                    Block b = c.getBlock(x, y, z);
                    if(x == 6 && z == 6) b.setType(Material.AIR);
                    else generateWallBlock(random, b, false);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void generateWallBlock(Random random, Block b, boolean floor){
        switch (random.nextInt(4)){
            case 0:
                b.setTypeIdAndData(Material.SMOOTH_BRICK.getId(), (byte) 0, false);
                break;
            case 1:
                b.setTypeIdAndData(Material.SMOOTH_BRICK.getId(), (byte) 1, false);
                break;
            case 2:
                b.setTypeIdAndData(Material.SMOOTH_BRICK.getId(), (byte) 2, false);
                break;
            default:
                b.setType(floor ? Material.GRASS : Material.AIR);
                break;
        }
    }

    private void generateSpawner(Random random, Block b){
        b.setType(Material.MOB_SPAWNER);
        CraftCreatureSpawner spawner = (CraftCreatureSpawner)b.getState();
        try {
            Field nmsSpawnerField = spawner.getClass().getDeclaredField("spawner");
            nmsSpawnerField.setAccessible(true);
            TileEntityMobSpawner nmsSpawnerTile = (TileEntityMobSpawner)nmsSpawnerField.get(spawner);
            NBTTagCompound spawnerTag = new NBTTagCompound();
            NBTTagCompound spawnDataTag = new NBTTagCompound();
            nmsSpawnerTile.b(spawnerTag); //All tags are required so load the ones bukkit has already set
            switch(random.nextInt(10)){
                case 0:
                    spawnerTag.setString("EntityId", "Creeper");
                    spawnDataTag.setBoolean("powered", random.nextInt(6) < 1);
                    spawnDataTag.setInt("ExplosionRadius", 4);
                    break;
                case 1:
                    spawnerTag.setString("EntityId", "Blaze");
                    break;
                case 2:
                    spawnerTag.setString("EntityId", "CaveSpider");
                    break;
                case 3:
                    spawnerTag.setString("EntityId", "PigZombie");
                    spawnDataTag.setInt("Anger", 32767);
                    break;
                case 4:
                    spawnerTag.setString("EntityId", "Rabbit");
                    spawnDataTag.setInt("RabbitType", 99); //Killer Rabbit
                    break;
                case 5:
                    spawnerTag.setString("EntityId", "Silverfish");
                    break;
                case 6:
                    spawnerTag.setString("EntityId", "Witch");
                    break;
                case 7:
                    spawnerTag.setString("EntityId", "Zombie");
                    break;
                case 8:
                    spawnDataTag.setInt("SkeletonType", 1); //Set Wither Skeleton
                case 9:
                    spawnerTag.setString("EntityId", "Skeleton");
                    break;
                case 10: //Disabled as wolves do not currently spawn (pigs do instead, NBT data ignored)
                    spawnerTag.setString("EntityId", "Wolf");
                    spawnDataTag.setBoolean("Angry", true);
                    spawnDataTag.setByte("CollarColor", (byte) 14);
                    break;
            }
            spawnerTag.setShort("MinSpawnDelay", (short) 9);
            spawnerTag.setShort("MaxSpawnDelay", (short) 14);

            spawnDataTag.setBoolean("Silent", true); //Sneaky Sneaky
            NBTTagList attributesTag = new NBTTagList();

            //Increase Max Health to 15 hearts
            NBTTagCompound healthTag = new NBTTagCompound();
            healthTag.setString("Name", "generic.maxHealth");
            healthTag.setDouble("Base", 30.0f);
            attributesTag.add(healthTag);

            //Set Knockback resistance to 80 percent
            NBTTagCompound knockbackResist = new NBTTagCompound();
            knockbackResist.setString("Name", "generic.knockbackResistance");
            knockbackResist.setDouble("Base", 0.8f);
            attributesTag.add(knockbackResist);

            //Set Follow range to 32 (this actually lowers zombie follow range as it is default 40)
            NBTTagCompound followRange = new NBTTagCompound();
            followRange.setString("Name", "generic.followRange");
            followRange.setDouble("Base", 32.0f);
            attributesTag.add(followRange);

            spawnerTag.set("SpawnData", spawnDataTag);
            nmsSpawnerTile.a(spawnerTag);
            nmsSpawnerTile.update();
            spawner.update(true);

            if(spawner.getSpawnedType() == EntityType.PIG){
                DungeonCrawler.logPluginMsg("Failed to set SpawnData for the following NBT: " + spawnerTag.toString());
            }
        } catch (NoSuchFieldException|IllegalAccessException e){
            e.printStackTrace();
        }
    }

    private void generateChest(Random random, Block b){
        b.setType(Material.CHEST);
    }
}
