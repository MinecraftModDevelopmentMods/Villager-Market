package net.modcrafters.minecraft.villagermarket.fabric.gametest;

import net.modcrafters.minecraft.villagermarket.gametest.MarketGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class FabricMarketGameTests {
    @GameTest(structure = "minecraft:empty")
    public void oneEmeraldBuysBread(GameTestHelper helper) {
        MarketGameTests.oneEmeraldBuysBread(helper);
    }

    @GameTest(structure = "minecraft:empty")
    public void componentsMustMatch(GameTestHelper helper) {
        MarketGameTests.componentsMustMatch(helper);
    }

    @GameTest(structure = "minecraft:empty")
    public void villagerOutsideTheBoxIsNotTraded(GameTestHelper helper) {
        MarketGameTests.villagerOutsideTheBoxIsNotTraded(helper);
    }

    @GameTest(structure = "minecraft:empty")
    public void adjustedPriceIsWhatIsCharged(GameTestHelper helper) {
        MarketGameTests.adjustedPriceIsWhatIsCharged(helper);
    }

    @GameTest(structure = "minecraft:empty")
    public void experienceLandsOnThePlayer(GameTestHelper helper) {
        MarketGameTests.experienceLandsOnThePlayer(helper);
    }

    @GameTest(structure = "minecraft:empty")
    public void automatonTradesFromItsSlots(GameTestHelper helper) {
        MarketGameTests.automatonTradesFromItsSlots(helper);
    }

    @GameTest(structure = "minecraft:empty")
    public void automatonRejectsADifferentBook(GameTestHelper helper) {
        MarketGameTests.automatonRejectsADifferentBook(helper);
    }

    @GameTest(structure = "minecraft:empty")
    public void automatonStopsWithoutRedstone(GameTestHelper helper) {
        MarketGameTests.automatonStopsWithoutRedstone(helper);
    }

    @GameTest(structure = "minecraft:empty")
    public void automatonBuysTheCheapestSimilar(GameTestHelper helper) {
        MarketGameTests.automatonBuysTheCheapestSimilar(helper);
    }
}
