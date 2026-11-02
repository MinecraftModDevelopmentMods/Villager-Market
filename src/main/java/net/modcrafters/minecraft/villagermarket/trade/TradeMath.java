package net.modcrafters.minecraft.villagermarket.trade;

public final class TradeMath {
    private TradeMath() {
    }

    public static int affordableUses(int haveA, int perA, int haveB, int perB, boolean hasSecond, int remainingUses) {
        if (perA <= 0 || remainingUses <= 0 || (hasSecond && perB <= 0)) {
            return 0;
        }
        int usesA = Math.max(haveA, 0) / perA;
        int usesB = hasSecond ? Math.max(haveB, 0) / perB : Integer.MAX_VALUE;
        return Math.min(usesA, Math.min(usesB, remainingUses));
    }
}
