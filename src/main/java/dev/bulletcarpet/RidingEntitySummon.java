package dev.bulletcarpet;

import java.util.Locale;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public final class RidingEntitySummon {
    private RidingEntitySummon() { }

    public static String command(Entity entity) {
        ResourceLocation id = EntityList.getKey(entity);
        if (entity.isRiding() || id == null) return null;
        String command = String.format(Locale.ROOT, "/summon %s %f %f %f", id, entity.posX, entity.posY, entity.posZ);
        if (entity instanceof EntityItemFrame) {
            BlockPos anchor = ((EntityItemFrame) entity).getHangingPosition();
            command = "/summon " + id + " " + anchor.getX() + " " + anchor.getY() + " " + anchor.getZ();
        }
        if (entity instanceof EntityItemFrame || !entity.getPassengers().isEmpty()) {
            NBTTagCompound tree = tree(entity, 0, new int[] {0});
            tree.removeTag("id");
            command += " " + tree;
        }
        // shortcut: chat limits commands to 256 characters; larger entity payloads need typed entity paste.
        if (command.length() > 256) throw new IllegalArgumentException("Entity data exceeds the 256-character summon limit");
        return command;
    }

    private static NBTTagCompound tree(Entity entity, int depth, int[] count) {
        if (depth >= 32 || ++count[0] > 4096) throw new IllegalArgumentException("Too many entities or passenger levels");
        ResourceLocation id = EntityList.getKey(entity);
        if (id == null) throw new IllegalArgumentException("Unsupported passenger type");
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("id", id.toString());
        if (entity instanceof EntityItemFrame) ((EntityItemFrame) entity).writeEntityToNBT(tag);
        if (!entity.getPassengers().isEmpty()) {
            NBTTagList passengers = new NBTTagList();
            for (Entity passenger : entity.getPassengers()) passengers.appendTag(tree(passenger, depth + 1, count));
            tag.setTag("Passengers", passengers);
        }
        return tag;
    }
}
