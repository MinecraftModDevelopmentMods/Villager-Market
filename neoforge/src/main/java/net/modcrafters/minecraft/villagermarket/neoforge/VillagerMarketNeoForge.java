package net.modcrafters.minecraft.villagermarket.neoforge;

import net.modcrafters.minecraft.villagermarket.Constants;
import net.modcrafters.minecraft.villagermarket.ModContent;
import net.modcrafters.minecraft.villagermarket.VillagerMarket;
import net.modcrafters.minecraft.villagermarket.neoforge.client.VillagerMarketNeoForgeClient;
import net.modcrafters.minecraft.villagermarket.neoforge.gametest.NeoForgeMarketGameTests;
import net.modcrafters.minecraft.villagermarket.network.AutomatonPayloads;
import net.modcrafters.minecraft.villagermarket.network.MarketPayloads;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(Constants.MOD_ID)
public class VillagerMarketNeoForge {
    public VillagerMarketNeoForge(IEventBus modBus) {
        VillagerMarket.init();
        modBus.addListener(VillagerMarketNeoForge::registerContent);
        modBus.addListener(VillagerMarketNeoForge::registerPayloads);
        NeoForgeMarketGameTests.register(modBus);
        //? if >=1.21.9 {
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
        //?} else
        //if (FMLEnvironment.dist == Dist.CLIENT) {
            VillagerMarketNeoForgeClient.register(modBus);
        }
    }

    private static void registerContent(RegisterEvent event) {
        event.register(Registries.BLOCK, ModContent.MARKET_ID.identifier(), ModContent::marketBlock);
        event.register(Registries.ITEM, ModContent.MARKET_ITEM_ID.identifier(), ModContent::marketItem);
        event.register(Registries.MENU, ModContent.MENU_ID.identifier(), ModContent::menu);
        event.register(Registries.BLOCK, ModContent.AUTOMATON_ID.identifier(), ModContent::automatonBlock);
        event.register(Registries.ITEM, ModContent.AUTOMATON_ITEM_ID.identifier(), ModContent::automatonItem);
        event.register(Registries.BLOCK_ENTITY_TYPE, ModContent.AUTOMATON_ID.identifier(), ModContent::automatonType);
        event.register(Registries.MENU, ModContent.AUTOMATON_MENU_ID.identifier(), ModContent::automatonMenu);
        event.register(Registries.CREATIVE_MODE_TAB, ModContent.TAB_ID.identifier(), () ->
                ModContent.titled(CreativeModeTab.builder())
                        .displayItems((parameters, output) -> {
                            output.accept(ModContent.MARKET_ITEM);
                            output.accept(ModContent.AUTOMATON_ITEM);
                        })
                        .build());
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        //? if >=1.21.6 {
        registrar.playToClient(MarketPayloads.Offers.TYPE, MarketPayloads.Offers.STREAM_CODEC);
        //?} else {
        /*registrar.playToClient(MarketPayloads.Offers.TYPE, MarketPayloads.Offers.STREAM_CODEC, (payload, context) -> {
            net.modcrafters.minecraft.villagermarket.client.VillagerMarketScreen.apply(payload);
            net.modcrafters.minecraft.villagermarket.client.MarketAutomatonScreen.applyOffers(payload);
        });
        *///?}
        registrar.playToServer(MarketPayloads.Trade.TYPE, MarketPayloads.Trade.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> MarketPayloads.handleTrade((net.minecraft.server.level.ServerPlayer) context.player(), payload)));
        //? if >=1.21.6 {
        registrar.playToClient(AutomatonPayloads.Recipe.TYPE, AutomatonPayloads.Recipe.STREAM_CODEC);
        //?} else
        //registrar.playToClient(AutomatonPayloads.Recipe.TYPE, AutomatonPayloads.Recipe.STREAM_CODEC, (payload, context) -> net.modcrafters.minecraft.villagermarket.client.MarketAutomatonScreen.apply(payload));
        registrar.playToServer(AutomatonPayloads.Select.TYPE, AutomatonPayloads.Select.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AutomatonPayloads.handleSelect((net.minecraft.server.level.ServerPlayer) context.player(), payload)));
        //? if >=1.21.6 {
        registrar.playToClient(AutomatonPayloads.Mode.TYPE, AutomatonPayloads.Mode.STREAM_CODEC);
        //?} else
        //registrar.playToClient(AutomatonPayloads.Mode.TYPE, AutomatonPayloads.Mode.STREAM_CODEC, (payload, context) -> net.modcrafters.minecraft.villagermarket.client.MarketAutomatonScreen.applyMode(payload));
        registrar.playToServer(AutomatonPayloads.Redstone.TYPE, AutomatonPayloads.Redstone.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AutomatonPayloads.handleRedstone((net.minecraft.server.level.ServerPlayer) context.player(), payload)));
        //? if >=1.21.6 {
        registrar.playToClient(AutomatonPayloads.SimilarState.TYPE, AutomatonPayloads.SimilarState.STREAM_CODEC);
        //?} else
        //registrar.playToClient(AutomatonPayloads.SimilarState.TYPE, AutomatonPayloads.SimilarState.STREAM_CODEC, (payload, context) -> net.modcrafters.minecraft.villagermarket.client.MarketAutomatonScreen.applySimilar(payload));
        registrar.playToServer(AutomatonPayloads.Similar.TYPE, AutomatonPayloads.Similar.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AutomatonPayloads.handleSimilar((net.minecraft.server.level.ServerPlayer) context.player(), payload)));
    }
}
