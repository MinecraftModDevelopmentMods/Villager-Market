package net.modcrafters.minecraft.villagermarket.trade;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class MainSlots {
    private MainSlots() {
    }

    public static Iterable<ItemStack> of(Inventory inventory) {
        //? if >=26.1 {
        return inventory.getNonEquipmentItems();
        //?} else
        //return inventory.items;
    }
}
