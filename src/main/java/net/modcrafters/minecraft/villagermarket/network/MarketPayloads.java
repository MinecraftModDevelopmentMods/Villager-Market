package net.modcrafters.minecraft.villagermarket.network;

import net.modcrafters.minecraft.villagermarket.Constants;
import net.modcrafters.minecraft.villagermarket.menu.VillagerMarketMenu;
import net.modcrafters.minecraft.villagermarket.platform.Services;
import net.modcrafters.minecraft.villagermarket.trade.ListedOffer;
import net.modcrafters.minecraft.villagermarket.trade.TradeExecutor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public final class MarketPayloads {
    private MarketPayloads() {
    }

    public record Offers(List<ListedOffer> offers) implements CustomPacketPayload {
        public static final Type<Offers> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "offers"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Offers> STREAM_CODEC = StreamCodec.composite(
                ListedOffer.LIST_CODEC, Offers::offers, Offers::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record Trade(int villagerId, int offerIndex, int uses) implements CustomPacketPayload {
        public static final Type<Trade> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "trade"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Trade> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, Trade::villagerId,
                ByteBufCodecs.INT, Trade::offerIndex,
                ByteBufCodecs.INT, Trade::uses,
                Trade::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void handleTrade(ServerPlayer player, Trade trade) {
        if (!(player.containerMenu instanceof VillagerMarketMenu menu)) {
            return;
        }
        TradeExecutor.execute(player, menu.pos(), trade.villagerId(), trade.offerIndex(), trade.uses());
        menu.broadcastChanges();
        List<ListedOffer> fresh = VillagerMarketMenu.scan((ServerLevel) player.level(), menu.pos());
        menu.replace(fresh);
        Services.PLATFORM.sendToPlayer(player, new Offers(fresh));
    }
}
