package net.modcrafters.minecraft.villagermarket.fabric.platform;

import net.modcrafters.minecraft.villagermarket.menu.OpeningData;
import net.modcrafters.minecraft.villagermarket.menu.VillagerMarketMenu;
import net.modcrafters.minecraft.villagermarket.platform.services.PlatformHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class FabricPlatformHelper implements PlatformHelper {
    @Override
    public String platformName() {
        return "Fabric";
    }

    @Override
    public MenuType<VillagerMarketMenu> marketMenu() {
        return new ExtendedMenuType<>(VillagerMarketMenu::new, OpeningData.STREAM_CODEC);
    }

    @Override
    public void openMarket(ServerPlayer player, BlockPos pos) {
        OpeningData data = VillagerMarketMenu.opening((ServerLevel) player.level(), pos);
        player.openMenu(new ExtendedMenuProvider<OpeningData>() {
            @Override
            public OpeningData getScreenOpeningData(ServerPlayer player) {
                return data;
            }

            @Override
            public Component getDisplayName() {
                return Component.translatable("block.villagermarket.villager_market");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
                return new VillagerMarketMenu(containerId, inventory, data);
            }
        });
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
