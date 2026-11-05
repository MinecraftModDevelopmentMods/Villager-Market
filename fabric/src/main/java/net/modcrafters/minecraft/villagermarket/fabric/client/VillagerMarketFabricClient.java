package net.modcrafters.minecraft.villagermarket.fabric.client;

import net.modcrafters.minecraft.villagermarket.ModContent;
import net.modcrafters.minecraft.villagermarket.client.MarketAutomatonScreen;
import net.modcrafters.minecraft.villagermarket.client.VillagerMarketScreen;
import net.modcrafters.minecraft.villagermarket.network.AutomatonPayloads;
import net.modcrafters.minecraft.villagermarket.network.MarketPayloads;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.MenuScreens;

public class VillagerMarketFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModContent.menu(), VillagerMarketScreen::new);
        MenuScreens.register(ModContent.automatonMenu(), MarketAutomatonScreen::new);
        ClientPlayNetworking.registerGlobalReceiver(MarketPayloads.Offers.TYPE,
                (payload, context) -> context.client().execute(() -> {
                    VillagerMarketScreen.apply(payload);
                    MarketAutomatonScreen.applyOffers(payload);
                }));
        ClientPlayNetworking.registerGlobalReceiver(AutomatonPayloads.Recipe.TYPE,
                (payload, context) -> context.client().execute(() -> MarketAutomatonScreen.apply(payload)));
        ClientPlayNetworking.registerGlobalReceiver(AutomatonPayloads.Mode.TYPE,
                (payload, context) -> context.client().execute(() -> MarketAutomatonScreen.applyMode(payload)));
        ClientPlayNetworking.registerGlobalReceiver(AutomatonPayloads.SimilarState.TYPE,
                (payload, context) -> context.client().execute(() -> MarketAutomatonScreen.applySimilar(payload)));
    }
}
