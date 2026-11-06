package net.modcrafters.minecraft.villagermarket.gametest;

import net.modcrafters.minecraft.villagermarket.ModContent;
import net.modcrafters.minecraft.villagermarket.block.entity.MarketAutomatonBlockEntity;
import net.modcrafters.minecraft.villagermarket.block.entity.RedstoneMode;
import net.modcrafters.minecraft.villagermarket.menu.MachineSlots;
import net.modcrafters.minecraft.villagermarket.trade.MainSlots;
import net.modcrafters.minecraft.villagermarket.trade.TradeExecutor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
//? if >=26.1
import net.minecraft.world.level.GameType;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public final class MarketGameTests {
    public record Case(String name, Consumer<GameTestHelper> body) {
    }

    public static final List<Case> CASES = List.of(
            new Case("one_emerald_buys_bread", MarketGameTests::oneEmeraldBuysBread),
            new Case("components_must_match", MarketGameTests::componentsMustMatch),
            new Case("villager_outside_the_village_is_not_traded", MarketGameTests::villagerOutsideTheVillageIsNotTraded),
            new Case("distant_villager_with_a_village_job_is_traded", MarketGameTests::distantVillagerWithAVillageJobIsTraded),
            new Case("adjusted_price_is_what_is_charged", MarketGameTests::adjustedPriceIsWhatIsCharged),
            new Case("experience_lands_on_the_player", MarketGameTests::experienceLandsOnThePlayer),
            new Case("automaton_trades_from_its_slots", MarketGameTests::automatonTradesFromItsSlots),
            new Case("automaton_rejects_a_different_book", MarketGameTests::automatonRejectsADifferentBook),
            new Case("automaton_stops_without_redstone", MarketGameTests::automatonStopsWithoutRedstone),
            new Case("automaton_buys_the_cheapest_similar", MarketGameTests::automatonBuysTheCheapestSimilar),
            new Case("automaton_under_a_job_site_trades_with_its_owner", MarketGameTests::automatonUnderAJobSiteTradesWithItsOwner),
            new Case("unclaimed_job_site_does_not_trade", MarketGameTests::unclaimedJobSiteDoesNotTrade)
    );

    private MarketGameTests() {
    }

    public static void oneEmeraldBuysBread(GameTestHelper helper) {
        Villager villager = villager(helper, offer(new ItemCost(Items.EMERALD), new ItemStack(Items.BREAD), 4, 0.0f, 0));
        ServerPlayer player = player(helper);
        player.getInventory().add(new ItemStack(Items.EMERALD));

        TradeExecutor.Result result = TradeExecutor.execute(player, villager.blockPosition(), villager.getId(), 0, 1);

        helper.assertTrue(result.performed() == 1, "performed " + result.performed() + " outOfRange " + result.outOfRange());
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

    public static void villagerOutsideTheVillageIsNotTraded(GameTestHelper helper) {
        Villager villager = villager(helper, offer(new ItemCost(Items.EMERALD), new ItemStack(Items.BREAD), 4, 0.0f, 0));
        claim(helper, villager, 65, 1, 1);
        ServerPlayer player = player(helper);
        player.getInventory().add(new ItemStack(Items.EMERALD));

        TradeExecutor.Result result = TradeExecutor.execute(player, villager.blockPosition(), villager.getId(), 0, 1);

        helper.assertTrue(result.outOfRange(), "a villager from another hall was traded");
        helper.assertTrue(count(player, Items.EMERALD) == 1, "payment was taken");
        helper.succeed();
    }

    public static void distantVillagerWithAVillageJobIsTraded(GameTestHelper helper) {
        Villager villager = villager(helper, offer(new ItemCost(Items.EMERALD), new ItemStack(Items.BREAD), 4, 0.0f, 0));
        BlockPos market = villager.blockPosition();
        villager.setPos(villager.getX() + 80, villager.getY(), villager.getZ());
        ServerPlayer player = player(helper);
        player.getInventory().add(new ItemStack(Items.EMERALD));

        TradeExecutor.Result result = TradeExecutor.execute(player, market, villager.getId(), 0, 1);

        helper.assertTrue(result.performed() == 1, "a villager tied to the village was skipped");
        helper.assertTrue(count(player, Items.BREAD) == 1, "bread " + count(player, Items.BREAD));
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

    public static void experienceLandsOnThePlayer(GameTestHelper helper) {
        Villager villager = villager(helper, offer(new ItemCost(Items.EMERALD), new ItemStack(Items.BREAD), 4, 0.0f, 0));
        ServerPlayer player = player(helper);
        player.setPos(villager.getX() + 8, villager.getY(), villager.getZ());
        player.getInventory().add(new ItemStack(Items.EMERALD));
        AABB area = villager.getBoundingBox().minmax(player.getBoundingBox()).inflate(2);
        Set<Integer> before = orbIds(helper.getLevel(), area);

        TradeExecutor.Result result = TradeExecutor.execute(player, villager.blockPosition(), villager.getId(), 0, 1);

        helper.assertTrue(result.performed() == 1, "performed " + result.performed());
        assertRewardMoved(helper, area, before, player.position(), villager.position(), "player");
        helper.succeed();
    }

    public static void automatonTradesFromItsSlots(GameTestHelper helper) {
        BlockPos at = new BlockPos(6, 1, 1);
        helper.setBlock(at, ModContent.automatonBlock());
        // The game tests share one world, so this trade is diamonds rather than emeralds.
        Villager villager = villager(helper, offer(new ItemCost(Items.DIAMOND), new ItemStack(Items.BREAD), 2, 0.0f, 0));
        MarketAutomatonBlockEntity machine = machine(helper, at);
        MerchantOffer deal = villager.getOffers().get(0);
        machine.arm(deal.getCostA(), ItemStack.EMPTY, deal.getResult());
        machine.setItem(MachineSlots.INPUT_A, new ItemStack(Items.DIAMOND, 3));
        BlockPos absolute = helper.absolutePos(at);
        AABB area = new AABB(absolute).inflate(8);
        Set<Integer> before = orbIds(helper.getLevel(), area);
        tick(helper, machine, absolute);
        helper.assertTrue(machine.getItem(MachineSlots.OUTPUT).is(Items.BREAD) && machine.getItem(MachineSlots.OUTPUT).getCount() == 1, "first bread");
        helper.assertTrue(machine.getItem(MachineSlots.INPUT_A).getCount() == 2, "diamonds after one trade");
        helper.assertTrue(!machine.canTakeItemThroughFace(MachineSlots.INPUT_A, machine.getItem(MachineSlots.INPUT_A), Direction.NORTH), "a side hopper pulled the payment");
        helper.assertTrue(machine.canTakeItemThroughFace(MachineSlots.OUTPUT, machine.getItem(MachineSlots.OUTPUT), Direction.DOWN), "the bottom could not take the result");
        assertRewardMoved(helper, area, before, Vec3.atCenterOf(absolute), villager.position(), "automaton");
        tick(helper, machine, absolute);
        helper.assertTrue(machine.getItem(MachineSlots.OUTPUT).getCount() == 2, "second bread");
        helper.assertTrue(machine.getItem(MachineSlots.INPUT_A).getCount() == 1, "the locked trade kept spending");
        tick(helper, machine, absolute);
        helper.assertTrue(machine.getItem(MachineSlots.OUTPUT).getCount() == 2, "a locked trade produced more bread");
        helper.assertTrue(machine.getItem(MachineSlots.INPUT_A).getCount() == 1, "a locked trade took the last diamond");
        helper.succeed();
    }

    public static void automatonRejectsADifferentBook(GameTestHelper helper) {
        BlockPos at = new BlockPos(6, 1, 1);
        helper.setBlock(at, ModContent.automatonBlock());
        ItemCost named = new ItemCost(Items.BOOK).withComponents(builder -> builder.expect(DataComponents.CUSTOM_NAME, Component.literal("ledger")));
        MerchantOffer deal = new MerchantOffer(new ItemCost(Items.EMERALD), Optional.of(named), new ItemStack(Items.BREAD), 0, 4, 1, 0.0f, 0);
        villager(helper, deal);
        MarketAutomatonBlockEntity machine = machine(helper, at);
        machine.arm(deal.getCostA(), deal.getCostB(), deal.getResult());
        machine.setItem(MachineSlots.INPUT_A, new ItemStack(Items.EMERALD));
        machine.setItem(MachineSlots.INPUT_B, new ItemStack(Items.BOOK));
        tick(helper, machine, helper.absolutePos(at));
        helper.assertTrue(machine.getItem(MachineSlots.OUTPUT).isEmpty(), "a plain book paid for a named book");
        helper.assertTrue(machine.getItem(MachineSlots.INPUT_B).is(Items.BOOK), "the plain book was taken");
        helper.succeed();
    }

    public static void automatonStopsWithoutRedstone(GameTestHelper helper) {
        BlockPos at = new BlockPos(6, 1, 1);
        helper.setBlock(at, ModContent.automatonBlock());
        Villager villager = villager(helper, offer(new ItemCost(Items.AMETHYST_SHARD), new ItemStack(Items.BREAD), 4, 0.0f, 0));
        MarketAutomatonBlockEntity machine = machine(helper, at);
        MerchantOffer deal = villager.getOffers().get(0);
        machine.arm(deal.getCostA(), ItemStack.EMPTY, deal.getResult());
        machine.setItem(MachineSlots.INPUT_A, new ItemStack(Items.AMETHYST_SHARD, 2));
        machine.cycleRedstone();
        BlockPos absolute = helper.absolutePos(at);
        tick(helper, machine, absolute);
        helper.assertTrue(machine.redstone() == RedstoneMode.POWERED, "mode did not move to powered");
        helper.assertTrue(machine.getItem(MachineSlots.OUTPUT).isEmpty(), "traded with no redstone signal");
        helper.setBlock(at.east(), Blocks.REDSTONE_BLOCK);
        tick(helper, machine, absolute);
        helper.assertTrue(machine.getItem(MachineSlots.OUTPUT).is(Items.BREAD), "did not trade while powered");
        machine.cycleRedstone();
        machine.setItem(MachineSlots.OUTPUT, ItemStack.EMPTY);
        tick(helper, machine, absolute);
        helper.assertTrue(machine.redstone() == RedstoneMode.UNPOWERED, "mode did not move to unpowered");
        helper.assertTrue(machine.getItem(MachineSlots.OUTPUT).isEmpty(), "traded while a signal blocked the unpowered mode");
        helper.succeed();
    }

    public static void automatonBuysTheCheapestSimilar(GameTestHelper helper) {
        BlockPos at = new BlockPos(6, 1, 1);
        helper.setBlock(at, ModContent.automatonBlock());
        MerchantOffer cheapDeal = offer(new ItemCost(Items.ECHO_SHARD), new ItemStack(Items.BREAD), 4, 0.0f, 0);
        MerchantOffer priceyDeal = offer(new ItemCost(Items.ECHO_SHARD), new ItemStack(Items.BREAD), 4, 0.2f, 100);
        int cheapCount = cheapDeal.getCostA().getCount();
        int priceyCount = priceyDeal.getCostA().getCount();
        helper.assertTrue(priceyCount > cheapCount, "the expensive offer was not more expensive");
        Villager cheap = villager(helper, cheapDeal);
        Villager pricey = villager(helper, priceyDeal);
        MarketAutomatonBlockEntity machine = machine(helper, at);
        machine.arm(priceyDeal.getCostA(), ItemStack.EMPTY, priceyDeal.getResult());
        machine.setSimilar(true);
        machine.setItem(MachineSlots.INPUT_A, new ItemStack(Items.ECHO_SHARD, priceyCount));
        tick(helper, machine, helper.absolutePos(at));
        helper.assertTrue(cheap.getOffers().get(0).getUses() == 1, "did not buy the cheaper offer");
        helper.assertTrue(pricey.getOffers().get(0).getUses() == 0, "bought the expensive offer");
        helper.assertTrue(machine.getItem(MachineSlots.INPUT_A).getCount() == priceyCount - cheapCount, "did not pay the cheaper price");
        helper.succeed();
    }

    public static void automatonUnderAJobSiteTradesWithItsOwner(GameTestHelper helper) {
        BlockPos at = new BlockPos(6, 1, 1);
        helper.setBlock(at, ModContent.automatonBlock());
        MerchantOffer ownerDeal = offer(new ItemCost(Items.PRISMARINE_CRYSTALS), new ItemStack(Items.BREAD), 4, 0.2f, 100);
        MerchantOffer neighborDeal = offer(new ItemCost(Items.PRISMARINE_CRYSTALS), new ItemStack(Items.BREAD), 4, 0.0f, 0);
        int ownerCount = ownerDeal.getCostA().getCount();
        int neighborCount = neighborDeal.getCostA().getCount();
        helper.assertTrue(ownerCount > neighborCount, "the owner's offer was not more expensive");
        Villager owner = villagerAt(helper, ownerDeal, 2, 2, 1);
        claim(helper, owner, 6, 2, 1);
        Villager neighbor = villagerAt(helper, neighborDeal, 3, 2, 1);
        MarketAutomatonBlockEntity machine = machine(helper, at);
        machine.arm(ownerDeal.getCostA(), ItemStack.EMPTY, ownerDeal.getResult());
        machine.setSimilar(true);
        machine.setItem(MachineSlots.INPUT_A, new ItemStack(Items.PRISMARINE_CRYSTALS, ownerCount));
        tick(helper, machine, helper.absolutePos(at));
        helper.assertTrue(owner.getOffers().get(0).getUses() == 1, "did not buy from the workstation's owner");
        helper.assertTrue(neighbor.getOffers().get(0).getUses() == 0, "bought from another villager in the village");
        helper.assertTrue(machine.getItem(MachineSlots.INPUT_A).isEmpty(), "did not pay the owner's price");
        helper.succeed();
    }

    public static void unclaimedJobSiteDoesNotTrade(GameTestHelper helper) {
        BlockPos at = new BlockPos(6, 1, 1);
        helper.setBlock(at, ModContent.automatonBlock());
        helper.setBlock(new BlockPos(6, 2, 1), Blocks.LECTERN);
        Villager villager = villager(helper, offer(new ItemCost(Items.RABBIT_FOOT), new ItemStack(Items.BREAD), 4, 0.0f, 0));
        MarketAutomatonBlockEntity machine = machine(helper, at);
        MerchantOffer deal = villager.getOffers().get(0);
        machine.arm(deal.getCostA(), ItemStack.EMPTY, deal.getResult());
        machine.setItem(MachineSlots.INPUT_A, new ItemStack(Items.RABBIT_FOOT));
        tick(helper, machine, helper.absolutePos(at));
        helper.assertTrue(machine.getItem(MachineSlots.OUTPUT).isEmpty(), "an unclaimed workstation traded");
        helper.assertTrue(villager.getOffers().get(0).getUses() == 0, "the nearby villager was traded");
        helper.succeed();
    }

    private static Set<Integer> orbIds(Level level, AABB area) {
        Set<Integer> ids = new HashSet<>();
        for (ExperienceOrb orb : level.getEntitiesOfClass(ExperienceOrb.class, area)) {
            ids.add(orb.getId());
        }
        return ids;
    }

    private static void assertRewardMoved(GameTestHelper helper, AABB area, Set<Integer> before, Vec3 destination, Vec3 origin, String where) {
        boolean arrived = false;
        boolean stayed = false;
        for (ExperienceOrb orb : helper.getLevel().getEntitiesOfClass(ExperienceOrb.class, area)) {
            if (before.contains(orb.getId())) {
                continue;
            }
            if (orb.distanceToSqr(destination) < 4.0) {
                arrived = true;
            }
            if (orb.distanceToSqr(origin) < 4.0) {
                stayed = true;
            }
        }
        helper.assertTrue(arrived, "the experience orb was not on the " + where);
        helper.assertTrue(!stayed, "the experience orb stayed on the villager");
    }

    private static MarketAutomatonBlockEntity machine(GameTestHelper helper, BlockPos at) {
        //? if >=26.1 {
        return helper.getBlockEntity(at, MarketAutomatonBlockEntity.class);
        //?} else
        //return (MarketAutomatonBlockEntity) helper.getBlockEntity(at);
    }

    private static void tick(GameTestHelper helper, MarketAutomatonBlockEntity machine, BlockPos absolute) {
        for (int i = 0; i < MachineSlots.COOLDOWN; i++) {
            MarketAutomatonBlockEntity.serverTick(helper.getLevel(), absolute, machine.getBlockState(), machine);
        }
    }

    private static MerchantOffer offer(ItemCost cost, ItemStack result, int maxUses, float multiplier, int demand) {
        return new MerchantOffer(cost, Optional.empty(), result, 0, maxUses, 1, multiplier, demand);
    }

    private static Villager villager(GameTestHelper helper, MerchantOffer offer) {
        return villagerAt(helper, offer, 1, 2, 1);
    }

    private static Villager villagerAt(GameTestHelper helper, MerchantOffer offer, int x, int y, int z) {
        Villager villager = helper.spawn(EntityTypes.VILLAGER, x, y, z);
        MerchantOffers offers = new MerchantOffers();
        offers.add(offer);
        villager.setOffers(offers);
        claim(helper, villager, x, y - 1, z);
        return villager;
    }

    private static void claim(GameTestHelper helper, Villager villager, int x, int y, int z) {
        BlockPos relative = new BlockPos(x, y, z);
        helper.setBlock(relative, Blocks.LECTERN);
        villager.getBrain().setMemory(MemoryModuleType.JOB_SITE,
                GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(relative)));
    }

    @SuppressWarnings("removal")
    private static ServerPlayer player(GameTestHelper helper) {
        //? if >=26.1 {
        return (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);
        //?} else
        //return helper.makeMockServerPlayerInLevel();
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
