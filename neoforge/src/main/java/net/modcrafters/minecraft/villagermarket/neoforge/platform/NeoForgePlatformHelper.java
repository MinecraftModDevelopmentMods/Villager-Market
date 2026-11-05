package net.modcrafters.minecraft.villagermarket.neoforge.platform;

import net.modcrafters.minecraft.villagermarket.ModContent;
import net.modcrafters.minecraft.villagermarket.block.entity.MarketAutomatonBlockEntity;
import net.modcrafters.minecraft.villagermarket.menu.AutomatonOpening;
import net.modcrafters.minecraft.villagermarket.menu.MarketAutomatonMenu;
import net.modcrafters.minecraft.villagermarket.menu.OpeningData;
import net.modcrafters.minecraft.villagermarket.menu.VillagerMarketMenu;
import net.modcrafters.minecraft.villagermarket.platform.services.PlatformHelper;
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
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.PacketDistributor;
//? if >=1.21.6
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class NeoForgePlatformHelper implements PlatformHelper {
    @Override
    public String platformName() {
        return "NeoForge";
    }

    @Override
    public MenuType<VillagerMarketMenu> marketMenu() {
        return IMenuTypeExtension.create((containerId, inventory, buf) ->
                new VillagerMarketMenu(containerId, inventory, OpeningData.STREAM_CODEC.decode(buf)));
    }

    @Override
    public MenuType<MarketAutomatonMenu> automatonMenu() {
        return IMenuTypeExtension.create((containerId, inventory, buf) ->
                new MarketAutomatonMenu(containerId, inventory, AutomatonOpening.STREAM_CODEC.decode(buf)));
    }

    @Override
    public BlockEntityType<MarketAutomatonBlockEntity> automatonType() {
        //? if >=26.1 {
        return new BlockEntityType<>(MarketAutomatonBlockEntity::new, Set.of(ModContent.automatonBlock()));
        //?} else
        //return BlockEntityType.Builder.of(MarketAutomatonBlockEntity::new, ModContent.automatonBlock()).build(null);
    }

    @Override
    public void openMarket(ServerPlayer player, BlockPos pos) {
        OpeningData data = VillagerMarketMenu.opening((ServerLevel) player.level(), pos);
        player.openMenu(new net.minecraft.world.MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("block.villagermarket.villager_market");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
                return new VillagerMarketMenu(containerId, inventory, data);
            }
        }, buf -> OpeningData.STREAM_CODEC.encode(buf, data));
    }

    @Override
    public void openAutomaton(ServerPlayer player, BlockPos pos) {
        AutomatonOpening data = MarketAutomatonMenu.opening((ServerLevel) player.level(), pos);
        player.openMenu(new net.minecraft.world.MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("block.villagermarket.market_automaton");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
                return new MarketAutomatonMenu(containerId, inventory, data);
            }
        }, buf -> AutomatonOpening.STREAM_CODEC.encode(buf, data));
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        //? if >=1.21.6 {
        ClientPacketDistributor.sendToServer(payload);
        //?} else
        //PacketDistributor.sendToServer(payload);
    }
}
