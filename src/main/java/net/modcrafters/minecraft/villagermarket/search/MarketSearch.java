package net.modcrafters.minecraft.villagermarket.search;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class MarketSearch {
    public static final int RADIUS = 48;
    public static final int HEIGHT = 16;

    private MarketSearch() {
    }

    public static AABB box(BlockPos marketPos) {
        return new AABB(marketPos).inflate(RADIUS, HEIGHT, RADIUS);
    }

    public static boolean contains(BlockPos marketPos, Vec3 villagerPos) {
        return box(marketPos).contains(villagerPos);
    }

    public static List<Villager> find(ServerLevel level, BlockPos marketPos) {
        return level.getEntitiesOfClass(Villager.class, box(marketPos), villager -> villager.isAlive() && !villager.getOffers().isEmpty());
    }
}
