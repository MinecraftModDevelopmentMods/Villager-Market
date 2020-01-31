package net.ndrei.villagermarket;

import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.swing.*;
import java.util.List;
import java.util.Objects;

/**
 * Created by CF on 2017-02-21.
 */
public class VillagerMarketContainer extends Container {
    private EntityPlayer player;

    private List<EntityVillager> villagers = null;
    private List<VillagerInfo> villagerInfos = null;
    private List<String> villagerTypes;

    VillagerMarketContainer(World world, BlockPos pos, EntityPlayer player) {
        this.player = player;

        int radius = 16;
        Village village = world.getVillageCollection().getNearestVillage(pos, radius);
        if (village != null) {
            pos = village.getCenter();
            radius = village.getVillageRadius();
        }
        AxisAlignedBB bb = new AxisAlignedBB(
                pos.east(radius).south(radius).down(radius),
                pos.west(radius).north(radius).up(radius));

        this.villagers = Lists.newArrayList();
        this.villagerTypes = Lists.newArrayList();
        for(EntityVillager villager : world.getEntitiesWithinAABB(EntityVillager.class, bb)) {
            String villagerName = villager.getDisplayName().getFormattedText();
            if (!this.villagerTypes.contains(villagerName)) {
                this.villagerTypes.add(villagerName);
            }
            // VillagerMarketMod.logger.info(villagerName + ": " + String.valueOf(villager.getEntityId() + " - " + villager.getProfessionForge().getRegistryName().toString()));
            this.villagers.add(villager);
        }
        this.villagerTypes.sort(String::compareTo);
    }

    VillagerMarketContainer(EntityPlayer player) {
        this.player = player;
        this.villagerInfos = Lists.newArrayList();
        this.villagerTypes = Lists.newArrayList();

        NBTTagCompound message = new NBTTagCompound();
        message.setInteger("LOAD", 42);
        VillagerMarketMod.sendMessageToServer(message);
    }

    void readServerCompound(NBTTagCompound compound) {
        NBTTagList list = compound.getTagList("villagers", Constants.NBT.TAG_COMPOUND);
        final List<VillagerInfo> infos = Lists.newArrayList();
        final List<String> types = Lists.newArrayList();
        for(int index = 0; index < list.tagCount(); index++) {
            NBTTagCompound v = list.getCompoundTagAt(index);

            String profession = v.getString("profession");
            int villagerId = v.getInteger("entityId");
            MerchantRecipeList recipes = new MerchantRecipeList(v.getCompoundTag("recipes"));

            if (!type.contains(profession)) {
                type.add(profession);
            }

            infos.add(new VillagerInfo(profession, villagerId, recipes));
        }
        this.villagerInfos = infos;
        this.villagerTypes = types;
    }

    @Override
    public boolean canInteractWith(@SuppressWarnings("NullableProblems") EntityPlayer playerIn) {
        return (playerIn == this.player);
    }

    String[] getVillagerTypes() {
        return (this.villagerTypes == null) ? null : this.villagerTypes.toArray(new String[0]);
    }

    MerchantRecipeInfo[] getRecipes(String villagerTypeFilter) {
        List<MerchantRecipeInfo> recipes = Lists.newArrayList();

        if (this.villagers != null) {
            for (EntityVillager villager : this.villagers) {
                if ((villagerTypeFilter == null) || (villagerTypeFilter.length() == 0)
                        || (Objects.equals(villager.getDisplayName().getFormattedText(), villagerTypeFilter))) {
                    int index = 0;
                    for (MerchantRecipe recipe : villager.getRecipes(this.player)) {
                        recipes.add(new MerchantRecipeInfo(villager, villager.getEntityId(), recipe, index++, this));
                    }
                }
            }
        }
        else if (this.villagerInfos != null) {
            for (VillagerInfo villager : this.villagerInfos) {
                if ((villagerTypeFilter == null) || (villagerTypeFilter.length() == 0)
                        || (Objects.equals(villager.profession, villagerTypeFilter))) {
                    int index = 0;
                    for (MerchantRecipe recipe : villager.recipes) {
                        recipes.add(new MerchantRecipeInfo(null, villager.villagerId, recipe, index++, this));
                    }
                }
            }
        }

        return recipes.toArray(new MerchantRecipeInfo[0]);
    }

    List<ItemStack> getPlayerInventory() {
        return VillagerMarketMod.getCombinedInventory(this.player.inventory);
    }

    void processMessageFromClient(NBTTagCompound compound) {
        int uses = compound.getInteger("uses");
        int villagerId = compound.getInteger("villagerId");
        int recipeId = compound.getInteger("recipeId");

        Entity raw = this.player.world.getEntityByID(villagerId);
        if ((raw != null) && (raw instanceof  EntityVillager)) {
            EntityVillager villager = (EntityVillager)raw;

            MerchantRecipe recipe = villager.getRecipes(this.player).get(recipeId);
            new MerchantRecipeInfo(villager, villager.getEntityId(), recipe, recipeId, this).useRecipe(uses);
        }
    }

    NBTTagCompound getNBTForMessage() {
        NBTTagCompound nbt = new NBTTagCompound();

        NBTTagList list = new NBTTagList();
        for(EntityVillager villager : this.villagers) {
            NBTTagCompound v = new NBTTagCompound();

            v.setString("profession", villager.getDisplayName().getFormattedText());
            v.setInteger("entityId", villager.getEntityId());

            v.setTag("recipes", villager.getRecipes(this.player).getRecipiesAsTags());

            list.appendTag(v);
        }
        nbt.setTag("villagers", list);

        return nbt;
    }

    private class VillagerInfo {
        final int villagerId;
        final MerchantRecipeList recipes;
        final String profession;

        VillagerInfo(String profession, int villagerId, MerchantRecipeList recipes) {
            this.profession = profession;
            this.villagerId = villagerId;
            this.recipes = recipes;
        }
    }

    class MerchantRecipeInfo {
        private final EntityVillager villager;
        final int villagerId;
        final MerchantRecipe recipe;
        final VillagerMarketContainer container;
        final int recipeIndex;

        MerchantRecipeInfo(EntityVillager villager, int villagerId, MerchantRecipe recipe, int recipeIndex, VillagerMarketContainer container) {
            this.villager = villager;
            this.villagerId = villagerId;
            this.recipe = recipe;
            this.recipeIndex = recipeIndex;
            this.container = container;
        }

        int getUses(List<ItemStack> inventory, boolean isCombined) {
            if (this.recipe.isRecipeDisabled() || (this.recipe.getMaxTradeUses() == 0)) {
                return 0;
            }

            int uses = VillagerMarketMod.getAmountOf(inventory, this.recipe.getItemToBuy(), true, isCombined);
            if (recipe.hasSecondItemToBuy()) {
               uses = Math.min(uses,
                       VillagerMarketMod.getAmountOf(inventory, this.recipe.getSecondItemToBuy(), true, isCombined));
            }

            return uses;
        }

        int getMaxUses() {
            if (this.recipe.isRecipeDisabled()) {
                return 0;
            }

            return Math.max(0, this.recipe.getMaxTradeUses() - this.recipe.getToolUses());
        }

        void useRecipe(int uses) {
            List<ItemStack> inventory = this.container.getPlayerInventory();

            uses = Math.min(uses, this.getUses(inventory, true));
            if (uses == 0) {
                return;
            }

            ItemStack item1 = this.recipe.getItemToBuy();
            int item1Size = item1.getCount() * uses;

            ItemStack item2 = this.recipe.getSecondItemToBuy();
            int item2Size = 0;
            if (!item2.isEmpty()) {
                item2Size = item2.getCount() * uses;
            }

            int item1Extracted = VillagerMarketMod.extractFromCombinedInventory(this.container.player.inventory, item1, item1Size);
            if (item1Extracted != item1Size) {
                VillagerMarketMod.logger.warn("Could not extract " + String.valueOf(item1Size) + " of " + item1.getDisplayName() + " from player inventory.");
            }
            if (item2Size > 0) {
                int item2Extracted = VillagerMarketMod.extractFromCombinedInventory(this.container.player.inventory, item2, item2Size);
                if (item2Extracted != item2Size) {
                    VillagerMarketMod.logger.warn("Could not extract " + String.valueOf(item2Size) + " of " + item2.getDisplayName() + " from player inventory.");
                }
            }

            for(int use = 0; use < uses; use++) {
                ItemStack result = this.recipe.getItemToSell().copy();

                // try to merge result into existing slots
                int emptySlot = -1;
                for(int index = 0; index < this.container.player.inventory.getSizeInventory(); index++) {
                    if (!this.container.player.inventory.isItemValidForSlot(index, result)) {
                        // TODO: find a way to ignore armor slots for non-armor items
                        return;
                    }

                    ItemStack inv = this.container.player.inventory.getStackInSlot(index);
                    if (inv.isEmpty() && (emptySlot == -1)) {
                        emptySlot = index;
                    }
                    else if (!inv.isEmpty() && inv.isItemEqual(result)) {
                        int max = inv.getMaxStackSize();
                        int canInsert = Math.min(Math.min(max, result.getCount()), max - inv.getCount());
                        if (canInsert > 0) {
                            inv.setCount(inv.getCount() + canInsert);
                            this.container.player.inventory.setInventorySlotContents(index, inv);

                            result.shrink(canInsert);
                        }
                    }

                    if (result.isEmpty()) {
                        break;
                    }
                }

                if (!result.isEmpty()) {
                    if (emptySlot >= 0) {
                        this.container.player.inventory.setInventorySlotContents(emptySlot, result);
                    }
                    else {
                        BlockPos pos = this.container.player.getPosition();
                        InventoryHelper.spawnItemStack(this.container.player.world,
                                pos.getX(), pos.getY(), pos.getZ(),
                                result.copy());
                        VillagerMarketMod.logger.info("Spawned at " + pos.toString() + " : " + result.toString());
                    }
                }

                this.villager.useRecipe(this.recipe);
            }

            BlockPos villagerPos = this.villager.getPos();
            AxisAlignedBB aabb = new AxisAlignedBB(villagerPos.south().east().down(), villagerPos.north().west().up());
            for (EntityXPOrb xp: this.villager.getWorld().getEntitiesWithinAABB(EntityXPOrb.class, aabb)) {
                xp.setPosition(this.container.player.posX, this.container.player.posY, this.container.player.posZ);
            }

            this.container.player.inventory.markDirty();
            this.container.player.inventoryContainer.detectAndSendChanges();
            ((EntityPlayerMP)this.container.player).sendContainerToPlayer(this.container.player.inventoryContainer);
            VillagerMarketMod.sendMessageToClient(container.getNBTForMessage(), (EntityPlayerMP) this.container.player);
        }
    }
}
