package com.nicka101.DungeonCrawler;

import net.minecraft.server.v1_8_R2.NBTTagCompound;
import net.minecraft.server.v1_8_R2.NBTTagFloat;
import net.minecraft.server.v1_8_R2.NBTTagList;
import net.minecraft.server.v1_8_R2.TileEntityMobSpawner;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R2.block.CraftCreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.noise.PerlinNoiseGenerator;
import org.bukkit.util.noise.PerlinOctaveGenerator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Random;

/**
 * Created by Nick on 22/03/2015.
 */
public class ChallengingDungeonPopulator extends BlockPopulator {

    private PerlinOctaveGenerator noiseGenerator;
    private long inThreshold = 0;
    private long chunksProcessed = 0;
    private double highestNoise = 0;
    private double lowestNoise = 1;

    @Override
    public void populate(World world, Random random, Chunk chunk){
        Chunk adjacent = DungeonCrawler.Instance.adjacentToDungeonChunk(chunk);

        if(noiseGenerator == null){
            noiseGenerator = new PerlinOctaveGenerator(world, 8);
        }
        int dungeonHeight = DungeonCrawler.Instance.getDungeonHeight(chunk);
        double noise = Math.abs(noiseGenerator.noise(chunk.getX(), chunk.getZ(), 0.5D, 0.5D));

        if(noise > 0.63 || (adjacent != null && noise < 0.1)){
            generateDungeon(random, chunk, adjacent);
            if(random.nextInt(20) == 8){
                generatePathToNextFloor(chunk, random, dungeonHeight);
                generateDungeonPart(chunk, random, dungeonHeight - 15, null);
            }
            inThreshold++;
        }
        chunksProcessed++;

        if(noise > highestNoise) highestNoise = noise;
        if(noise < lowestNoise) lowestNoise = noise;

        if(chunksProcessed % 500 == 0){
            DungeonCrawler.logPluginMsg(String.format("Highest Noise: %.4f, Lowest Noise: %.4f", highestNoise, lowestNoise));
            DungeonCrawler.logPluginMsg("Threshold Percentage: " + (double)inThreshold * 100/ chunksProcessed);
        }
    }

    private void generateDungeon(@Nonnull Random random, @Nonnull Chunk thisChunk, @Nullable Chunk adjacentChunk){
        int generateLowerChance = random.nextInt(20);
        int dungeonHeight = DungeonCrawler.Instance.getDungeonHeight(thisChunk);
        generateDungeonPart(thisChunk, random, dungeonHeight, adjacentChunk);
        if(generateLowerChance > 8 && generateLowerChance < 12){
            generateDungeonPart(thisChunk, random, dungeonHeight - 15, null);
            generatePathToNextFloor(thisChunk, random, dungeonHeight);
        }
    }

    @SuppressWarnings("deprecation")
    private void generateDungeonPart(@Nonnull Chunk c, @Nonnull Random random, int height, @Nullable Chunk adjacent){
        int exemptX = -1, exemptZ = -1;
        if(adjacent != null){
            int xDiff = c.getX() - adjacent.getX();
            int zDiff = c.getZ() - adjacent.getZ();
            exemptX = xDiff < 0 ? 15 : xDiff > 0 ? 0 : -1;
            exemptZ = zDiff < 0 ? 15 : zDiff > 0 ? 0 : -1;
            if(exemptX != -1){
                for(int y = height + 1; y < height + 6; y++) {
                    for (int z = 0; z < 15; z++) {
                        adjacent.getBlock(exemptX == 15 ? 0 : 15, y, z).setType(Material.AIR, false);
                    }
                }
            }
            if(exemptZ != -1){
                for(int y = height + 1; y < height + 6; y++) {
                    for (int x = 0; x < 15; x++) {
                        adjacent.getBlock(x, y, exemptZ == 15 ? 0 : 15).setType(Material.AIR, false);
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
                    if(x == 6 && z == 6) b.setType(Material.AIR, false);
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
                b.setType(floor ? Material.GRASS : Material.AIR, false);
                break;
        }
    }

    @Deprecated
    private void generateSpawner(Random random, Block b){
        boolean setType;
        do {
            setType = b.setTypeId(Material.MOB_SPAWNER.getId()); //Use the deprecated setTypeId because it tells us success
        } while(!setType);
        CraftCreatureSpawner spawner = (CraftCreatureSpawner)b.getState();
        try {
            Field nmsSpawnerField = Util.getField(spawner.getClass(), "spawner");
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
                    ItemStack pigSword = new ItemStack(Material.DIAMOND_SWORD, 1);
                    pigSword.addEnchantment(Enchantment.FIRE_ASPECT, 2);
                    pigSword.addEnchantment(Enchantment.DAMAGE_ALL, 2);
                    spawnDataTag.set("Equipment", Util.generateEquipmentNBT(pigSword, null, null, null, null));
                    break;
                case 4:
                    spawnerTag.setString("EntityId", "Rabbit");
                    spawnDataTag.setInt("RabbitType", 99); //Killer Rabbit
                    b.getRelative(BlockFace.UP).setType(Material.TORCH, false); //Rabbits only spawn in high light levels
                    break;
                case 5:
                    spawnerTag.setString("EntityId", "Silverfish");
                    break;
                case 6:
                    spawnerTag.setString("EntityId", "Witch");
                    break;
                case 7:
                    spawnerTag.setString("EntityId", "Zombie");
                    ItemStack zombSword = new ItemStack(Material.DIAMOND_SWORD, 1);
                    zombSword.addEnchantment(Enchantment.DAMAGE_ALL, 2);
                    zombSword.addEnchantment(Enchantment.KNOCKBACK, 1);
                    spawnDataTag.set("Equipment", Util.generateEquipmentNBT(zombSword, null, null, null, null));
                    break;
                case 8:
                    spawnerTag.setString("EntityId", "Skeleton");
                    spawnDataTag.setInt("SkeletonType", 1); //Set Wither Skeleton
                    ItemStack skeleSword = new ItemStack(Material.IRON_SWORD, 1);
                    skeleSword.addEnchantment(Enchantment.KNOCKBACK, 1);
                    skeleSword.addEnchantment(Enchantment.FIRE_ASPECT, 1);
                    spawnDataTag.set("Equipment", Util.generateEquipmentNBT(skeleSword, null, null, null, null));
                    break;
                case 9:
                    spawnerTag.setString("EntityId", "Skeleton");
                    ItemStack skeleBow = new ItemStack(Material.BOW, 1);
                    skeleBow.addEnchantment(Enchantment.ARROW_DAMAGE, 3);
                    skeleBow.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 32);
                    spawnDataTag.set("Equipment", Util.generateEquipmentNBT(skeleBow, null, null, null, null));
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

            //Disable dropping gear
            NBTTagList dropChances = new NBTTagList();
            for(int i = 0; i < 5; i++) dropChances.add(new NBTTagFloat(0.0f));
            spawnDataTag.set("DropChances", dropChances);

            spawnerTag.set("SpawnData", spawnDataTag);

            nmsSpawnerTile.a(spawnerTag);
            nmsSpawnerTile.update();
            spawner.update(true);

            if(spawner.getSpawnedType() == EntityType.PIG){
                DungeonCrawler.logPluginMsg("Failed to set SpawnData for the following NBT: " + spawnerTag.toString());
            }
        } catch (IllegalAccessException e){
            e.printStackTrace();
        }
    }

    private void generateChest(Random random, Block b){
        b.setType(Material.CHEST);
    }
}
