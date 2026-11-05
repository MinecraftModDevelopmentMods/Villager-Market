package net.modcrafters.minecraft.villagermarket.fabric.gametest;

import net.modcrafters.minecraft.villagermarket.gametest.MarketGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class FabricMarketGameTests {
    @GameTest(structure = "villagermarket:wide")
    public void oneEmeraldBuysBread(GameTestHelper helper) {
        MarketGameTests.oneEmeraldBuysBread(helper);
    }

    @GameTest(structure = "villagermarket:wide")
    public void componentsMustMatch(GameTestHelper helper) {
        MarketGameTests.componentsMustMatch(helper);
    }

    @GameTest(structure = "villagermarket:wide")
    public void villagerOutsideTheBoxIsNotTraded(GameTestHelper helper) {
        MarketGameTests.villagerOutsideTheBoxIsNotTraded(helper);
    }

    @GameTest(structure = "villagermarket:wide")
    public void adjustedPriceIsWhatIsCharged(GameTestHelper helper) {
        MarketGameTests.adjustedPriceIsWhatIsCharged(helper);
    }

    @GameTest(structure = "villagermarket:wide")
    public void experienceLandsOnThePlayer(GameTestHelper helper) {
        MarketGameTests.experienceLandsOnThePlayer(helper);
    }

    @GameTest(structure = "villagermarket:wide")
    public void automatonTradesFromItsSlots(GameTestHelper helper) {
        MarketGameTests.automatonTradesFromItsSlots(helper);
    }

    @GameTest(structure = "villagermarket:wide")
    public void automatonRejectsADifferentBook(GameTestHelper helper) {
        MarketGameTests.automatonRejectsADifferentBook(helper);
    }

    @GameTest(structure = "villagermarket:wide")
    public void automatonStopsWithoutRedstone(GameTestHelper helper) {
        MarketGameTests.automatonStopsWithoutRedstone(helper);
    }

    @GameTest(structure = "villagermarket:wide")
    public void automatonBuysTheCheapestSimilar(GameTestHelper helper) {
        MarketGameTests.automatonBuysTheCheapestSimilar(helper);
    }
}
