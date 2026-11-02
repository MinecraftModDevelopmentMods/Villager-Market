package net.modcrafters.minecraft.villagermarket.client;

import net.modcrafters.minecraft.villagermarket.trade.ListedOffer;
import net.modcrafters.minecraft.villagermarket.trade.MainSlots;
import net.modcrafters.minecraft.villagermarket.trade.TradeMath;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

// Hit testing and the Show All filter. Both screens draw this; neither one decides the rules.
public final class MarketScreenLogic {
    public static final int WIDTH = 256;
    public static final int HEIGHT = 209;
    public static final int GROUP_X = 6;
    public static final int GROUP_Y = 6;
    public static final int GROUP_W = 88;
    public static final int GROUP_H = 140;
    public static final int OFFER_X = 100;
    public static final int OFFER_Y = 6;
    public static final int OFFER_W = 149;
    public static final int OFFER_H = 165;
    public static final int OFFER_ROW = 28;
    public static final int SHOW_X = 5;
    public static final int SHOW_Y = 152;
    public static final int SHOW_W = 90;
    public static final int SHOW_H = 20;
    public static final int SELECTED_Y = 176;
    public static final int ONCE_X = 156;
    public static final int MAX_X = 196;
    public static final int BUTTON_Y = 180;
    public static final int BUTTON_W = 35;
    public static final int BUTTON_H = 20;

    public boolean showAll = true;
    public String selectedGroup = "";
    public int groupScroll;
    public int offerScroll;
    public ListedOffer selected;

    public List<String> groups(List<ListedOffer> offers) {
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        for (ListedOffer offer : offers) {
            keys.add(offer.groupKey());
        }
        return new ArrayList<>(keys);
    }

    public List<ListedOffer> visible(List<ListedOffer> offers, Inventory inventory) {
        List<ListedOffer> shown = new ArrayList<>();
        for (ListedOffer offer : offers) {
            if (!selectedGroup.isEmpty() && !selectedGroup.equals(offer.groupKey())) {
                continue;
            }
            if (!showAll && affordable(offer, inventory) <= 0) {
                continue;
            }
            shown.add(offer);
        }
        return shown;
    }

    public int affordable(ListedOffer offer, Inventory inventory) {
        boolean hasSecond = !offer.costB().isEmpty();
        return TradeMath.affordableUses(
                count(inventory, offer.costA()), offer.costA().getCount(),
                hasSecond ? count(inventory, offer.costB()) : 0, offer.costB().getCount(),
                hasSecond, offer.usesLeft());
    }

    public void onOffers(List<ListedOffer> offers) {
        if (selected == null) {
            return;
        }
        for (ListedOffer offer : offers) {
            if (offer.villagerId() == selected.villagerId() && offer.offerIndex() == selected.offerIndex() && offer.usesLeft() > 0) {
                selected = offer;
                return;
            }
        }
        selected = null;
    }

    public Click click(double mouseX, double mouseY, int left, int top, int rowHeight, List<ListedOffer> offers, Inventory inventory) {
        int x = (int) mouseX - left;
        int y = (int) mouseY - top;
        if (inside(x, y, SHOW_X, SHOW_Y, SHOW_W, SHOW_H)) {
            showAll = !showAll;
            return Click.TOGGLE;
        }
        if (selected != null && affordable(selected, inventory) > 0) {
            if (inside(x, y, ONCE_X, BUTTON_Y, BUTTON_W, BUTTON_H)) {
                return Click.ONCE;
            }
            if (inside(x, y, MAX_X, BUTTON_Y, BUTTON_W, BUTTON_H)) {
                return Click.MAX;
            }
        }
        if (inside(x, y, GROUP_X, GROUP_Y, GROUP_W, GROUP_H)) {
            int index = groupScroll + (y - GROUP_Y) / rowHeight;
            List<String> groups = groups(offers);
            if (index == 0) {
                selectedGroup = "";
            } else if (index > 0 && index <= groups.size()) {
                selectedGroup = groups.get(index - 1);
            }
            return Click.GROUP;
        }
        if (inside(x, y, OFFER_X, OFFER_Y, OFFER_W, OFFER_H)) {
            int index = offerScroll + (y - OFFER_Y) / OFFER_ROW;
            List<ListedOffer> shown = visible(offers, inventory);
            selected = index >= 0 && index < shown.size() ? shown.get(index) : null;
            return Click.OFFER;
        }
        return Click.NONE;
    }

    public void scroll(double mouseX, double mouseY, int left, int top, double delta, int rowHeight, int offerCount, int groupCount) {
        int x = (int) mouseX - left;
        int y = (int) mouseY - top;
        int step = delta > 0 ? -1 : 1;
        if (inside(x, y, GROUP_X, GROUP_Y, GROUP_W, GROUP_H)) {
            int visible = Math.max(1, GROUP_H / rowHeight);
            groupScroll = clamp(groupScroll + step, groupCount + 1 - visible);
        } else if (inside(x, y, OFFER_X, OFFER_Y, OFFER_W, OFFER_H)) {
            int visible = Math.max(1, OFFER_H / OFFER_ROW);
            offerScroll = clamp(offerScroll + step, offerCount - visible);
        }
    }

    private static int clamp(int value, int max) {
        return Math.max(0, Math.min(value, Math.max(0, max)));
    }

    private static boolean inside(int x, int y, int boxX, int boxY, int width, int height) {
        return x >= boxX && y >= boxY && x < boxX + width && y < boxY + height;
    }

    private static int count(Inventory inventory, ItemStack cost) {
        int have = 0;
        for (ItemStack stack : MainSlots.of(inventory)) {
            if (ItemStack.isSameItemSameComponents(stack, cost)) {
                have += stack.getCount();
            }
        }
        return have;
    }

    public enum Click {
        NONE, TOGGLE, GROUP, OFFER, ONCE, MAX
    }
}
