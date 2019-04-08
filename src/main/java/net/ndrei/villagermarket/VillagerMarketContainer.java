package net.ndrei.villagermarket;

import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by CF on 2017-02-21.
 */
public class VillagerMarketContainer extends Container {
    private EntityPlayer player;
    private BlockPos pos;

    private List<EntityVillager> villagers = null;
    private List<VillagerInfo> villagerInfos = null;
    private List<String> villagerTypes;
    private List<MerchantRecipeInfo> merchantRecipes = new ArrayList<>();

    public VillagerMarketContainer(World world, BlockPos pos, EntityPlayer player) {
        this.player = player;
        this.pos = pos;

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

    public VillagerMarketContainer(EntityPlayer player, BlockPos pos) {
        this.pos = pos;
        this.player = player;
        this.villagerInfos = Lists.newArrayList();
        this.villagerTypes = Lists.newArrayList();

        NBTTagCompound message = new NBTTagCompound();
        message.setInteger("LOAD", 42);
        VillagerMarketMod.sendMessageToServer(message);
    }

    public void readServerCompound(NBTTagCompound compound) {
        NBTTagList list = compound.getTagList("villagers", Constants.NBT.TAG_COMPOUND);
        this.villagerInfos.clear();
        this.villagerTypes.clear();
        this.merchantRecipes.clear();
        for(int index = 0; index < list.tagCount(); index++) {
            NBTTagCompound v = list.getCompoundTagAt(index);

            String profession = v.getString("profession");
            int villagerId = v.getInteger("entityId");
            MerchantRecipeList recipes = new MerchantRecipeList(v.getCompoundTag("recipes"));

            if (!this.villagerTypes.contains(profession)) {
                this.villagerTypes.add(profession);
            }

            NBTTagList recipeInfos = v.getTagList("recipeInfo", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < recipeInfos.tagCount(); i++) {
                this.merchantRecipes.add(new MerchantRecipeInfo(recipeInfos.getCompoundTagAt(i), this));
            }

            this.villagerInfos.add(new VillagerInfo(profession, villagerId, recipes));
        }
    }

    @Override
    public boolean canInteractWith(@SuppressWarnings("NullableProblems") EntityPlayer playerIn) {
        return (playerIn == this.player);
    }

    public String[] getVillagerTypes() {
        return (this.villagerTypes == null) ? null : this.villagerTypes.toArray(new String[0]);
    }

    public MerchantRecipeInfo[] getRecipes(String villagerTypeFilter) {
        if (player.world.isRemote) {
            List<MerchantRecipeInfo> temp = merchantRecipes;
            if (villagerTypeFilter != null && !villagerTypeFilter.isEmpty()) {
                temp = merchantRecipes.stream().filter((recipe) -> !recipe.getVillager().getDisplayName().getFormattedText().equals(villagerTypeFilter)).collect(Collectors.toList());
            }
            return temp.toArray(new MerchantRecipeInfo[0]);
        }

        List<MerchantRecipeInfo> recipes = Lists.newArrayList();
        List<ItemStack> inventory = getWorkableInventory();
        if (this.villagers != null) {
            this.villagers.forEach((villager) -> recipes.addAll(getVillagerRecipes(villager, inventory)));
        }
        else if (this.villagerInfos != null) {
            this.villagerInfos.forEach((villager) -> recipes.addAll(getVillagerRecipes(villager, inventory)));
        }

        return recipes.toArray(new MerchantRecipeInfo[0]);
    }

    private List<MerchantRecipeInfo> getVillagerRecipes(EntityVillager villager, List<ItemStack> inventory) {
        int index = 0;
        List<MerchantRecipeInfo> recipes = new ArrayList<>();
        for (MerchantRecipe recipe : villager.getRecipes(this.player)) {
            recipes.add(new MerchantRecipeInfo(villager, villager.getEntityId(), recipe, index++, getMaxTimes(inventory, recipe), this));
        }
        return recipes;
    }

    private List<MerchantRecipeInfo> getVillagerRecipes(VillagerInfo villager, List<ItemStack> inventory) {
        int index = 0;
        List<MerchantRecipeInfo> recipes = new ArrayList<>();
        for (MerchantRecipe recipe : villager.recipes) {
            recipes.add(new MerchantRecipeInfo(null, villager.villagerId, recipe, index++, getMaxTimes(inventory, recipe), this));
        }
        return recipes;
    }


    private int getMaxTimes(List<ItemStack> inventory, MerchantRecipe recipe) {
        int times = VillagerMarketMod.getAmountOf(inventory, recipe.getItemToBuy(), true, true);
        if (recipe.hasSecondItemToBuy()) {
            times = Math.min(times, VillagerMarketMod.getAmountOf(inventory, recipe.getSecondItemToBuy(), true, true));
        }
        return times;
    }

    public IItemHandlerModifiable getInventories() {
        List<IItemHandlerModifiable> handlers = new ArrayList<>();
        handlers.add((IItemHandlerModifiable) player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP));

        for (EnumFacing side : EnumFacing.values()) {
            BlockPos offset = pos.offset(side);
            TileEntity te = player.world.getTileEntity(offset);
            if (te != null) {
                if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                    IItemHandler cap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                    if (!(cap instanceof IItemHandlerModifiable)) {
                        VillagerMarketMod.logger.warn(String.format("inventory on side %s is not modifiable", side.getName()));
                        continue;
                    }
                    handlers.add((IItemHandlerModifiable) cap);
                }
            }
        }
        return new CombinedInvWrapper(handlers.toArray(new IItemHandlerModifiable[0]));
    }

    public List<ItemStack> getWorkableInventory() {
        return VillagerMarketMod.getCombinedInventory(getInventories());
    }

    public void processMessageFromClient(NBTTagCompound compound) {
        int uses = compound.getInteger("uses");
        int villagerId = compound.getInteger("villagerId");
        int recipeId = compound.getInteger("recipeId");

        Entity raw = this.player.world.getEntityByID(villagerId);
        if ((raw != null) && (raw instanceof  EntityVillager)) {
            EntityVillager villager = (EntityVillager)raw;

            MerchantRecipe recipe = villager.getRecipes(this.player).get(recipeId);
            new MerchantRecipeInfo(villager, villager.getEntityId(), recipe, recipeId, 0, this).useRecipe(uses);
        }
    }

    public NBTTagCompound getNBTForMessage() {
        NBTTagCompound nbt = new NBTTagCompound();

        List<ItemStack> inventory = getWorkableInventory();
        NBTTagList list = new NBTTagList();
        for(EntityVillager villager : this.villagers) {
            NBTTagCompound v = new NBTTagCompound();

            v.setString("profession", villager.getDisplayName().getFormattedText());
            v.setInteger("entityId", villager.getEntityId());

            v.setTag("recipes", villager.getRecipes(this.player).getRecipiesAsTags());

            NBTTagList recipeInfo = new NBTTagList();
            for (MerchantRecipeInfo mri : getVillagerRecipes(villager, inventory)) {
                recipeInfo.appendTag(mri.getAsNBT());
            }

            v.setTag("recipeInfo", recipeInfo);

            list.appendTag(v);
        }
        nbt.setTag("villagers", list);

        return nbt;
    }

    private class VillagerInfo {
        final int villagerId;
        final MerchantRecipeList recipes;
        final String profession;

        public VillagerInfo(String profession, int villagerId, MerchantRecipeList recipes) {
            this.profession = profession;
            this.villagerId = villagerId;
            this.recipes = recipes;
        }
    }

    public class MerchantRecipeInfo {
        public final EntityVillager villager;
        public final VillagerMarketContainer container;

        public final int villagerId;
        public final MerchantRecipe recipe;
        public final int recipeIndex;
        public final int times;

        public MerchantRecipeInfo (NBTTagCompound tag, VillagerMarketContainer container) {
            this.villager = null;
            this.container = container;
            this.villagerId = tag.getInteger("id");
            this.recipeIndex = tag.getInteger("recipeIndex");
            this.times = tag.getInteger("times");
            this.recipe = new MerchantRecipe(tag.getCompoundTag("recipe"));
        }

        public MerchantRecipeInfo(EntityVillager villager, int villagerId, MerchantRecipe recipe, int recipeIndex, int times, VillagerMarketContainer container) {
            this.villager = villager;
            this.villagerId = villagerId;
            this.recipe = recipe;
            this.recipeIndex = recipeIndex;
            this.times = times;
            this.container = container;
        }

        public NBTTagCompound getAsNBT () {
            NBTTagCompound result = new NBTTagCompound();
            result.setInteger("id", villagerId);
            result.setInteger("recipeIndex", recipeIndex);
            result.setInteger("times", times);
            result.setTag("recipe", recipe.writeToTags());
            return result;
        }

        public EntityVillager getVillager() {
            return villager;
        }

        public int getUses(List<ItemStack> inventory) {
            if (this.recipe.isRecipeDisabled() || (this.recipe.getMaxTradeUses() == 0)) {
                return 0;
            }

            return getMaxTimes(inventory, recipe);
        }

        public int getMaxUses() {
            if (this.recipe.isRecipeDisabled()) {
                return 0;
            }

            return Math.max(0, this.recipe.getMaxTradeUses() - this.recipe.getToolUses());
        }

        public int getTimes() {
            return times;
        }

        public void useRecipe(int uses) {
            List<ItemStack> inventory = this.container.getWorkableInventory();

            uses = Math.min(uses, this.getUses(inventory));
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

            extract(item1, item1Size);
            if (item2Size > 0) {
                extract(item2, item2Size);
            }

            for(int use = 0; use < uses; use++) {
                ItemStack result = this.recipe.getItemToSell().copy();

                IItemHandlerModifiable playerInventory = (IItemHandlerModifiable) player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
                for (int index = 0; index < playerInventory.getSlots(); index++) {
                    result = playerInventory.insertItem(index, result, false);
                    if (result.isEmpty()) {
                        break;
                    }
                }

                if (!result.isEmpty()) {
                    BlockPos pos = this.container.player.getPosition();
                    InventoryHelper.spawnItemStack(this.container.player.world,
                            pos.getX(), pos.getY(), pos.getZ(),
                            result.copy());
                    VillagerMarketMod.logger.info("Spawned at " + pos.toString() + " : " + result.toString());
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

        private void extract(ItemStack item1, int item1Size) {
            int item1Extracted = VillagerMarketMod.extractFromCombinedInventory(this.container.getInventories(), item1, item1Size);
            if (item1Extracted != item1Size) {
                VillagerMarketMod.logger.warn("Could not extract " + String.valueOf(item1Size) + " of " + item1.getDisplayName() + " from player inventory.");
            }
        }
    }
}
