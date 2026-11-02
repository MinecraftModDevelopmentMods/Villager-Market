package net.modcrafters.minecraft.villagermarket.menu;

import net.modcrafters.minecraft.villagermarket.trade.ListedOffer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record OpeningData(BlockPos pos, List<ListedOffer> offers) {
    public static final StreamCodec<RegistryFriendlyByteBuf, OpeningData> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            OpeningData::pos,
            ListedOffer.LIST_CODEC,
            OpeningData::offers,
            OpeningData::new
    );
}
