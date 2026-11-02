package net.modcrafters.minecraft.villagermarket.trade;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TradeMathTest {
    @Test
    void dividesTheFirstCostAndCapsAtRemainingUses() {
        assertEquals(3, TradeMath.affordableUses(10, 3, 0, 1, false, 5));
    }

    @Test
    void theSecondCostCanBeTheLimit() {
        assertEquals(2, TradeMath.affordableUses(10, 3, 4, 2, true, 100));
    }

    @Test
    void remainingUsesCapTheResult() {
        assertEquals(1, TradeMath.affordableUses(64, 1, 64, 1, true, 1));
    }

    @Test
    void aMissingSecondCostIsIgnored() {
        assertEquals(3, TradeMath.affordableUses(10, 3, 0, 0, false, 5));
    }

    @Test
    void aZeroFirstCostPaysForNothing() {
        assertEquals(0, TradeMath.affordableUses(10, 0, 0, 1, false, 5));
    }

    @Test
    void aZeroSecondCostPaysForNothing() {
        assertEquals(0, TradeMath.affordableUses(10, 3, 4, 0, true, 5));
    }

    @Test
    void aNegativeCountIsEmpty() {
        assertEquals(0, TradeMath.affordableUses(-1, 1, 0, 1, false, 5));
    }

    @Test
    void noUsesLeftPaysForNothing() {
        assertEquals(0, TradeMath.affordableUses(10, 3, 0, 1, false, 0));
    }
}
