package net.modcrafters.minecraft.villagermarket.menu;

import net.modcrafters.minecraft.villagermarket.ModContent;
import net.modcrafters.minecraft.villagermarket.block.entity.MarketAutomatonBlockEntity;
import net.modcrafters.minecraft.villagermarket.block.entity.RedstoneMode;
import net.modcrafters.minecraft.villagermarket.search.MarketSearch;
import net.modcrafters.minecraft.villagermarket.trade.ArmedTrade;
import net.modcrafters.minecraft.villagermarket.trade.ListedOffer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class MarketAutomatonMenu extends AbstractContainerMenu {
    private final BlockPos pos;
    private final net.minecraft.world.Container container;
    private List<ListedOffer> offers;
    private ItemStack costA;
    private ItemStack costB;
    private ItemStack result;
    private RedstoneMode redstone;
    private boolean similar;

    public MarketAutomatonMenu(int containerId, Inventory inventory, AutomatonOpening data) {
        super(ModContent.automatonMenu(), containerId);
        this.pos = data.pos();
        this.offers = List.copyOf(data.offers());
        BlockEntity found = inventory.player.level().getBlockEntity(data.pos());
        this.container = found instanceof MarketAutomatonBlockEntity machine ? machine : new SimpleContainer(MachineSlots.COUNT);
        arm(data.costA(), data.costB(), data.result());
        this.redstone = RedstoneMode.byId(data.redstone());
        this.similar = data.similar();
        addSlot(new Slot(container, MachineSlots.INPUT_A, MachineSlots.INPUT_X, MachineSlots.INPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !costA.isEmpty() && ArmedTrade.sameKind(stack, costA);
            }
        });
        addSlot(new Slot(container, MachineSlots.INPUT_B, MachineSlots.INPUT_X + 18, MachineSlots.INPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !costB.isEmpty() && ArmedTrade.sameKind(stack, costB);
            }
        });
        addSlot(new Slot(container, MachineSlots.OUTPUT, MachineSlots.OUTPUT_X, MachineSlots.OUTPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, MachineSlots.INV_X + column * 18, MachineSlots.INV_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, MachineSlots.INV_X + column * 18, MachineSlots.HOTBAR_Y));
        }
    }

    public static AutomatonOpening opening(ServerLevel level, BlockPos pos) {
        ItemStack costA = ItemStack.EMPTY;
        ItemStack costB = ItemStack.EMPTY;
        ItemStack result = ItemStack.EMPTY;
        int redstone = 0;
        boolean similar = false;
        if (level.getBlockEntity(pos) instanceof MarketAutomatonBlockEntity machine) {
            costA = machine.costA().copy();
            costB = machine.costB().copy();
            result = machine.result().copy();
            redstone = machine.redstone().ordinal();
            similar = machine.similar();
        }
        return new AutomatonOpening(pos, scan(level, pos), costA, costB, result, redstone, similar);
    }

    public static List<ListedOffer> scan(ServerLevel level, BlockPos pos) {
        return VillagerMarketMenu.list(MarketSearch.forAutomaton(level, pos));
    }

    public BlockPos pos() {
        return pos;
    }

    public List<ListedOffer> offers() {
        return offers;
    }

    public void replace(List<ListedOffer> offers) {
        this.offers = List.copyOf(offers);
    }

    public ItemStack costA() {
        return costA;
    }

    public ItemStack costB() {
        return costB;
    }

    public ItemStack result() {
        return result;
    }

    public RedstoneMode redstone() {
        return redstone;
    }

    public void redstone(RedstoneMode redstone) {
        this.redstone = redstone;
    }

    public boolean similar() {
        return similar;
    }

    public void similar(boolean similar) {
        this.similar = similar;
    }

    public void arm(ItemStack costA, ItemStack costB, ItemStack result) {
        this.costA = costA.copy();
        this.costB = costB.copy();
        this.result = result.copy();
    }

    public boolean armed(ListedOffer offer) {
        return ArmedTrade.matches(offer.costA(), offer.costB(), offer.result(), costA, costB, result);
    }

    @Override
    public boolean stillValid(Player player) {
        if (container instanceof MarketAutomatonBlockEntity machine) {
            return machine.stillValid(player);
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < MachineSlots.COUNT) {
            if (!moveItemStackTo(stack, MachineSlots.COUNT, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, MachineSlots.INPUT_A, MachineSlots.OUTPUT, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }
}
