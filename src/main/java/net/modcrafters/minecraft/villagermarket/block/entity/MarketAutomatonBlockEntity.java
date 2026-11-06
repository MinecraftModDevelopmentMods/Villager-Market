package net.modcrafters.minecraft.villagermarket.block.entity;

import net.modcrafters.minecraft.villagermarket.ModContent;
import net.modcrafters.minecraft.villagermarket.menu.MachineSlots;
import net.modcrafters.minecraft.villagermarket.menu.MarketAutomatonMenu;
import net.modcrafters.minecraft.villagermarket.network.MarketPayloads;
import net.modcrafters.minecraft.villagermarket.platform.Services;
import net.modcrafters.minecraft.villagermarket.search.JobSites;
import net.modcrafters.minecraft.villagermarket.search.MarketSearch;
import net.modcrafters.minecraft.villagermarket.trade.ArmedTrade;
import net.modcrafters.minecraft.villagermarket.trade.ListedOffer;
import net.modcrafters.minecraft.villagermarket.trade.TradeExecutor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;
//? if >=26.1 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?} else {
/*import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
*///?}

public class MarketAutomatonBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    private static final int[] INPUTS = {MachineSlots.INPUT_A, MachineSlots.INPUT_B};
    private static final int[] OUTPUTS = {MachineSlots.OUTPUT};
    private final NonNullList<ItemStack> items = NonNullList.withSize(MachineSlots.COUNT, ItemStack.EMPTY);
    private final NonNullList<ItemStack> recipe = NonNullList.withSize(MachineSlots.COUNT, ItemStack.EMPTY);
    private RedstoneMode redstone = RedstoneMode.IGNORED;
    private boolean similar;
    private int ticks;
    private boolean sawBinding;
    private boolean bound;

    public MarketAutomatonBlockEntity(BlockPos pos, BlockState state) {
        super(ModContent.automatonType(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MarketAutomatonBlockEntity machine) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        machine.watchBinding(server, pos);
        if (!machine.hasRecipe() || !machine.allowsWork(level)) {
            return;
        }
        if (machine.ticks > 0) {
            machine.ticks--;
        }
        if (machine.ticks > 0) {
            return;
        }
        if (machine.trade(server, pos)) {
            machine.ticks = MachineSlots.COOLDOWN;
        }
    }

    public void arm(ItemStack costA, ItemStack costB, ItemStack result) {
        recipe.set(MachineSlots.INPUT_A, costA.copy());
        recipe.set(MachineSlots.INPUT_B, costB.copy());
        recipe.set(MachineSlots.OUTPUT, result.copy());
        setChanged();
    }

    public boolean hasRecipe() {
        return !costA().isEmpty() && !result().isEmpty();
    }

    public RedstoneMode redstone() {
        return redstone;
    }

    public void cycleRedstone() {
        redstone = redstone.next();
        setChanged();
    }

    public boolean similar() {
        return similar;
    }

    public void setSimilar(boolean similar) {
        this.similar = similar;
        setChanged();
    }

    public void toggleSimilar() {
        similar = !similar;
        setChanged();
    }

    public boolean allowsWork(Level level) {
        boolean powered = level.hasNeighborSignal(this.worldPosition);
        return switch (redstone) {
            case IGNORED -> true;
            case POWERED -> powered;
            case UNPOWERED -> !powered;
        };
    }

    public ItemStack costA() {
        return recipe.get(MachineSlots.INPUT_A);
    }

    public ItemStack costB() {
        return recipe.get(MachineSlots.INPUT_B);
    }

    public ItemStack result() {
        return recipe.get(MachineSlots.OUTPUT);
    }

    private boolean trade(ServerLevel server, BlockPos pos) {
        Villager chosenVillager = null;
        MerchantOffer chosen = null;
        int bestA = Integer.MAX_VALUE;
        int bestB = Integer.MAX_VALUE;
        for (Villager villager : MarketSearch.forAutomaton(server, pos)) {
            var offers = villager.getOffers();
            for (int index = 0; index < offers.size(); index++) {
                MerchantOffer offer = offers.get(index);
                if (!payable(offer)) {
                    continue;
                }
                if (!similar) {
                    perform(villager, offer);
                    publishOffers(server);
                    return true;
                }
                int priceA = offer.getCostA().getCount();
                int priceB = offer.getCostB().isEmpty() ? 0 : offer.getCostB().getCount();
                if (chosen == null || priceA < bestA || (priceA == bestA && priceB < bestB)) {
                    bestA = priceA;
                    bestB = priceB;
                    chosenVillager = villager;
                    chosen = offer;
                }
            }
        }
        if (chosen == null) {
            return false;
        }
        perform(chosenVillager, chosen);
        publishOffers(server);
        return true;
    }

    private void watchBinding(ServerLevel server, BlockPos pos) {
        boolean now = JobSites.workstation(server.getBlockState(pos.above()));
        if (!sawBinding) {
            sawBinding = true;
            bound = now;
            return;
        }
        if (now == bound) {
            return;
        }
        bound = now;
        publishOffers(server);
    }

    private void publishOffers(ServerLevel server) {
        List<ListedOffer> fresh = MarketAutomatonMenu.scan(server, this.worldPosition);
        for (ServerPlayer player : server.players()) {
            if (player.containerMenu instanceof MarketAutomatonMenu menu && menu.pos().equals(this.worldPosition)) {
                menu.replace(fresh);
                Services.PLATFORM.sendToPlayer(player, new MarketPayloads.Offers(fresh));
            }
        }
    }

    private boolean payable(MerchantOffer offer) {
        if (!ArmedTrade.matches(offer, costA(), costB(), result())) {
            return false;
        }
        if (Math.max(0, offer.getMaxUses() - offer.getUses()) <= 0) {
            return false;
        }
        ItemStack payA = offer.getCostA();
        ItemStack payB = offer.getCostB();
        ItemStack give = offer.getResult();
        if (payA.isEmpty() || give.isEmpty() || !canPay(MachineSlots.INPUT_A, payA)) {
            return false;
        }
        if (!payB.isEmpty() && !canPay(MachineSlots.INPUT_B, payB)) {
            return false;
        }
        return roomFor(give);
    }

    private void perform(Villager villager, MerchantOffer offer) {
        ItemStack payA = offer.getCostA();
        ItemStack payB = offer.getCostB();
        ItemStack give = offer.getResult();
        shrink(MachineSlots.INPUT_A, payA.getCount());
        if (!payB.isEmpty()) {
            shrink(MachineSlots.INPUT_B, payB.getCount());
        }
        ItemStack out = getItem(MachineSlots.OUTPUT);
        if (out.isEmpty()) {
            setItem(MachineSlots.OUTPUT, give.copy());
        } else {
            out.grow(give.getCount());
            setChanged();
        }
        TradeExecutor.reward(villager, offer, Vec3.atCenterOf(this.worldPosition));
    }

    private boolean canPay(int slot, ItemStack cost) {
        ItemStack stack = getItem(slot);
        return ArmedTrade.sameKind(stack, cost) && stack.getCount() >= cost.getCount();
    }

    private boolean roomFor(ItemStack give) {
        ItemStack out = getItem(MachineSlots.OUTPUT);
        if (out.isEmpty()) {
            return true;
        }
        return ArmedTrade.sameKind(out, give) && out.getCount() + give.getCount() <= out.getMaxStackSize();
    }

    private void shrink(int slot, int count) {
        ItemStack stack = getItem(slot);
        stack.shrink(count);
        if (stack.isEmpty()) {
            setItem(slot, ItemStack.EMPTY);
        } else {
            setChanged();
        }
    }

    //? if >=26.1 {
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, items);
        ContainerHelper.loadAllItems(input.childOrEmpty("Recipe"), recipe);
        redstone = RedstoneMode.byId(input.getByteOr("Redstone", (byte) 0));
        similar = input.getByteOr("Similar", (byte) 0) != 0;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        ContainerHelper.saveAllItems(output.child("Recipe"), recipe);
        output.putByte("Redstone", (byte) redstone.ordinal());
        output.putByte("Similar", (byte) (similar ? 1 : 0));
    }
    //?} else {
    /*@Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        ContainerHelper.loadAllItems(tag, items, provider);
        if (tag.contains("Recipe")) {
            ContainerHelper.loadAllItems(tag.getCompound("Recipe"), recipe, provider);
        }
        redstone = RedstoneMode.byId(tag.getByte("Redstone"));
        similar = tag.getByte("Similar") != 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        ContainerHelper.saveAllItems(tag, items, provider);
        CompoundTag recipeTag = new CompoundTag();
        ContainerHelper.saveAllItems(recipeTag, recipe, provider);
        tag.put("Recipe", recipeTag);
        tag.putByte("Redstone", (byte) redstone.ordinal());
        tag.putByte("Similar", (byte) (similar ? 1 : 0));
    }
    *///?}

    @Override
    public int getContainerSize() {
        return MachineSlots.COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, count);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockEntity(this.worldPosition) == this
                && player.distanceToSqr(Vec3.atCenterOf(this.worldPosition)) < 64.0;
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == MachineSlots.INPUT_A) {
            return !costA().isEmpty() && ArmedTrade.sameKind(stack, costA());
        }
        if (slot == MachineSlots.INPUT_B) {
            return !costB().isEmpty() && ArmedTrade.sameKind(stack, costB());
        }
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return direction == Direction.DOWN ? OUTPUTS : INPUTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return direction != Direction.DOWN && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == MachineSlots.OUTPUT && direction == Direction.DOWN;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.villagermarket.market_automaton");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        if (!(player.level() instanceof ServerLevel server)) {
            return null;
        }
        return new MarketAutomatonMenu(containerId, inventory, MarketAutomatonMenu.opening(server, this.worldPosition));
    }
}
