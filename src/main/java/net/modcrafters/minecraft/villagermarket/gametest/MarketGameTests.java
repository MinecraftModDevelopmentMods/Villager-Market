package net.modcrafters.minecraft.villagermarket.gametest;

import net.modcrafters.minecraft.villagermarket.search.MarketSearch;
import net.modcrafters.minecraft.villagermarket.trade.MainSlots;
import net.modcrafters.minecraft.villagermarket.trade.TradeExecutor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public final class MarketGameTests {
    public record Case(String name, Consumer<GameTestHelper> body) {
    }

    public static final List<Case> CASES = List.of(
            new Case("one_emerald_buys_bread", MarketGameTests::oneEmeraldBuysBread),
            new Case("components_must_match", MarketGameTests::componentsMustMatch),
            new Case("villager_outside_the_box_is_not_traded", MarketGameTests::villagerOutsideTheBoxIsNotTraded),
            new Case("adjusted_price_is_what_is_charged", MarketGameTests::adjustedPriceIsWhatIsCharged)
    );

    private MarketGameTests() {
    }

    public static void oneEmeraldBuysBread(GameTestHelper helper) {
        Villager villager = villager(helper, offer(new ItemCost(Items.EMERALD), new ItemStack(Items.BREAD), 4, 0.0f, 0));
        ServerPlayer player = player(helper);
        player.getInventory().add(new ItemStack(Items.EMERALD));

        TradeExecutor.Result result = TradeExecutor.execute(player, villager.blockPosition(), villager.getId(), 0, 1);

        helper.assertTrue(result.performed() == 1, "performed " + result.performed());
        helper.assertTrue(count(player, Items.BREAD) == 1, "bread " + count(player, Items.BREAD));
        helper.assertTrue(count(player, Items.EMERALD) == 0, "emeralds left " + count(player, Items.EMERALD));
        helper.succeed();
    }

    public static void componentsMustMatch(GameTestHelper helper) {
        ItemCost named = new ItemCost(Items.BOOK).withComponents(builder -> builder.expect(DataComponents.CUSTOM_NAME, Component.literal("ledger")));
        Villager villager = villager(helper, offer(named, new ItemStack(Items.BREAD), 4, 0.0f, 0));
        ServerPlayer player = player(helper);
        player.getInventory().add(new ItemStack(Items.BOOK));

        TradeExecutor.Result result = TradeExecutor.execute(player, villager.blockPosition(), villager.getId(), 0, 1);

        helper.assertTrue(result.performed() == 0, "a plain book paid for a named book");
        helper.assertTrue(count(player, Items.BOOK) == 1, "the book was taken");
        helper.succeed();
    }

    public static void villagerOutsideTheBoxIsNotTraded(GameTestHelper helper) {
        Villager villager = villager(helper, offer(new ItemCost(Items.EMERALD), new ItemStack(Items.BREAD), 4, 0.0f, 0));
        BlockPos market = villager.blockPosition();
        villager.setPos(villager.getX() + MarketSearch.RADIUS + 5, villager.getY(), villager.getZ());
        ServerPlayer player = player(helper);
        player.getInventory().add(new ItemStack(Items.EMERALD));

        TradeExecutor.Result result = TradeExecutor.execute(player, market, villager.getId(), 0, 1);

        helper.assertTrue(result.outOfRange(), "a villager past the box was traded");
        helper.assertTrue(count(player, Items.EMERALD) == 1, "payment was taken");
        helper.succeed();
    }

    public static void adjustedPriceIsWhatIsCharged(GameTestHelper helper) {
        MerchantOffer offer = offer(new ItemCost(Items.EMERALD), new ItemStack(Items.BREAD), 12, 0.2f, 100);
        int adjusted = offer.getCostA().getCount();
        if (adjusted <= 1) {
            helper.fail("adjusted price did not rise");
            return;
        }
        Villager villager = villager(helper, offer);
        ServerPlayer player = player(helper);
        player.getInventory().add(new ItemStack(Items.EMERALD, adjusted));

        TradeExecutor.Result result = TradeExecutor.execute(player, villager.blockPosition(), villager.getId(), 0, 1);

        helper.assertTrue(result.performed() == 1, "performed " + result.performed());
        helper.assertTrue(count(player, Items.EMERALD) == 0, "charged " + (adjusted - count(player, Items.EMERALD)) + " of " + adjusted);
        helper.succeed();
    }

    private static MerchantOffer offer(ItemCost cost, ItemStack result, int maxUses, float multiplier, int demand) {
        return new MerchantOffer(cost, Optional.empty(), result, 0, maxUses, 1, multiplier, demand);
    }

    private static Villager villager(GameTestHelper helper, MerchantOffer offer) {
        Villager villager = helper.spawn(EntityTypes.VILLAGER, 1, 2, 1);
        MerchantOffers offers = new MerchantOffers();
        offers.add(offer);
        villager.setOffers(offers);
        return villager;
    }

    private static ServerPlayer player(GameTestHelper helper) {
        return helper.makeMockServerPlayerInLevel();
    }

    private static int count(ServerPlayer player, Item item) {
        int total = 0;
        for (ItemStack stack : MainSlots.of(player.getInventory())) {
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }
}
