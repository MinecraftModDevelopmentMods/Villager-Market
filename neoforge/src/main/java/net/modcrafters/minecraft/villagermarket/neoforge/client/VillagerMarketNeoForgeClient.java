package net.modcrafters.minecraft.villagermarket.neoforge.client;

import net.modcrafters.minecraft.villagermarket.ModContent;
import net.modcrafters.minecraft.villagermarket.client.VillagerMarketScreen;
import net.modcrafters.minecraft.villagermarket.network.MarketPayloads;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
//? if >=1.21.6
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

public final class VillagerMarketNeoForgeClient {
    private VillagerMarketNeoForgeClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(VillagerMarketNeoForgeClient::registerScreens);
        //? if >=1.21.6
        modBus.addListener(VillagerMarketNeoForgeClient::registerPayloads);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModContent.menu(), VillagerMarketScreen::new);
    }

    //? if >=1.21.6 {
    private static void registerPayloads(RegisterClientPayloadHandlersEvent event) {
        event.register(MarketPayloads.Offers.TYPE, (payload, context) -> VillagerMarketScreen.apply(payload));
    }
    //?}
}
