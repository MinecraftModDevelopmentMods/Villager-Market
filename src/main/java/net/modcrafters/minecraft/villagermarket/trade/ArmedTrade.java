package net.modcrafters.minecraft.villagermarket.trade;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

public final class ArmedTrade {
    private ArmedTrade() {
    }

    public static boolean sameKind(ItemStack first, ItemStack second) {
        return ItemStack.isSameItemSameComponents(first, second);
    }

    public static boolean matches(MerchantOffer offer, ItemStack costA, ItemStack costB, ItemStack result) {
        return matches(offer.getCostA(), offer.getCostB(), offer.getResult(), costA, costB, result);
    }

    public static boolean matches(ItemStack offerA, ItemStack offerB, ItemStack offerResult, ItemStack costA, ItemStack costB, ItemStack result) {
        if (costA.isEmpty() || result.isEmpty() || offerA.isEmpty() || offerResult.isEmpty()) {
            return false;
        }
        if (!sameKind(offerResult, result) || !sameKind(offerA, costA)) {
            return false;
        }
        if (costB.isEmpty()) {
            return offerB.isEmpty();
        }
        return sameKind(offerB, costB);
    }
}
