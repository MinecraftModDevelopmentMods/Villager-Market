package net.modcrafters.minecraft.villagermarket.menu;

import net.modcrafters.minecraft.villagermarket.ModContent;
import net.modcrafters.minecraft.villagermarket.block.MarketBlock;
import net.modcrafters.minecraft.villagermarket.search.MarketSearch;
import net.modcrafters.minecraft.villagermarket.trade.ListedOffer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class VillagerMarketMenu extends AbstractContainerMenu {
    private static final int MAIN_SLOTS = 36;
    private final BlockPos pos;
    private List<ListedOffer> offers;

    public VillagerMarketMenu(int containerId, Inventory inventory, OpeningData data) {
        super(ModContent.menu(), containerId);
        this.pos = data.pos();
        this.offers = List.copyOf(data.offers());
        // The screen never draws these. They exist so a purchase is sent to the client inventory.
        for (int index = 0; index < MAIN_SLOTS; index++) {
            this.addSlot(new HiddenSlot(inventory, index));
        }
    }

    public static OpeningData opening(ServerLevel level, BlockPos pos) {
        return new OpeningData(pos, scan(level, pos));
    }

    public static List<ListedOffer> scan(ServerLevel level, BlockPos pos) {
        return list(MarketSearch.village(level, pos));
    }

    public static List<ListedOffer> list(List<Villager> villagers) {
        List<ListedOffer> listed = new ArrayList<>();
        for (Villager villager : villagers) {
            listed.addAll(ListedOffer.from(villager));
        }
        return List.copyOf(listed);
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

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockState(pos).getBlock() instanceof MarketBlock
                && player.distanceToSqr(Vec3.atCenterOf(pos)) < 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    private static final class HiddenSlot extends Slot {
        private HiddenSlot(Inventory inventory, int index) {
            super(inventory, index, 0, 0);
        }

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }
}
