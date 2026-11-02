package net.modcrafters.minecraft.villagermarket.trade;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TradeGroupsTest {
    @Test
    void aCustomNameWins() {
        assertEquals("Steve", TradeGroups.groupKey("Steve", "minecraft:farmer"));
    }

    @Test
    void aMissingNameUsesTheProfession() {
        assertEquals("minecraft:farmer", TradeGroups.groupKey(null, "minecraft:farmer"));
    }

    @Test
    void aBlankNameUsesTheProfession() {
        assertEquals("minecraft:farmer", TradeGroups.groupKey("  ", "minecraft:farmer"));
    }
}
