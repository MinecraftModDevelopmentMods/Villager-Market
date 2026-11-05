package net.modcrafters.minecraft.villagermarket.fabric.platform;

import net.modcrafters.minecraft.villagermarket.ModContent;
import net.modcrafters.minecraft.villagermarket.block.entity.MarketAutomatonBlockEntity;
import net.modcrafters.minecraft.villagermarket.menu.AutomatonOpening;
import net.modcrafters.minecraft.villagermarket.menu.MarketAutomatonMenu;
import net.modcrafters.minecraft.villagermarket.menu.OpeningData;
import net.modcrafters.minecraft.villagermarket.menu.VillagerMarketMenu;
import net.modcrafters.minecraft.villagermarket.platform.services.PlatformHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
//? if <26.1
/*import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;*/
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import java.util.Set;

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
    public MenuType<MarketAutomatonMenu> automatonMenu() {
        return new ExtendedMenuType<>(MarketAutomatonMenu::new, AutomatonOpening.STREAM_CODEC);
    }

    @Override
    public BlockEntityType<MarketAutomatonBlockEntity> automatonType() {
        //? if >=26.1 {
        return new BlockEntityType<>(MarketAutomatonBlockEntity::new, Set.of(ModContent.automatonBlock()));
        //?} else
        //return FabricBlockEntityTypeBuilder.create(MarketAutomatonBlockEntity::new, ModContent.automatonBlock()).build();
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
    public void openAutomaton(ServerPlayer player, BlockPos pos) {
        AutomatonOpening data = MarketAutomatonMenu.opening((ServerLevel) player.level(), pos);
        player.openMenu(new ExtendedMenuProvider<AutomatonOpening>() {
            @Override
            public AutomatonOpening getScreenOpeningData(ServerPlayer player) {
                return data;
            }

            @Override
            public Component getDisplayName() {
                return Component.translatable("block.villagermarket.market_automaton");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
                return new MarketAutomatonMenu(containerId, inventory, data);
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
