package dev.bulletcarpet;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecartEmpty;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import sun.misc.Unsafe;

public final class RidingEntitySummonCheck {
    private static void set(Entity entity, String name, Object value) throws Exception {
        Field field = Entity.class.getDeclaredField(name); field.setAccessible(true); field.set(entity, value);
    }
    private static EntityPig pig() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe"); field.setAccessible(true);
        EntityPig pig = (EntityPig) ((Unsafe) field.get(null)).allocateInstance(EntityPig.class);
        set(pig, "riddenByEntities", new ArrayList<Entity>());
        return pig;
    }
    public static void main(String[] args) throws Exception {
        net.minecraft.init.Bootstrap.register();
        for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
            BlockPos anchor = new BlockPos(-12, 63, 8);
            EntityItemFrame frame = new EntityItemFrame(null, anchor, facing);
            frame.setSilent(true);
            NBTTagCompound original = new NBTTagCompound();
            frame.writeEntityToNBT(original);
            original.setTag("Item", new ItemStack(Items.STICK).writeToNBT(new NBTTagCompound()));
            original.setByte("ItemRotation", (byte) 5);
            frame.readEntityFromNBT(original);

            String summon = RidingEntitySummon.command(frame);
            String prefix = "/summon minecraft:item_frame -12 63 8 ";
            if (!summon.startsWith(prefix)) throw new AssertionError("Wrong frame attachment coordinates: " + summon);
            NBTTagCompound pasted = JsonToNBT.getTagFromJson(summon.substring(prefix.length()));
            if (!original.getTag("Facing").equals(pasted.getTag("Facing"))
                    || !original.getTag("Item").equals(pasted.getTag("Item"))
                    || pasted.getByte("ItemRotation") != 5
                    || pasted.getInteger("TileX") != -12 || pasted.getInteger("TileY") != 63 || pasted.getInteger("TileZ") != 8) {
                throw new AssertionError("Lost item frame data: " + summon);
            }
        }
        EntityMinecartEmpty cart = new EntityMinecartEmpty(null, 10.5, 64, -2.5);
        String bare = RidingEntitySummon.command(cart);
        if (!"/summon minecraft:minecart 10.500000 64.000000 -2.500000".equals(bare)) throw new AssertionError("Bare summon changed");
        EntityPig pig = pig(); set(cart, "riddenByEntities", Arrays.<Entity>asList(pig)); set(pig, "ridingEntity", cart);
        if (RidingEntitySummon.command(pig) != null) throw new AssertionError("Rider summoned separately");
        String command = RidingEntitySummon.command(cart);
        NBTTagCompound data = JsonToNBT.getTagFromJson(command.substring(command.indexOf('{')));
        if (!"minecraft:pig".equals(data.getTagList("Passengers", 10).getCompoundTagAt(0).getString("id"))) throw new AssertionError("Lost mounted pig");
        if (command.length() > 256 || command.contains("UUID") || command.contains("Items")) throw new AssertionError("Unexpected summon data");
        EntityPig nested = pig(); set(pig, "riddenByEntities", Arrays.<Entity>asList(nested));
        command = RidingEntitySummon.command(cart);
        data = JsonToNBT.getTagFromJson(command.substring(command.indexOf('{')));
        if (data.getTagList("Passengers", 10).getCompoundTagAt(0).getTagList("Passengers", 10).tagCount() != 1) throw new AssertionError("Nested mount lost");
        ArrayList<Entity> passengers = new ArrayList<>();
        for (int i = 0; i < 20; i++) passengers.add(pig());
        set(cart, "riddenByEntities", passengers);
        try { RidingEntitySummon.command(cart); throw new AssertionError("Oversize command silently truncated"); }
        catch (IllegalArgumentException expected) { }
        System.out.println("PASS item frame data, mounted pig and nested passenger summon, standalone rider exclusion, unchanged bare summon and explicit length limit");
    }
}
