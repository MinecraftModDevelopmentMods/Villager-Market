package net.modcrafters.minecraft.villagermarket;

import net.modcrafters.minecraft.villagermarket.block.MarketAutomatonBlock;
import net.modcrafters.minecraft.villagermarket.block.MarketBlock;
import net.modcrafters.minecraft.villagermarket.block.entity.MarketAutomatonBlockEntity;
import net.modcrafters.minecraft.villagermarket.menu.MarketAutomatonMenu;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
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
    public static final ResourceKey<net.minecraft.world.level.block.Block> AUTOMATON_ID = ResourceKey.create(
            Registries.BLOCK, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "market_automaton"));
    public static final ResourceKey<Item> AUTOMATON_ITEM_ID = ResourceKey.create(
            Registries.ITEM, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "market_automaton"));
    public static final ResourceKey<MenuType<?>> AUTOMATON_MENU_ID = ResourceKey.create(
            Registries.MENU, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "market_automaton"));
    public static final ResourceKey<CreativeModeTab> TAB_ID = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "villagermarket"));

    public static MarketBlock MARKET;
    public static BlockItem MARKET_ITEM;
    public static MarketAutomatonBlock AUTOMATON;
    public static BlockItem AUTOMATON_ITEM;
    public static BlockEntityType<MarketAutomatonBlockEntity> AUTOMATON_TYPE;

    private static MenuType<VillagerMarketMenu> menu;
    private static MenuType<MarketAutomatonMenu> automatonMenu;

    private ModContent() {
    }

    public static void init() {
        menu = Services.PLATFORM.marketMenu();
        automatonMenu = Services.PLATFORM.automatonMenu();
        Constants.LOG.info("{} common init", Constants.MOD_NAME);
    }

    public static MenuType<VillagerMarketMenu> menu() {
        return menu;
    }

    public static MenuType<MarketAutomatonMenu> automatonMenu() {
        return automatonMenu;
    }

    public static MarketBlock marketBlock() {
        if (MARKET == null) {
            MARKET = new MarketBlock(wood(MARKET_ID));
        }
        return MARKET;
    }

    public static BlockItem marketItem() {
        if (MARKET_ITEM == null) {
            MARKET_ITEM = new BlockItem(marketBlock(), item(MARKET_ITEM_ID));
        }
        return MARKET_ITEM;
    }

    public static MarketAutomatonBlock automatonBlock() {
        if (AUTOMATON == null) {
            AUTOMATON = new MarketAutomatonBlock(wood(AUTOMATON_ID));
        }
        return AUTOMATON;
    }

    public static BlockItem automatonItem() {
        if (AUTOMATON_ITEM == null) {
            AUTOMATON_ITEM = new BlockItem(automatonBlock(), item(AUTOMATON_ITEM_ID));
        }
        return AUTOMATON_ITEM;
    }

    public static BlockEntityType<MarketAutomatonBlockEntity> automatonType() {
        if (AUTOMATON_TYPE == null) {
            AUTOMATON_TYPE = Services.PLATFORM.automatonType();
        }
        return AUTOMATON_TYPE;
    }

    public static CreativeModeTab.Builder titled(CreativeModeTab.Builder builder) {
        return builder.title(Component.translatable("itemGroup.villagermarket")).icon(() -> new ItemStack(MARKET_ITEM));
    }

    private static BlockBehaviour.Properties wood(ResourceKey<net.minecraft.world.level.block.Block> id) {
        var properties = BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BROWN)
                .sound(SoundType.WOOD)
                .strength(3.0F);
        //? if >=1.21.2
        properties = properties.setId(id);
        return properties;
    }

    private static Item.Properties item(ResourceKey<Item> id) {
        var properties = new Item.Properties();
        //? if >=1.21.2
        properties = properties.useBlockDescriptionPrefix();
        //? if >=1.21.2
        properties = properties.setId(id);
        return properties;
    }
}
