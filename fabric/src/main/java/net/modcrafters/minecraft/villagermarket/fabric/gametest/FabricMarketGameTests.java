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
}
