package net.modcrafters.minecraft.villagermarket.platform.services;

import net.modcrafters.minecraft.villagermarket.menu.VillagerMarketMenu;
import net.modcrafters.minecraft.villagermarket.network.MarketPayloads;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;

public interface PlatformHelper {
    String platformName();

    MenuType<VillagerMarketMenu> marketMenu();

    void openMarket(ServerPlayer player, BlockPos pos);

    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

    void sendToServer(CustomPacketPayload payload);
}
