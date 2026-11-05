package net.modcrafters.minecraft.villagermarket.platform.services;

import net.modcrafters.minecraft.villagermarket.menu.MarketAutomatonMenu;
import net.modcrafters.minecraft.villagermarket.block.entity.MarketAutomatonBlockEntity;
import net.modcrafters.minecraft.villagermarket.menu.VillagerMarketMenu;
import net.modcrafters.minecraft.villagermarket.network.MarketPayloads;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;

public interface PlatformHelper {
    String platformName();

    MenuType<VillagerMarketMenu> marketMenu();

    MenuType<MarketAutomatonMenu> automatonMenu();

    BlockEntityType<MarketAutomatonBlockEntity> automatonType();

    void openMarket(ServerPlayer player, BlockPos pos);

    void openAutomaton(ServerPlayer player, BlockPos pos);

    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

    void sendToServer(CustomPacketPayload payload);
}
