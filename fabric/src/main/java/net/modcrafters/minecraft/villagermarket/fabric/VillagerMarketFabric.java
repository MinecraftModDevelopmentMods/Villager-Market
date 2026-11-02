package net.modcrafters.minecraft.villagermarket.fabric;

import net.modcrafters.minecraft.villagermarket.Constants;
import net.modcrafters.minecraft.villagermarket.ModContent;
import net.modcrafters.minecraft.villagermarket.VillagerMarket;
import net.modcrafters.minecraft.villagermarket.network.MarketPayloads;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class VillagerMarketFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        VillagerMarket.init();
        Registry.register(BuiltInRegistries.BLOCK, ModContent.MARKET_ID.identifier(), ModContent.marketBlock());
        Registry.register(BuiltInRegistries.ITEM, ModContent.MARKET_ITEM_ID.identifier(), ModContent.marketItem());
        Registry.register(BuiltInRegistries.MENU, ModContent.MENU_ID.identifier(), ModContent.menu());
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ModContent.TAB_ID.identifier(),
                ModContent.titled(FabricCreativeModeTab.builder())
                        .displayItems((parameters, output) -> output.accept(ModContent.MARKET_ITEM))
                        .build());
        PayloadTypeRegistry.clientboundPlay().register(MarketPayloads.Offers.TYPE, MarketPayloads.Offers.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(MarketPayloads.Trade.TYPE, MarketPayloads.Trade.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(MarketPayloads.Trade.TYPE,
                (payload, context) -> context.server().execute(() -> MarketPayloads.handleTrade(context.player(), payload)));
        Constants.LOG.info("Villager Market Fabric init");
    }
}
