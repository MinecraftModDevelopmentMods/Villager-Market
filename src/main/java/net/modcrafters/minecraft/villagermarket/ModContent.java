package net.modcrafters.minecraft.villagermarket;

import net.modcrafters.minecraft.villagermarket.block.MarketBlock;
import net.modcrafters.minecraft.villagermarket.menu.VillagerMarketMenu;
import net.modcrafters.minecraft.villagermarket.platform.Services;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.inventory.MenuType;

public final class ModContent {
    public static final ResourceKey<net.minecraft.world.level.block.Block> MARKET_ID = ResourceKey.create(
            Registries.BLOCK, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "villager_market"));
    public static final ResourceKey<Item> MARKET_ITEM_ID = ResourceKey.create(
            Registries.ITEM, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "villager_market"));
    public static final ResourceKey<MenuType<?>> MENU_ID = ResourceKey.create(
            Registries.MENU, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "villager_market"));
    public static final ResourceKey<CreativeModeTab> TAB_ID = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "villagermarket"));

    public static MarketBlock MARKET;
    public static BlockItem MARKET_ITEM;

    private static MenuType<VillagerMarketMenu> menu;

    private ModContent() {
    }

    public static void init() {
        menu = Services.PLATFORM.marketMenu();
        Constants.LOG.info("{} common init", Constants.MOD_NAME);
    }

    public static MenuType<VillagerMarketMenu> menu() {
        return menu;
    }

    public static MarketBlock marketBlock() {
        if (MARKET == null) {
            MARKET = new MarketBlock(blockProperties());
        }
        return MARKET;
    }

    public static BlockItem marketItem() {
        if (MARKET_ITEM == null) {
            MARKET_ITEM = new BlockItem(marketBlock(), itemProperties());
        }
        return MARKET_ITEM;
    }

    public static CreativeModeTab.Builder titled(CreativeModeTab.Builder builder) {
        return builder.title(Component.translatable("itemGroup.villagermarket")).icon(() -> new ItemStack(MARKET_ITEM));
    }

    private static BlockBehaviour.Properties blockProperties() {
        var properties = BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BROWN)
                .sound(SoundType.WOOD)
                .strength(3.0F);
        //? if >=1.21.2
        properties = properties.setId(MARKET_ID);
        return properties;
    }

    private static Item.Properties itemProperties() {
        var properties = new Item.Properties();
        //? if >=1.21.2
        properties = properties.useBlockDescriptionPrefix();
        //? if >=1.21.2
        properties = properties.setId(MARKET_ITEM_ID);
        return properties;
    }
}
