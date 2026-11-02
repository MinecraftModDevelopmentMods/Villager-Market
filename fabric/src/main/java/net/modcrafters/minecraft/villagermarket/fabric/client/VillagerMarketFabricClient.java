package net.modcrafters.minecraft.villagermarket.fabric.client;

import net.modcrafters.minecraft.villagermarket.ModContent;
import net.modcrafters.minecraft.villagermarket.client.VillagerMarketScreen;
import net.modcrafters.minecraft.villagermarket.network.MarketPayloads;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.MenuScreens;

public class VillagerMarketFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModContent.menu(), VillagerMarketScreen::new);
        ClientPlayNetworking.registerGlobalReceiver(MarketPayloads.Offers.TYPE,
                (payload, context) -> context.client().execute(() -> VillagerMarketScreen.apply(payload)));
    }
}
