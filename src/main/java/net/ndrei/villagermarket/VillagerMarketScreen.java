package net.ndrei.villagermarket;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListButton;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipe;
import net.minecraftforge.fml.client.GuiScrollingList;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;

/**
 * Created by CF on 2017-02-21.
 */
public class VillagerMarketScreen extends GuiContainer {
    public static final ResourceLocation BACKGROUND = new ResourceLocation(Constants.MOD_ID, "textures/gui/gui.png");

    private VillagerMarketContainer container;

    private GuiScrollingList list;
    private GuiScrollingList listRecipes;

    private GuiListButton btn;

    private String selectedVillagerType = "";
    private boolean showAllRecipes = true;

    private VillagerMarketContainer.MerchantRecipeInfo currentRecipe;
    private GuiButton btnOnce, btnMax;

    private List<ItemStackSlotInfo> drawnStacks;
    private class ItemStackSlotInfo {
        final ItemStack stack;
        final int x;
        final int y;

        ItemStackSlotInfo(ItemStack s, int x, int y) {
            this.stack = s;
            this.x = x;
            this.y = y;
        }

        int r() {
            return this.x + 16;
        }

        int b() {
            return this.y + 16;
        }
    }

    public VillagerMarketScreen(VillagerMarketContainer container) {
        super(container);

        this.container = container;

        super.xSize = 256;
        super.ySize = 209;
    }

    void readServerCompound(NBTTagCompound compound) {
        this.container.readServerCompound(compound);

        if (this.currentRecipe != null) {
            // update current recipe
            VillagerMarketContainer.MerchantRecipeInfo info = this.currentRecipe;
            this.setCurrentRecipe(null);
            for(VillagerMarketContainer.MerchantRecipeInfo mri : this.container.getRecipes("")) {
                if ((mri.villagerId == info.villagerId) && (mri.recipeIndex == info.recipeIndex)) {
                    if (!mri.recipe.isRecipeDisabled() && (mri.getMaxUses() > 0)) {
                        this.setCurrentRecipe(mri);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        int guiWidth = super.xSize;
        int guiHeight = super.ySize;

        int guiTop = super.guiTop; // (this.height - guiHeight) / 2;
        int guiLeft = super.guiLeft; // (this.width - guiWidth) / 2;

        this.list = new GuiScrollingList(this.mc, 88, 140, guiTop + 5 + 1, guiTop + 140 + 5 + 1, guiLeft + 5 + 1, this.fontRenderer.FONT_HEIGHT + 2, this.width, this.height) {
            private int selected = 0;

            @Override
            protected int getSize() {
                String[] types = VillagerMarketScreen.this.container.getVillagerTypes();
                return 1 + ((types == null) ? 0 : types.length);
            }

            @Override
            protected void elementClicked(int index, boolean doubleClick) {
                this.selected = index;
                String[] types = VillagerMarketScreen.this.container.getVillagerTypes();
                if ((types != null) && (index > 0) && (index <= types.length)) {
                    VillagerMarketScreen.this.selectedVillagerType = types[index - 1];
                } else {
                    VillagerMarketScreen.this.selectedVillagerType = "";
                }
            }

            @Override
            protected boolean isSelected(int index) {
                return (this.selected == index);
            }

            @Override
            protected void drawBackground() {
            }

            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
                if (slotIdx == this.selectedIndex) {
                    GlStateManager.color(0.4f, 0.4f, 0.4f);
                } else {
                    GlStateManager.color(1.0f, 1.0f, 1.0f);
                }

                String[] types = VillagerMarketScreen.this.container.getVillagerTypes();
                if ((types != null) && (slotIdx > 0) && (slotIdx <= types.length)) {
                    VillagerMarketScreen.this.drawString(VillagerMarketScreen.this.fontRenderer,
                            types[slotIdx - 1], this.left + 1, slotTop, 0xFFFFFF);
                } else {
                    VillagerMarketScreen.this.drawString(VillagerMarketScreen.this.fontRenderer,
                            "[ ALL ]", this.left + 1, slotTop, 0xFFFFFF);
                }

                if (slotIdx == this.selectedIndex) {
                    // reset color change
                    GlStateManager.color(1.0f, 1.0f, 1.0f);
                }
            }
        };
        this.listRecipes = new GuiScrollingList(this.mc, 149, 165, guiTop + 5 + 1, guiTop + 165 + 5 + 1, guiLeft + 90 + 5 + 5 + 1, 28, this.width, this.height) {
            @Override
            protected int getSize() {
                List<VillagerMarketContainer.MerchantRecipeInfo> recipes = VillagerMarketScreen.this.getRecipes();
                return (recipes == null) ? 0 : recipes.size();
            }

            @Override
            protected void elementClicked(int index, boolean doubleClick) {
                List<VillagerMarketContainer.MerchantRecipeInfo> recipes = VillagerMarketScreen.this.getRecipes();
                if ((recipes != null) && (index >= 0) && (index < recipes.size())) {
                    VillagerMarketScreen.this.setCurrentRecipe(recipes.get(index));
                }
                else {
                    VillagerMarketScreen.this.setCurrentRecipe(null);
                }
            }

            @Override
            protected boolean isSelected(int index) {
                return false;
            }

            @Override
            protected void drawBackground() {
            }

            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
                if (slotIdx == this.selectedIndex) {
                    GlStateManager.color(0.4f, 0.4f, 0.4f);
                } else {
                    GlStateManager.color(1.0f, 1.0f, 1.0f);
                }

                VillagerMarketScreen.this.mc.getTextureManager().bindTexture(VillagerMarketScreen.BACKGROUND);
                VillagerMarketScreen.this.drawTexturedModalRect(this.left, slotTop, 2, 226, 151, 28);

                List<VillagerMarketContainer.MerchantRecipeInfo> recipes = VillagerMarketScreen.this.getRecipes();
                if ((recipes != null) && (slotIdx >= 0) && (slotIdx < recipes.size())) {
                    VillagerMarketContainer.MerchantRecipeInfo recipeInfo = recipes.get(slotIdx);
                    MerchantRecipe recipe = recipeInfo.recipe;

                    RenderHelper.enableGUIStandardItemLighting();
                    GlStateManager.pushMatrix();
                    GlStateManager.enableDepth();

                    VillagerMarketScreen.this.drawItemStack(recipe.getItemToBuy(), this.left + 5, slotTop + 5);
                    VillagerMarketScreen.this.drawItemStack(recipe.getSecondItemToBuy(), this.left + 30, slotTop + 5);
                    VillagerMarketScreen.this.drawItemStack(recipe.getItemToSell(), this.left + 73, slotTop + 5);

                    GlStateManager.popMatrix();
                    RenderHelper.disableStandardItemLighting();

                    VillagerMarketScreen.this.drawString(VillagerMarketScreen.this.fontRenderer,
                            "[" + recipeInfo.getTimes() + " / " + recipeInfo.getMaxUses() + "]",
                            this.left + 1 + 100, slotTop + (this.slotHeight - VillagerMarketScreen.this.fontRenderer.FONT_HEIGHT) / 2, 0xFFFFFF);
                }

                if (slotIdx == this.selectedIndex) {
                    // reset color change
                    GlStateManager.color(1.0f, 1.0f, 1.0f);
                }
            }
        };

        this.btn = new GuiListButton(new GuiPageButtonList.GuiResponder() {
            @Override
            public void setEntryValue(int id, boolean value) {
                if (id == 42) {
                    VillagerMarketScreen.this.showAllRecipes = value;
                }
            }

            @Override
            public void setEntryValue(int id, float value) {
            }

            @Override
            public void setEntryValue(int id, String value) {
            }
        }, 42, guiLeft + 5, guiTop + 152, "Show All", true);
        this.btn.setWidth(90);
        this.addButton(this.btn);

        this.btnOnce = new GuiButton(4201, guiLeft + 156, guiTop + 180, 35, 20, "x 1");
        this.btnOnce.visible = false;
        this.addButton(this.btnOnce);

        this.btnMax = new GuiButton(4202, guiLeft + 196, guiTop + 180, 35, 20, "x 10");
        this.btnMax.visible = false;
        this.addButton(this.btnMax);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if ((button == null) || (this.currentRecipe == null)) {
            return;
        }

        NBTTagCompound compound = new NBTTagCompound();
        if (button.id == 4201) {
            // handle 'once' click
            compound.setInteger("uses", 1);
        }
        else if (button.id == 4202) {
            // handle 'max' click
            compound.setInteger("uses", this.currentRecipe.getMaxUses());
        }

        if (compound.getSize() > 0) {
            compound.setInteger("villagerId", this.currentRecipe.villagerId);
            compound.setInteger("recipeId", this.currentRecipe.recipeIndex);
            VillagerMarketMod.sendMessageToServer(compound);
        }
    }

    private void setCurrentRecipe(VillagerMarketContainer.MerchantRecipeInfo recipeInfo) {
        if (null != (this.currentRecipe = recipeInfo)) {
            this.btnOnce.visible = true;
            this.btnMax.visible = true;
        }
        else {
            this.btnOnce.visible = false;
            this.btnMax.visible = false;
        }
    }

    private List<VillagerMarketContainer.MerchantRecipeInfo> getRecipes() {
        List<VillagerMarketContainer.MerchantRecipeInfo> recipes = Lists.newArrayList();
        for(VillagerMarketContainer.MerchantRecipeInfo recipe : this.container.getRecipes(this.selectedVillagerType)) {
            if (!this.showAllRecipes && (recipe.getTimes() == 0)) {
                continue;
            }

            recipes.add(recipe);
        }

        return recipes;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        this.mc.getTextureManager().bindTexture(VillagerMarketScreen.BACKGROUND);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, 256, 209);

        this.drawnStacks = Lists.newArrayList();
        this.list.drawScreen(mouseX, mouseY, partialTicks);
        this.listRecipes.drawScreen(mouseX, mouseY, partialTicks);

        // GuiOptionButton btn = new GuiOptionButton(42, 0, 0, 20, 100, "only available");
        this.btn.drawButton(this.mc, mouseX, mouseY, partialTicks);

        if (this.currentRecipe != null) {
            MerchantRecipe recipe = this.currentRecipe.recipe;

            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.pushMatrix();
            GlStateManager.enableDepth();

            int left = this.guiLeft + 5;
            int top = this.guiTop + 176;

            this.drawItemStack(recipe.getItemToBuy(), left + 5, top + 5);
            this.drawItemStack(recipe.getSecondItemToBuy(), left + 30, top + 5);
            this.drawItemStack(recipe.getItemToSell(), left + 73, top + 5);

            GlStateManager.popMatrix();
            RenderHelper.disableStandardItemLighting();

            String maxUses = "-1";
            int times = 0;
            if (this.currentRecipe != null) {
                maxUses = String.valueOf(this.currentRecipe.getMaxUses());
                times = this.currentRecipe.getTimes();
            }

            VillagerMarketScreen.this.drawString(VillagerMarketScreen.this.fontRenderer,
                    "[" + times + " / " + maxUses + "]",
                    left + 1 + 100, top + (28 - VillagerMarketScreen.this.fontRenderer.FONT_HEIGHT) / 2, 0xFFFFFF);

            int uses = Math.min(this.currentRecipe.getTimes(), this.currentRecipe.getMaxUses());
            this.btnMax.displayString = "x " + uses;
            this.btnOnce.enabled = (uses > 0);
            this.btnMax.enabled = (uses > 0);

            this.btnOnce.drawButton(this.mc, mouseX, mouseY, partialTicks);
            this.btnMax.drawButton(this.mc, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        if (this.drawnStacks != null) {
            for(ItemStackSlotInfo info : this.drawnStacks) {
                if (!info.stack.isEmpty() && (mouseX >= info.x) && (mouseX <= info.r()) && (mouseY >= info.y) && (mouseY <= info.b())) {
                    super.renderToolTip(info.stack, mouseX - this.getGuiLeft(), mouseY - this.getGuiTop());
                    break;
                }
            }
        }
    }

    private void drawItemStack(ItemStack stack, int left, int top) {
        VillagerMarketScreen.this.itemRender.renderItemIntoGUI(
                stack, left, top);
        VillagerMarketScreen.this.itemRender.renderItemOverlays(VillagerMarketScreen.this.fontRenderer,
                stack, left, top);

        if (this.drawnStacks != null) {
            this.drawnStacks.add(new ItemStackSlotInfo(stack, left, top));
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        super.handleMouseInput();
        if (this.list != null)
            this.list.handleMouseInput(mouseX, mouseY);
        if (this.listRecipes != null)
            this.listRecipes.handleMouseInput(mouseX, mouseY);
    }
}
