package com.nicka101.DungeonCrawler;

import net.minecraft.server.v1_8_R2.NBTTagCompound;
import net.minecraft.server.v1_8_R2.NBTTagList;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

/**
 * Created by Nick on 28/03/2015.
 */
@SuppressWarnings("unused")
public class Util {

    @Deprecated
    protected static NBTTagList generateEquipmentNBT(@Nullable ItemStack hand,
                                                  @Nullable ItemStack head,
                                                  @Nullable ItemStack chest,
                                                  @Nullable ItemStack legs,
                                                  @Nullable ItemStack feet){
        NBTTagList equipment = new NBTTagList();
        equipment.add(hand != null ? CraftItemStack.asNMSCopy(hand).save(new NBTTagCompound()) : new NBTTagCompound());
        equipment.add(feet != null ? CraftItemStack.asNMSCopy(feet).save(new NBTTagCompound()) : new NBTTagCompound());
        equipment.add(legs != null ? CraftItemStack.asNMSCopy(legs).save(new NBTTagCompound()) : new NBTTagCompound());
        equipment.add(chest != null ? CraftItemStack.asNMSCopy(chest).save(new NBTTagCompound()) : new NBTTagCompound());
        equipment.add(head != null ? CraftItemStack.asNMSCopy(head).save(new NBTTagCompound()) : new NBTTagCompound());

        return equipment;
    }

    @Deprecated
    protected static Field getField(Class clazz, String name){
        Field f = null;
        try {
            f = clazz.getDeclaredField(name);
            f.setAccessible(true);
        } catch(NoSuchFieldException e){
            e.printStackTrace();
        }
        return f;
    }
}
