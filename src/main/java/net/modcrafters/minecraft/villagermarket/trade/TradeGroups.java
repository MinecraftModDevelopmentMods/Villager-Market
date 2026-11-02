package net.modcrafters.minecraft.villagermarket.trade;

public final class TradeGroups {
    private TradeGroups() {
    }

    public static String groupKey(String customName, String professionId) {
        if (customName != null && !customName.isBlank()) {
            return customName;
        }
        return professionId;
    }
}
