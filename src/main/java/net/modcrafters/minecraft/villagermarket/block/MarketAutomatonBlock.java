package net.modcrafters.minecraft.villagermarket.block;

import net.modcrafters.minecraft.villagermarket.ModContent;
import net.modcrafters.minecraft.villagermarket.block.entity.MarketAutomatonBlockEntity;
import net.modcrafters.minecraft.villagermarket.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
//? if <26.1 {
/*import com.mojang.serialization.MapCodec;
import net.minecraft.world.Containers;
import net.minecraft.world.level.block.RenderShape;
*///?}

public class MarketAutomatonBlock extends BaseEntityBlock {
    //? if <26.1 {
    /*private static final MapCodec<MarketAutomatonBlock> CODEC = simpleCodec(MarketAutomatonBlock::new);

    @Override
    protected MapCodec<MarketAutomatonBlock> codec() {
        return CODEC;
    }
    *///?}

    public MarketAutomatonBlock(Properties properties) {
        super(properties);
    }

    //? if <26.1 {
    /*@Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
    *///?}

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MarketAutomatonBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, ModContent.automatonType(), MarketAutomatonBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer) {
            Services.PLATFORM.openAutomaton(serverPlayer, pos);
        }
        return InteractionResult.SUCCESS;
    }

    //? if <26.1 {
    /*@Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof MarketAutomatonBlockEntity machine) {
            Containers.dropContents(level, pos, machine);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
    *///?}
}
