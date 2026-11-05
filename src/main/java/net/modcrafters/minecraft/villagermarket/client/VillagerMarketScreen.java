package net.modcrafters.minecraft.villagermarket.client;

import net.modcrafters.minecraft.villagermarket.menu.VillagerMarketMenu;
import net.modcrafters.minecraft.villagermarket.network.MarketPayloads;
import net.modcrafters.minecraft.villagermarket.platform.Services;
import net.modcrafters.minecraft.villagermarket.trade.ListedOffer;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class VillagerMarketScreen extends AbstractContainerScreen<VillagerMarketMenu> {
    private static final Identifier BUTTON = Identifier.withDefaultNamespace("widget/button");
    private static final Identifier BUTTON_HIGHLIGHTED = Identifier.withDefaultNamespace("widget/button_highlighted");
    private static final Identifier BUTTON_DISABLED = Identifier.withDefaultNamespace("widget/button_disabled");
    private static final Identifier SLOT = Identifier.withDefaultNamespace("container/slot");
    private static final Identifier TRADE_ARROW = Identifier.withDefaultNamespace("container/villager/trade_arrow");
    private static final Identifier TRADE_ARROW_OUT = Identifier.withDefaultNamespace("container/villager/trade_arrow_out_of_stock");
    private static final Identifier SCROLLER = Identifier.withDefaultNamespace("widget/scroller");
    private static final Identifier SCROLLER_BACKGROUND = Identifier.withDefaultNamespace("widget/scroller_background");
    private static final int PANEL = 0xFFC6C6C6;
    private static final int HIGHLIGHT = 0xFFFFFFFF;
    private static final int SHADOW = 0xFF555555;
    private static final int BORDER = 0xFF000000;
    private static final int LABEL = 0xFF404040;
    private static final int BUTTON_TEXT = 0xFFFFFFFF;
    private final MarketScreenLogic logic = new MarketScreenLogic();
    private ItemStack hovered = ItemStack.EMPTY;

    public VillagerMarketScreen(VillagerMarketMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, MarketScreenLogic.WIDTH, MarketScreenLogic.HEIGHT);
    }

    public void onOffers(List<ListedOffer> offers) {
        logic.onOffers(offers);
    }

    public static void apply(MarketPayloads.Offers payload) {
        if (Minecraft.getInstance().gui.screen() instanceof VillagerMarketScreen screen) {
            screen.getMenu().replace(payload.offers());
            screen.onOffers(payload.offers());
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int left = this.leftPos;
        int top = this.topPos;
        drawPanel(graphics, left, top);
        graphics.text(this.font, this.title, left + 8, top + 6, LABEL, false);
        List<String> groups = logic.groups(this.menu.offers());
        graphics.enableScissor(left + MarketScreenLogic.GROUP_X, top + MarketScreenLogic.GROUP_Y,
                left + MarketScreenLogic.GROUP_X + MarketScreenLogic.GROUP_W, top + MarketScreenLogic.GROUP_Y + MarketScreenLogic.GROUP_H);
        drawGroup(graphics, left, top, mouseX, mouseY, 0, logic.selectedGroup.isEmpty(), Component.translatable("gui.villagermarket.all"));
        for (int i = 0; i < groups.size(); i++) {
            drawGroup(graphics, left, top, mouseX, mouseY, i + 1, groups.get(i).equals(logic.selectedGroup), groupLabel(groups.get(i)));
        }
        graphics.disableScissor();
        hovered = ItemStack.EMPTY;
        List<ListedOffer> shown = logic.visible(this.menu.offers(), this.minecraft.player.getInventory());
        logic.fitOffer(shown.size());
        int offerW = logic.offerWidth(shown.size());
        graphics.enableScissor(left + MarketScreenLogic.OFFER_X, top + MarketScreenLogic.OFFER_Y,
                left + MarketScreenLogic.OFFER_X + offerW, top + MarketScreenLogic.OFFER_Y + MarketScreenLogic.OFFER_H);
        for (int i = logic.offerScroll; i < shown.size(); i++) {
            int y = top + MarketScreenLogic.OFFER_Y + (i - logic.offerScroll) * MarketScreenLogic.OFFER_ROW;
            if (y + MarketScreenLogic.OFFER_ROW > top + MarketScreenLogic.OFFER_Y + MarketScreenLogic.OFFER_H) {
                break;
            }
            drawOffer(graphics, shown.get(i), left + MarketScreenLogic.OFFER_X, y, offerW, mouseX, mouseY, this.minecraft.player.getInventory());
        }
        graphics.disableScissor();
        drawOfferBar(graphics, left, top, shown.size());
        graphics.text(this.font, Component.translatable("gui.villagermarket.offer_count"),
                left + MarketScreenLogic.OFFER_X, top + MarketScreenLogic.COUNT_Y, LABEL, false);
        Component show = Component.translatable(logic.showAll ? "gui.villagermarket.show_all" : "gui.villagermarket.available");
        drawButton(graphics, left + MarketScreenLogic.SHOW_X, top + MarketScreenLogic.SHOW_Y, MarketScreenLogic.SHOW_W, MarketScreenLogic.SHOW_H,
                show, true, logic.showAll || inside(mouseX, mouseY, left + MarketScreenLogic.SHOW_X, top + MarketScreenLogic.SHOW_Y, MarketScreenLogic.SHOW_W, MarketScreenLogic.SHOW_H));
        if (logic.selected != null) {
            Inventory inventory = this.minecraft.player.getInventory();
            boolean second = !logic.selected.costB().isEmpty();
            drawHeld(graphics, logic.selected.costA(), left + MarketScreenLogic.costSlotX(0, second), top + MarketScreenLogic.COST_Y, mouseX, mouseY, inventory);
            if (second) {
                drawHeld(graphics, logic.selected.costB(), left + MarketScreenLogic.costSlotX(1, true), top + MarketScreenLogic.COST_Y, mouseX, mouseY, inventory);
            }
            int affordable = logic.affordable(logic.selected, inventory);
            drawButton(graphics, left + MarketScreenLogic.ONCE_X, top + MarketScreenLogic.BUTTON_Y, MarketScreenLogic.BUTTON_W, MarketScreenLogic.BUTTON_H,
                    Component.translatable("gui.villagermarket.once"), affordable > 0,
                    inside(mouseX, mouseY, left + MarketScreenLogic.ONCE_X, top + MarketScreenLogic.BUTTON_Y, MarketScreenLogic.BUTTON_W, MarketScreenLogic.BUTTON_H));
            drawButton(graphics, left + MarketScreenLogic.MAX_X, top + MarketScreenLogic.BUTTON_Y, MarketScreenLogic.BUTTON_W, MarketScreenLogic.BUTTON_H,
                    Component.literal("x " + affordable), affordable > 0,
                    inside(mouseX, mouseY, left + MarketScreenLogic.MAX_X, top + MarketScreenLogic.BUTTON_Y, MarketScreenLogic.BUTTON_W, MarketScreenLogic.BUTTON_H));
        }
    }

    private void drawPanel(GuiGraphicsExtractor graphics, int x, int y) {
        int right = x + MarketScreenLogic.WIDTH;
        int bottom = y + MarketScreenLogic.HEIGHT;
        graphics.fill(x, y, right, bottom, PANEL);
        graphics.fill(x + 1, y + 1, right - 1, y + 3, HIGHLIGHT);
        graphics.fill(x + 1, y + 1, x + 3, bottom - 1, HIGHLIGHT);
        graphics.fill(x + 3, bottom - 3, right - 1, bottom - 1, SHADOW);
        graphics.fill(right - 3, y + 3, right - 1, bottom - 1, SHADOW);
        graphics.fill(x, y, right, y + 1, BORDER);
        graphics.fill(x, bottom - 1, right, bottom, BORDER);
        graphics.fill(x, y, x + 1, bottom, BORDER);
        graphics.fill(right - 1, y, right, bottom, BORDER);
    }

    private void drawGroup(GuiGraphicsExtractor graphics, int left, int top, int mouseX, int mouseY, int index, boolean selected, Component label) {
        int x = left + MarketScreenLogic.GROUP_X;
        int y = top + MarketScreenLogic.GROUP_Y + (index - logic.groupScroll) * MarketScreenLogic.GROUP_ROW;
        if (y + MarketScreenLogic.GROUP_ROW <= top + MarketScreenLogic.GROUP_Y || y >= top + MarketScreenLogic.GROUP_Y + MarketScreenLogic.GROUP_H) {
            return;
        }
        boolean hot = selected || inside(mouseX, mouseY, x, y, MarketScreenLogic.GROUP_W, MarketScreenLogic.GROUP_ROW);
        blitButton(graphics, x, y, MarketScreenLogic.GROUP_W, MarketScreenLogic.GROUP_ROW, true, hot);
        graphics.text(this.font, label, x + 4, y + 6, BUTTON_TEXT, true);
    }

    private void drawOffer(GuiGraphicsExtractor graphics, ListedOffer offer, int x, int y, int width, int mouseX, int mouseY, Inventory inventory) {
        boolean selected = logic.selected != null && logic.selected.villagerId() == offer.villagerId() && logic.selected.offerIndex() == offer.offerIndex();
        boolean hot = selected || inside(mouseX, mouseY, x, y, width, MarketScreenLogic.OFFER_ROW);
        blitButton(graphics, x, y, width, MarketScreenLogic.OFFER_ROW, true, hot);
        drawStack(graphics, offer.costA(), x + 3, y + 2, mouseX, mouseY);
        drawStack(graphics, offer.costB(), x + 22, y + 2, mouseX, mouseY);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, offer.usesLeft() > 0 ? TRADE_ARROW : TRADE_ARROW_OUT, x + 41, y + 5, 10, 9);
        drawStack(graphics, offer.result(), x + 54, y + 2, mouseX, mouseY);
        int affordable = logic.affordable(offer, inventory);
        graphics.text(this.font, "[" + affordable + " / " + offer.usesLeft() + "]", x + 74, y + 6, BUTTON_TEXT, true);
    }

    private void drawOfferBar(GuiGraphicsExtractor graphics, int left, int top, int count) {
        if (!logic.offerBar(count)) {
            return;
        }
        int x = left + MarketScreenLogic.BAR_X;
        int y = top + MarketScreenLogic.OFFER_Y;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_BACKGROUND, x, y, MarketScreenLogic.BAR_W, MarketScreenLogic.OFFER_H);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER, x, top + logic.offerThumbY(count), MarketScreenLogic.BAR_W, logic.offerThumbH(count));
    }

    private int shownCount() {
        return logic.visible(this.menu.offers(), this.minecraft.player.getInventory()).size();
    }

    private void drawButton(GuiGraphicsExtractor graphics, int x, int y, int width, int height, Component label, boolean enabled, boolean hovered) {
        blitButton(graphics, x, y, width, height, enabled, hovered);
        int textX = x + (width - this.font.width(label)) / 2;
        graphics.text(this.font, label, textX, y + (height - 8) / 2, BUTTON_TEXT, true);
    }

    private void blitButton(GuiGraphicsExtractor graphics, int x, int y, int width, int height, boolean enabled, boolean hovered) {
        Identifier sprite = !enabled ? BUTTON_DISABLED : hovered ? BUTTON_HIGHLIGHTED : BUTTON;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height);
    }

    private void drawHeld(GuiGraphicsExtractor graphics, ItemStack cost, int x, int y, int mouseX, int mouseY, Inventory inventory) {
        if (cost.isEmpty()) {
            return;
        }
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT, x, y, MarketScreenLogic.COST_SLOT, MarketScreenLogic.COST_SLOT);
        int itemX = x + 1;
        int itemY = y + 1;
        graphics.item(cost, itemX, itemY);
        graphics.itemDecorations(this.font, cost, itemX, itemY, Integer.toString(logic.held(inventory, cost)));
        if (inside(mouseX, mouseY, x, y, MarketScreenLogic.COST_SLOT, MarketScreenLogic.COST_SLOT)) {
            hovered = cost;
        }
    }

    private void drawStack(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, int mouseX, int mouseY) {
        if (stack.isEmpty()) {
            return;
        }
        graphics.item(stack, x, y);
        graphics.itemDecorations(this.font, stack, x, y);
        if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
            hovered = stack;
        }
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        if (!hovered.isEmpty()) {
            graphics.setTooltipForNextFrame(this.font, hovered, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        Inventory inventory = this.minecraft.player.getInventory();
        MarketScreenLogic.Click click = logic.click(event.x(), event.y(), this.leftPos, this.topPos, this.menu.offers(), inventory);
        if (click == MarketScreenLogic.Click.ONCE && logic.selected != null) {
            send(1);
        } else if (click == MarketScreenLogic.Click.MAX && logic.selected != null) {
            send(logic.affordable(logic.selected, inventory));
        }
        return click != MarketScreenLogic.Click.NONE || super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        logic.dragBar((int) event.y() - this.topPos, shownCount());
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        logic.releaseBar();
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        List<ListedOffer> shown = logic.visible(this.menu.offers(), this.minecraft.player.getInventory());
        logic.scroll(mouseX, mouseY, this.leftPos, this.topPos, scrollY, shown.size(), logic.groups(this.menu.offers()).size());
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
