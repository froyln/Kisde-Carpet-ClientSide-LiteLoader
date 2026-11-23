package dev.bulletcarpet.mixin;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(World.class)
public interface WorldAccessor {
    @Invoker("getEntitiesWithinAABBExcludingEntity") java.util.List<net.minecraft.entity.Entity> bulletcarpet$getEntities(net.minecraft.entity.Entity excluded, net.minecraft.util.math.AxisAlignedBB bounds);
    @Invoker("getBlockState") IBlockState bulletcarpet$getBlockState(BlockPos pos);
    @Invoker("getTileEntity") TileEntity bulletcarpet$getTileEntity(BlockPos pos);
}
