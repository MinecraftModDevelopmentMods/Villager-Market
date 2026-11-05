package net.modcrafters.minecraft.villagermarket.network;

import net.modcrafters.minecraft.villagermarket.Constants;
import net.modcrafters.minecraft.villagermarket.block.entity.MarketAutomatonBlockEntity;
import net.modcrafters.minecraft.villagermarket.menu.MarketAutomatonMenu;
import net.modcrafters.minecraft.villagermarket.platform.Services;
import net.modcrafters.minecraft.villagermarket.trade.ListedOffer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class AutomatonPayloads {
    private AutomatonPayloads() {
    }

    public record Select(int villagerId, int offerIndex) implements CustomPacketPayload {
        public static final Type<Select> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "automaton_select"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Select> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, Select::villagerId,
                ByteBufCodecs.INT, Select::offerIndex,
                Select::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record Recipe(ItemStack costA, ItemStack costB, ItemStack result) implements CustomPacketPayload {
        public static final Type<Recipe> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "automaton_recipe"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Recipe> STREAM_CODEC = StreamCodec.composite(
                ItemStack.OPTIONAL_STREAM_CODEC, Recipe::costA,
                ItemStack.OPTIONAL_STREAM_CODEC, Recipe::costB,
                ItemStack.OPTIONAL_STREAM_CODEC, Recipe::result,
                Recipe::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record Redstone() implements CustomPacketPayload {
        public static final Type<Redstone> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "automaton_redstone"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Redstone> STREAM_CODEC = StreamCodec.unit(new Redstone());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record Similar() implements CustomPacketPayload {
        public static final Type<Similar> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "automaton_similar"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Similar> STREAM_CODEC = StreamCodec.unit(new Similar());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SimilarState(boolean similar) implements CustomPacketPayload {
        public static final Type<SimilarState> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "automaton_similar_state"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SimilarState> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, SimilarState::similar,
                SimilarState::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record Mode(int mode) implements CustomPacketPayload {
        public static final Type<Mode> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "automaton_mode"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Mode> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, Mode::mode,
                Mode::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void handleSelect(ServerPlayer player, Select select) {
        if (!(player.containerMenu instanceof MarketAutomatonMenu menu)) {
            return;
        }
        ListedOffer chosen = null;
        for (ListedOffer offer : menu.offers()) {
            if (offer.villagerId() == select.villagerId() && offer.offerIndex() == select.offerIndex()) {
                chosen = offer;
                break;
            }
        }
        if (chosen == null) {
            return;
        }
        BlockEntity entity = player.level().getBlockEntity(menu.pos());
        if (!(entity instanceof MarketAutomatonBlockEntity machine)) {
            return;
        }
        machine.arm(chosen.costA(), chosen.costB(), chosen.result());
        menu.arm(machine.costA(), machine.costB(), machine.result());
        Services.PLATFORM.sendToPlayer(player, new Recipe(menu.costA(), menu.costB(), menu.result()));
    }

    public static void handleRedstone(ServerPlayer player, Redstone redstone) {
        if (!(player.containerMenu instanceof MarketAutomatonMenu menu)) {
            return;
        }
        if (!(player.level().getBlockEntity(menu.pos()) instanceof MarketAutomatonBlockEntity machine)) {
            return;
        }
        machine.cycleRedstone();
        menu.redstone(machine.redstone());
        Services.PLATFORM.sendToPlayer(player, new Mode(machine.redstone().ordinal()));
    }

    public static void handleSimilar(ServerPlayer player, Similar similar) {
        if (!(player.containerMenu instanceof MarketAutomatonMenu menu)) {
            return;
        }
        if (!(player.level().getBlockEntity(menu.pos()) instanceof MarketAutomatonBlockEntity machine)) {
            return;
        }
        machine.toggleSimilar();
        menu.similar(machine.similar());
        Services.PLATFORM.sendToPlayer(player, new SimilarState(machine.similar()));
    }
}
