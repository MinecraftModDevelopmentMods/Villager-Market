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
    public static final int HEIGHT = 180;
    public static final int GROUP_X = 8;
    public static final int GROUP_Y = 18;
    public static final int GROUP_W = 88;
    public static final int GROUP_H = 120;
    public static final int GROUP_ROW = 20;
    public static final int OFFER_X = 100;
    public static final int OFFER_Y = 18;
    public static final int OFFER_W = 148;
    public static final int OFFER_H = 120;
    public static final int OFFER_ROW = 20;
    public static final int COUNT_Y = OFFER_Y + OFFER_H + 2;
    public static final int SHOW_X = 8;
    public static final int SHOW_Y = 152;
    public static final int SHOW_W = 88;
    public static final int SHOW_H = 20;
    public static final int ONCE_X = 164;
    public static final int MAX_X = 208;
    public static final int BUTTON_W = 40;
    public static final int BUTTON_H = 16;
    public static final int BUTTON_Y = SHOW_Y + (SHOW_H - BUTTON_H) / 2;
    public static final int COST_SLOT = 18;
    public static final int COST_Y = BUTTON_Y + (BUTTON_H - COST_SLOT) / 2;
    public static final int BAR_W = 6;
    public static final int BAR_X = WIDTH - 4 - BAR_W;

    public boolean showAll = true;
    public String selectedGroup = "";
    public int groupScroll;
    public int offerScroll;
    public ListedOffer selected;
    private boolean draggingBar;
    private int barGrab;

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

    public static int costSlotX(int index, boolean second) {
        int slots = second ? 2 : 1;
        return ONCE_X - 4 - slots * COST_SLOT + index * COST_SLOT;
    }

    public static int offerRows() {
        return Math.max(1, OFFER_H / OFFER_ROW);
    }

    public static boolean offerBar(int count) {
        return count > offerRows();
    }

    public static int offerWidth(int count) {
        return offerBar(count) ? BAR_X - OFFER_X - 2 : OFFER_W;
    }

    public void fitOffer(int count) {
        offerScroll = clamp(offerScroll, count - offerRows());
    }

    public int offerThumbY(int count) {
        int thumb = offerThumbH(count);
        int travel = Math.max(1, OFFER_H - thumb);
        int max = Math.max(1, count - offerRows());
        return OFFER_Y + offerScroll * travel / max;
    }

    public int offerThumbH(int count) {
        return Math.max(BAR_W * 2, OFFER_H * offerRows() / Math.max(count, 1));
    }

    public boolean pressBar(int x, int y, int count) {
        if (!offerBar(count) || !inside(x, y, BAR_X, OFFER_Y, BAR_W, OFFER_H)) {
            return false;
        }
        draggingBar = true;
        int thumbY = offerThumbY(count);
        int thumbH = offerThumbH(count);
        barGrab = y >= thumbY && y < thumbY + thumbH ? y - thumbY : thumbH / 2;
        dragBar(y, count);
        return true;
    }

    public void dragBar(int y, int count) {
        if (!draggingBar || !offerBar(count)) {
            return;
        }
        int thumb = offerThumbH(count);
        int travel = Math.max(1, OFFER_H - thumb);
        int max = Math.max(1, count - offerRows());
        offerScroll = clamp(y - barGrab - OFFER_Y, travel);
        offerScroll = offerScroll * max / travel;
    }

    public void releaseBar() {
        draggingBar = false;
    }

    public int held(Inventory inventory, ItemStack cost) {
        return count(inventory, cost);
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

    public Click click(double mouseX, double mouseY, int left, int top, List<ListedOffer> offers, Inventory inventory) {
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
            int index = groupScroll + (y - GROUP_Y) / GROUP_ROW;
            List<String> groups = groups(offers);
            if (index == 0) {
                selectedGroup = "";
            } else if (index > 0 && index <= groups.size()) {
                selectedGroup = groups.get(index - 1);
            }
            return Click.GROUP;
        }
        List<ListedOffer> shown = visible(offers, inventory);
        fitOffer(shown.size());
        if (pressBar(x, y, shown.size())) {
            return Click.BAR;
        }
        if (inside(x, y, OFFER_X, OFFER_Y, offerWidth(shown.size()), OFFER_H)) {
            int index = offerScroll + (y - OFFER_Y) / OFFER_ROW;
            selected = index >= 0 && index < shown.size() ? shown.get(index) : null;
            return Click.OFFER;
        }
        return Click.NONE;
    }

    public void scroll(double mouseX, double mouseY, int left, int top, double delta, int offerCount, int groupCount) {
        int x = (int) mouseX - left;
        int y = (int) mouseY - top;
        int step = delta > 0 ? -1 : 1;
        if (inside(x, y, GROUP_X, GROUP_Y, GROUP_W, GROUP_H)) {
            int visible = Math.max(1, GROUP_H / GROUP_ROW);
            groupScroll = clamp(groupScroll + step, groupCount + 1 - visible);
        } else if (inside(x, y, OFFER_X, OFFER_Y, BAR_X + BAR_W - OFFER_X, OFFER_H)) {
            offerScroll = clamp(offerScroll + step, offerCount - offerRows());
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
        NONE, TOGGLE, GROUP, OFFER, ONCE, MAX, BAR
    }
}
