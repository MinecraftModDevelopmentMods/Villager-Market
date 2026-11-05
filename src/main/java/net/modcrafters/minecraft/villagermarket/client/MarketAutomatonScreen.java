package net.modcrafters.minecraft.villagermarket.client;

import net.modcrafters.minecraft.villagermarket.block.entity.RedstoneMode;
import net.modcrafters.minecraft.villagermarket.menu.MachineSlots;
import net.modcrafters.minecraft.villagermarket.menu.MarketAutomatonMenu;
import net.modcrafters.minecraft.villagermarket.network.AutomatonPayloads;
import net.modcrafters.minecraft.villagermarket.network.MarketPayloads;
import net.modcrafters.minecraft.villagermarket.platform.Services;
import net.modcrafters.minecraft.villagermarket.trade.ListedOffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class MarketAutomatonScreen extends AbstractContainerScreen<MarketAutomatonMenu> {
    private static final Identifier BUTTON = Identifier.withDefaultNamespace("widget/button");
    private static final Identifier BUTTON_HIGHLIGHTED = Identifier.withDefaultNamespace("widget/button_highlighted");
    private static final Identifier SLOT = Identifier.withDefaultNamespace("container/slot");
    private static final Identifier TRADE_ARROW = Identifier.withDefaultNamespace("container/villager/trade_arrow");
    private static final Identifier FLOW_ARROW = Identifier.withDefaultNamespace("container/furnace/burn_progress");
    private static final Identifier SIMILAR = Identifier.fromNamespaceAndPath("villagermarket", "similar");
    private static final Identifier TRADE_ARROW_OUT = Identifier.withDefaultNamespace("container/villager/trade_arrow_out_of_stock");
    private static final Identifier SCROLLER = Identifier.withDefaultNamespace("widget/scroller");
    private static final Identifier SCROLLER_BACKGROUND = Identifier.withDefaultNamespace("widget/scroller_background");
    private static final int PANEL = 0xFFC6C6C6;
    private static final int HIGHLIGHT = 0xFFFFFFFF;
    private static final int SHADOW = 0xFF555555;
    private static final int BORDER = 0xFF000000;
    private static final int LABEL = 0xFF404040;
    private static final int BUTTON_TEXT = 0xFFFFFFFF;
    private static final ItemStack REDSTONE_DUST = new ItemStack(Items.REDSTONE);
    private static final ItemStack REDSTONE_TORCH = new ItemStack(Items.REDSTONE_TORCH);
    private static final int SLASH = 0xFFCC3333;
    private final MarketScreenLogic logic = new MarketScreenLogic();
    private ItemStack hovered = ItemStack.EMPTY;
    private Component tip = Component.empty();

    public MarketAutomatonScreen(MarketAutomatonMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, MarketScreenLogic.WIDTH, MachineSlots.HEIGHT);
        logic.listHeight = MachineSlots.LIST_H;
        logic.showY = MachineSlots.SHOW_Y;
    }

    public static void apply(AutomatonPayloads.Recipe payload) {
        if (Minecraft.getInstance().gui.screen() instanceof MarketAutomatonScreen screen) {
            screen.getMenu().arm(payload.costA(), payload.costB(), payload.result());
        }
    }

    public static void applyMode(AutomatonPayloads.Mode payload) {
        if (Minecraft.getInstance().gui.screen() instanceof MarketAutomatonScreen screen) {
            screen.getMenu().redstone(RedstoneMode.byId(payload.mode()));
        }
    }

    public static void applySimilar(AutomatonPayloads.SimilarState payload) {
        if (Minecraft.getInstance().gui.screen() instanceof MarketAutomatonScreen screen) {
            screen.getMenu().similar(payload.similar());
        }
    }

    public static void applyOffers(MarketPayloads.Offers payload) {
        if (Minecraft.getInstance().gui.screen() instanceof MarketAutomatonScreen screen) {
            screen.getMenu().replace(payload.offers());
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
                left + MarketScreenLogic.GROUP_X + MarketScreenLogic.GROUP_W, top + MarketScreenLogic.GROUP_Y + logic.listHeight);
        drawGroup(graphics, left, top, mouseX, mouseY, 0, logic.selectedGroup.isEmpty(), Component.translatable("gui.villagermarket.all"));
        for (int i = 0; i < groups.size(); i++) {
            drawGroup(graphics, left, top, mouseX, mouseY, i + 1, groups.get(i).equals(logic.selectedGroup), VillagerMarketScreen.groupLabel(groups.get(i)));
        }
        graphics.disableScissor();
        hovered = ItemStack.EMPTY;
        tip = Component.empty();
        Inventory inventory = this.minecraft.player.getInventory();
        List<ListedOffer> shown = logic.visible(this.menu.offers(), inventory);
        logic.fitOffer(shown.size());
        int offerW = logic.offerWidth(shown.size());
        graphics.enableScissor(left + MarketScreenLogic.OFFER_X, top + MarketScreenLogic.OFFER_Y,
                left + MarketScreenLogic.OFFER_X + offerW, top + MarketScreenLogic.OFFER_Y + logic.listHeight);
        for (int i = logic.offerScroll; i < shown.size(); i++) {
            int y = top + MarketScreenLogic.OFFER_Y + (i - logic.offerScroll) * MarketScreenLogic.OFFER_ROW;
            if (y + MarketScreenLogic.OFFER_ROW > top + MarketScreenLogic.OFFER_Y + logic.listHeight) {
                break;
            }
            drawOffer(graphics, shown.get(i), left + MarketScreenLogic.OFFER_X, y, offerW, mouseX, mouseY, inventory);
        }
        graphics.disableScissor();
        drawOfferBar(graphics, left, top, shown.size());
        Component show = Component.translatable(logic.showAll ? "gui.villagermarket.show_all" : "gui.villagermarket.available");
        drawButton(graphics, left + MarketScreenLogic.SHOW_X, top + MachineSlots.SHOW_Y, MarketScreenLogic.SHOW_W, MarketScreenLogic.SHOW_H,
                show, logic.showAll || inside(mouseX, mouseY, left + MarketScreenLogic.SHOW_X, top + MachineSlots.SHOW_Y, MarketScreenLogic.SHOW_W, MarketScreenLogic.SHOW_H));
        drawSimilar(graphics, left, top, mouseX, mouseY);
        drawSlot(graphics, left + MachineSlots.INPUT_X - 1, top + MachineSlots.INPUT_Y - 1);
        drawSlot(graphics, left + MachineSlots.INPUT_X + 17, top + MachineSlots.INPUT_Y - 1);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FLOW_ARROW, left + MachineSlots.ARROW_X, top + MachineSlots.INPUT_Y, MachineSlots.ARROW_W, MachineSlots.ARROW_H);
        drawSlot(graphics, left + MachineSlots.OUTPUT_X - 1, top + MachineSlots.OUTPUT_Y - 1);
        drawRedstone(graphics, left, top, mouseX, mouseY);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlot(graphics, left + MachineSlots.INV_X + column * 18 - 1, top + MachineSlots.INV_Y + row * 18 - 1);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawSlot(graphics, left + MachineSlots.INV_X + column * 18 - 1, top + MachineSlots.HOTBAR_Y - 1);
        }
    }

    private void drawPanel(GuiGraphicsExtractor graphics, int x, int y) {
        int right = x + MarketScreenLogic.WIDTH;
        int bottom = y + MachineSlots.HEIGHT;
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
        if (y + MarketScreenLogic.GROUP_ROW <= top + MarketScreenLogic.GROUP_Y || y >= top + MarketScreenLogic.GROUP_Y + logic.listHeight) {
            return;
        }
        boolean hot = selected || inside(mouseX, mouseY, x, y, MarketScreenLogic.GROUP_W, MarketScreenLogic.GROUP_ROW);
        blitButton(graphics, x, y, MarketScreenLogic.GROUP_W, MarketScreenLogic.GROUP_ROW, hot);
        graphics.text(this.font, label, x + 4, y + 6, BUTTON_TEXT, true);
    }

    private void drawOffer(GuiGraphicsExtractor graphics, ListedOffer offer, int x, int y, int width, int mouseX, int mouseY, Inventory inventory) {
        boolean selected = this.menu.armed(offer);
        boolean hot = selected || inside(mouseX, mouseY, x, y, width, MarketScreenLogic.OFFER_ROW);
        blitButton(graphics, x, y, width, MarketScreenLogic.OFFER_ROW, hot);
        drawStack(graphics, offer.costA(), x + 3, y + 2, mouseX, mouseY);
        drawStack(graphics, offer.costB(), x + 22, y + 2, mouseX, mouseY);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, offer.usesLeft() > 0 ? TRADE_ARROW : TRADE_ARROW_OUT, x + 41, y + 5, 10, 9);
        drawStack(graphics, offer.result(), x + 54, y + 2, mouseX, mouseY);
        Component uses = offer.usesLeft() > 0
                ? Component.translatable("gui.villagermarket.max_uses", offer.usesLeft())
                : Component.translatable("gui.villagermarket.locked");
        graphics.text(this.font, uses, x + 74, y + 6, BUTTON_TEXT, true);
    }

    private void drawOfferBar(GuiGraphicsExtractor graphics, int left, int top, int count) {
        if (!logic.offerBar(count)) {
            return;
        }
        int x = left + MarketScreenLogic.BAR_X;
        int y = top + MarketScreenLogic.OFFER_Y;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_BACKGROUND, x, y, MarketScreenLogic.BAR_W, logic.listHeight);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER, x, top + logic.offerThumbY(count), MarketScreenLogic.BAR_W, logic.offerThumbH(count));
    }

    private void drawButton(GuiGraphicsExtractor graphics, int x, int y, int width, int height, Component label, boolean hot) {
        blitButton(graphics, x, y, width, height, hot);
        int textX = x + (width - this.font.width(label)) / 2;
        graphics.text(this.font, label, textX, y + (height - 8) / 2, BUTTON_TEXT, true);
    }

    private void blitButton(GuiGraphicsExtractor graphics, int x, int y, int width, int height, boolean hot) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, hot ? BUTTON_HIGHLIGHTED : BUTTON, x, y, width, height);
    }

    private void drawSimilar(GuiGraphicsExtractor graphics, int left, int top, int mouseX, int mouseY) {
        int x = left + MachineSlots.SIMILAR_X;
        int y = top + MachineSlots.SIMILAR_Y;
        boolean hot = this.menu.similar() || inside(mouseX, mouseY, x, y, MachineSlots.SIMILAR_SIZE, MachineSlots.SIMILAR_SIZE);
        blitButton(graphics, x, y, MachineSlots.SIMILAR_SIZE, MachineSlots.SIMILAR_SIZE, hot);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SIMILAR, x + 2, y + 2, 16, 16);
        if (inside(mouseX, mouseY, x, y, MachineSlots.SIMILAR_SIZE, MachineSlots.SIMILAR_SIZE)) {
            tip = Component.translatable(this.menu.similar() ? "gui.villagermarket.similar.on" : "gui.villagermarket.similar.off");
        }
    }

    private void drawRedstone(GuiGraphicsExtractor graphics, int left, int top, int mouseX, int mouseY) {
        int x = left + MachineSlots.REDSTONE_X;
        int y = top + MachineSlots.REDSTONE_Y;
        int size = MachineSlots.REDSTONE_SIZE;
        boolean hot = inside(mouseX, mouseY, x, y, size, size);
        blitButton(graphics, x, y, size, size, hot);
        RedstoneMode mode = this.menu.redstone();
        ItemStack icon = mode == RedstoneMode.UNPOWERED ? REDSTONE_TORCH : REDSTONE_DUST;
        graphics.item(icon, x + 2, y + 2);
        if (mode == RedstoneMode.IGNORED) {
            for (int step = 0; step < 12; step++) {
                graphics.fill(x + 3 + step, y + 4 + step, x + 6 + step, y + 7 + step, SLASH);
            }
        }
        if (hot) {
            tip = mode.label();
        }
    }

    private void drawSlot(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT, x, y, 18, 18);
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

    private boolean similarHit(double mouseX, double mouseY) {
        return inside((int) mouseX, (int) mouseY, this.leftPos + MachineSlots.SIMILAR_X, this.topPos + MachineSlots.SIMILAR_Y, MachineSlots.SIMILAR_SIZE, MachineSlots.SIMILAR_SIZE);
    }

    private boolean redstoneHit(double mouseX, double mouseY) {
        return inside((int) mouseX, (int) mouseY, this.leftPos + MachineSlots.REDSTONE_X, this.topPos + MachineSlots.REDSTONE_Y, MachineSlots.REDSTONE_SIZE, MachineSlots.REDSTONE_SIZE);
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
        } else if (!tip.getString().isEmpty()) {
            graphics.setTooltipForNextFrame(this.font, tip, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (similarHit(event.x(), event.y())) {
            Services.PLATFORM.sendToServer(new AutomatonPayloads.Similar());
            return true;
        }
        if (redstoneHit(event.x(), event.y())) {
            Services.PLATFORM.sendToServer(new AutomatonPayloads.Redstone());
            return true;
        }
        MarketScreenLogic.Click click = logic.browse(event.x(), event.y(), this.leftPos, this.topPos, this.menu.offers(), this.minecraft.player.getInventory());
        if (click == MarketScreenLogic.Click.OFFER && logic.selected != null) {
            Services.PLATFORM.sendToServer(new AutomatonPayloads.Select(logic.selected.villagerId(), logic.selected.offerIndex()));
        }
        return click != MarketScreenLogic.Click.NONE || super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        logic.dragBar((int) event.y() - this.topPos, logic.visible(this.menu.offers(), this.minecraft.player.getInventory()).size());
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
}
