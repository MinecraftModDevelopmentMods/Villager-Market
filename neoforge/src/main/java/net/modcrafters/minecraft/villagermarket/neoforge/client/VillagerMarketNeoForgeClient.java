package net.modcrafters.minecraft.villagermarket.neoforge.client;

import net.modcrafters.minecraft.villagermarket.ModContent;
import net.modcrafters.minecraft.villagermarket.client.MarketAutomatonScreen;
import net.modcrafters.minecraft.villagermarket.client.VillagerMarketScreen;
import net.modcrafters.minecraft.villagermarket.network.AutomatonPayloads;
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
        event.register(ModContent.automatonMenu(), MarketAutomatonScreen::new);
    }

    //? if >=1.21.6 {
    private static void registerPayloads(RegisterClientPayloadHandlersEvent event) {
        event.register(MarketPayloads.Offers.TYPE, (payload, context) -> {
            VillagerMarketScreen.apply(payload);
            MarketAutomatonScreen.applyOffers(payload);
        });
        event.register(AutomatonPayloads.Recipe.TYPE, (payload, context) -> MarketAutomatonScreen.apply(payload));
        event.register(AutomatonPayloads.Mode.TYPE, (payload, context) -> MarketAutomatonScreen.applyMode(payload));
        event.register(AutomatonPayloads.SimilarState.TYPE, (payload, context) -> MarketAutomatonScreen.applySimilar(payload));
    }
    //?}
}
