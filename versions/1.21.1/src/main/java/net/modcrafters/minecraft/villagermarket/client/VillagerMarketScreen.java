package net.modcrafters.minecraft.villagermarket.client;

import net.modcrafters.minecraft.villagermarket.menu.VillagerMarketMenu;
import net.modcrafters.minecraft.villagermarket.network.MarketPayloads;
import net.modcrafters.minecraft.villagermarket.platform.Services;
import net.modcrafters.minecraft.villagermarket.trade.ListedOffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

// 1.21.1 still draws with GuiGraphics. The 26.3 screen is the shared file.
public class VillagerMarketScreen extends AbstractContainerScreen<VillagerMarketMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("villagermarket", "textures/gui/gui.png");
    private static final int TEXT = 0xFFFFFFFF;
    private static final int SELECTED = 0x886B4A2A;
    private final MarketScreenLogic logic = new MarketScreenLogic();
    private ItemStack hovered = ItemStack.EMPTY;

    public VillagerMarketScreen(VillagerMarketMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = MarketScreenLogic.WIDTH;
        this.imageHeight = MarketScreenLogic.HEIGHT;
    }

    public void onOffers(List<ListedOffer> offers) {
        logic.onOffers(offers);
    }

    public static void apply(MarketPayloads.Offers payload) {
        if (Minecraft.getInstance().screen instanceof VillagerMarketScreen screen) {
            screen.getMenu().replace(payload.offers());
            screen.onOffers(payload.offers());
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;
        graphics.blit(TEXTURE, left, top, 0, 0, this.imageWidth, this.imageHeight);
        int row = this.font.lineHeight + 2;
        List<String> groups = logic.groups(this.menu.offers());
        drawGroup(graphics, left, top, row, 0, logic.selectedGroup.isEmpty(), Component.translatable("gui.villagermarket.all"));
        for (int i = 0; i < groups.size(); i++) {
            drawGroup(graphics, left, top, row, i + 1, groups.get(i).equals(logic.selectedGroup), groupLabel(groups.get(i)));
        }
        hovered = ItemStack.EMPTY;
        List<ListedOffer> shown = logic.visible(this.menu.offers(), this.minecraft.player.getInventory());
        for (int i = logic.offerScroll; i < shown.size(); i++) {
            int y = top + MarketScreenLogic.OFFER_Y + (i - logic.offerScroll) * MarketScreenLogic.OFFER_ROW;
            if (y + MarketScreenLogic.OFFER_ROW > top + MarketScreenLogic.OFFER_Y + MarketScreenLogic.OFFER_H) {
                break;
            }
            ListedOffer offer = shown.get(i);
            if (logic.selected != null && logic.selected.villagerId() == offer.villagerId() && logic.selected.offerIndex() == offer.offerIndex()) {
                graphics.fill(left + MarketScreenLogic.OFFER_X, y, left + MarketScreenLogic.OFFER_X + MarketScreenLogic.OFFER_W, y + MarketScreenLogic.OFFER_ROW, SELECTED);
            }
            drawOffer(graphics, offer, left + MarketScreenLogic.OFFER_X, y, mouseX, mouseY);
        }
        graphics.fill(left + MarketScreenLogic.SHOW_X, top + MarketScreenLogic.SHOW_Y,
                left + MarketScreenLogic.SHOW_X + MarketScreenLogic.SHOW_W, top + MarketScreenLogic.SHOW_Y + MarketScreenLogic.SHOW_H,
                logic.showAll ? SELECTED : 0x66000000);
        graphics.drawString(this.font, Component.translatable("gui.villagermarket.show_all"), left + MarketScreenLogic.SHOW_X + 4, top + MarketScreenLogic.SHOW_Y + 6, TEXT, false);
        if (logic.selected != null) {
            drawOffer(graphics, logic.selected, left + 5, top + MarketScreenLogic.SELECTED_Y, mouseX, mouseY);
            int affordable = logic.affordable(logic.selected, this.minecraft.player.getInventory());
            drawButton(graphics, left + MarketScreenLogic.ONCE_X, top + MarketScreenLogic.BUTTON_Y, Component.translatable("gui.villagermarket.once"), affordable > 0);
            drawButton(graphics, left + MarketScreenLogic.MAX_X, top + MarketScreenLogic.BUTTON_Y, Component.literal("x " + affordable), affordable > 0);
        }
        if (!hovered.isEmpty()) {
            graphics.renderTooltip(this.font, hovered, mouseX, mouseY);
        }
    }

    private void drawGroup(GuiGraphics graphics, int left, int top, int row, int index, boolean selected, Component label) {
        int y = top + MarketScreenLogic.GROUP_Y + (index - logic.groupScroll) * row;
        if (y < top + MarketScreenLogic.GROUP_Y || y + row > top + MarketScreenLogic.GROUP_Y + MarketScreenLogic.GROUP_H) {
            return;
        }
        if (selected) {
            graphics.fill(left + MarketScreenLogic.GROUP_X, y, left + MarketScreenLogic.GROUP_X + MarketScreenLogic.GROUP_W, y + row, SELECTED);
        }
        graphics.drawString(this.font, label, left + MarketScreenLogic.GROUP_X + 2, y + 1, TEXT, false);
    }

    private void drawOffer(GuiGraphics graphics, ListedOffer offer, int x, int y, int mouseX, int mouseY) {
        drawStack(graphics, offer.costA(), x + 5, y + 5, mouseX, mouseY);
        drawStack(graphics, offer.costB(), x + 30, y + 5, mouseX, mouseY);
        drawStack(graphics, offer.result(), x + 73, y + 5, mouseX, mouseY);
        int affordable = logic.affordable(offer, this.minecraft.player.getInventory());
        graphics.drawString(this.font, "[" + affordable + " / " + offer.usesLeft() + "]", x + 101, y + 10, TEXT, false);
    }

    private void drawStack(GuiGraphics graphics, ItemStack stack, int x, int y, int mouseX, int mouseY) {
        if (stack.isEmpty()) {
            return;
        }
        graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(this.font, stack, x, y);
        if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
            hovered = stack;
        }
    }

    private void drawButton(GuiGraphics graphics, int x, int y, Component label, boolean enabled) {
        graphics.fill(x, y, x + MarketScreenLogic.BUTTON_W, y + MarketScreenLogic.BUTTON_H, enabled ? SELECTED : 0x66000000);
        graphics.drawString(this.font, label, x + 4, y + 6, TEXT, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Inventory inventory = this.minecraft.player.getInventory();
        MarketScreenLogic.Click click = logic.click(mouseX, mouseY, this.leftPos, this.topPos, this.font.lineHeight + 2, this.menu.offers(), inventory);
        if (click == MarketScreenLogic.Click.ONCE && logic.selected != null) {
            send(1);
        } else if (click == MarketScreenLogic.Click.MAX && logic.selected != null) {
            send(logic.affordable(logic.selected, inventory));
        }
        return click != MarketScreenLogic.Click.NONE || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        List<ListedOffer> shown = logic.visible(this.menu.offers(), this.minecraft.player.getInventory());
        logic.scroll(mouseX, mouseY, this.leftPos, this.topPos, scrollY, this.font.lineHeight + 2, shown.size(), logic.groups(this.menu.offers()).size());
        return true;
    }

    private void send(int uses) {
        Services.PLATFORM.sendToServer(new MarketPayloads.Trade(logic.selected.villagerId(), logic.selected.offerIndex(), uses));
    }

    static Component groupLabel(String groupKey) {
        int colon = groupKey.indexOf(':');
        if (colon > 0 && groupKey.indexOf(' ') < 0) {
            return Component.translatable("entity.minecraft.villager." + groupKey.substring(colon + 1));
        }
        return Component.literal(groupKey);
    }
}
