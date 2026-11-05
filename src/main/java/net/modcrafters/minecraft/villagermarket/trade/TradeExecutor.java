package net.modcrafters.minecraft.villagermarket.trade;

import net.modcrafters.minecraft.villagermarket.search.MarketSearch;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class TradeExecutor {
    private TradeExecutor() {
    }

    public record Result(int performed, boolean outOfRange) {
    }

    public static Result execute(ServerPlayer player, BlockPos marketPos, int villagerId, int offerIndex, int requestedUses) {
        Entity entity = player.level().getEntity(villagerId);
        if (!(entity instanceof Villager villager) || !villager.isAlive() || !MarketSearch.contains(marketPos, villager.position())) {
            return new Result(0, true);
        }
        MerchantOffers offers = villager.getOffers();
        if (offerIndex < 0 || offerIndex >= offers.size()) {
            return new Result(0, false);
        }
        MerchantOffer offer = offers.get(offerIndex);
        ItemStack costA = offer.getCostA();
        ItemStack costB = offer.getCostB();
        boolean hasSecond = !costB.isEmpty();
        int usesLeft = Math.max(0, offer.getMaxUses() - offer.getUses());
        int performed = Math.min(Math.max(requestedUses, 0), TradeMath.affordableUses(
                count(player, costA), costA.getCount(),
                hasSecond ? count(player, costB) : 0, costB.getCount(),
                hasSecond, usesLeft));
        if (performed == 0) {
            return new Result(0, false);
        }
        for (int i = 0; i < performed; i++) {
            ItemStack payA = offer.getCostA();
            ItemStack payB = offer.getCostB();
            remove(player, payA, payA.getCount());
            if (!payB.isEmpty()) {
                remove(player, payB, payB.getCount());
            }
            ItemStack result = offer.getResult().copy();
            player.getInventory().add(result);
            if (!result.isEmpty()) {
                player.level().addFreshEntity(new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), result));
            }
            reward(villager, offer, player.position());
        }
        return new Result(performed, false);
    }

    public static void reward(Villager villager, MerchantOffer offer, Vec3 destination) {
        List<ExperienceOrb> already = orbsNear(villager);
        villager.notifyTrade(offer);
        for (ExperienceOrb orb : orbsNear(villager)) {
            if (!already.contains(orb)) {
                orb.setPos(destination.x, destination.y, destination.z);
            }
        }
    }

    private static List<ExperienceOrb> orbsNear(Villager villager) {
        return villager.level().getEntitiesOfClass(ExperienceOrb.class, villager.getBoundingBox().inflate(1.0));
    }

    static int count(ServerPlayer player, ItemStack cost) {
        int have = 0;
        for (ItemStack stack : MainSlots.of(player.getInventory())) {
            if (ItemStack.isSameItemSameComponents(stack, cost)) {
                have += stack.getCount();
            }
        }
        return have;
    }

    private static void remove(ServerPlayer player, ItemStack cost, int amount) {
        for (ItemStack stack : MainSlots.of(player.getInventory())) {
            if (amount <= 0) {
                return;
            }
            if (!ItemStack.isSameItemSameComponents(stack, cost)) {
                continue;
            }
            int take = Math.min(amount, stack.getCount());
            stack.shrink(take);
            amount -= take;
        }
    }
}
