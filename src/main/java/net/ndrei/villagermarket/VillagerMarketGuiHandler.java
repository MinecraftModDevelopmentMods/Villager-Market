package net.ndrei.villagermarket;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

/**
 * Created by CF on 2017-02-21.
 */
public class VillagerMarketGuiHandler implements IGuiHandler {
    private VillagerMarketContainer getContainer(EntityPlayer player, World world, BlockPos pos) {
        return new VillagerMarketContainer(world, pos, player);
    }

    @Nullable
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == VillagerMarketMod.villagerMarket) {
            return this.getContainer(player, world, pos);
        }

        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == VillagerMarketMod.villagerMarket) {
            return new VillagerMarketScreen(new VillagerMarketContainer(player));
        }

        return null;
    }
}
