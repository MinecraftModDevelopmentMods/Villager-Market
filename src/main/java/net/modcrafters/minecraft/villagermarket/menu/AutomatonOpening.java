package net.modcrafters.minecraft.villagermarket.menu;

import net.modcrafters.minecraft.villagermarket.trade.ListedOffer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record AutomatonOpening(
        BlockPos pos,
        List<ListedOffer> offers,
        ItemStack costA,
        ItemStack costB,
        ItemStack result,
        int redstone,
        boolean similar
) {
    public static final StreamCodec<RegistryFriendlyByteBuf, AutomatonOpening> STREAM_CODEC = StreamCodec.of(
            (buf, opening) -> {
                BlockPos.STREAM_CODEC.encode(buf, opening.pos());
                ListedOffer.LIST_CODEC.encode(buf, opening.offers());
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, opening.costA());
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, opening.costB());
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, opening.result());
                ByteBufCodecs.INT.encode(buf, opening.redstone());
                ByteBufCodecs.BOOL.encode(buf, opening.similar());
            },
            buf -> new AutomatonOpening(
                    BlockPos.STREAM_CODEC.decode(buf),
                    ListedOffer.LIST_CODEC.decode(buf),
                    ItemStack.OPTIONAL_STREAM_CODEC.decode(buf),
                    ItemStack.OPTIONAL_STREAM_CODEC.decode(buf),
                    ItemStack.OPTIONAL_STREAM_CODEC.decode(buf),
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf)));
}
