package net.modcrafters.minecraft.villagermarket.search;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class MarketSearch {
    private MarketSearch() {
    }

    public static boolean serves(ServerLevel level, BlockPos marketPos, Villager villager) {
        for (Villager member : village(level, marketPos)) {
            if (member == villager) {
                return true;
            }
        }
        return false;
    }

    public static List<Villager> village(ServerLevel level, BlockPos marketPos) {
        List<? extends Villager> loaded = level.getEntities(EntityTypes.VILLAGER, Villager::isAlive);
        List<Tie> ties = new ArrayList<>();
        Set<Section> centers = new HashSet<>();
        for (Villager villager : loaded) {
            for (BlockPos place : ties(level, villager)) {
                Section section = Section.of(place.getX(), place.getY(), place.getZ());
                centers.add(section);
                if (!villager.getOffers().isEmpty()) {
                    ties.add(new Tie(villager, section));
                }
            }
        }
        Set<Section> reached = VillageSections.reached(Section.of(marketPos.getX(), marketPos.getY(), marketPos.getZ()), centers);
        LinkedHashSet<Villager> members = new LinkedHashSet<>();
        for (Tie tie : ties) {
            if (reached.contains(tie.section)) {
                members.add(tie.villager);
            }
        }
        return List.copyOf(members);
    }

    public static List<Villager> forAutomaton(ServerLevel level, BlockPos pos) {
        BlockPos above = pos.above();
        if (!JobSites.workstation(level.getBlockState(above))) {
            return village(level, pos);
        }
        Villager owner = owner(level, above);
        if (owner == null) {
            return List.of();
        }
        return List.of(owner);
    }

    private static Villager owner(ServerLevel level, BlockPos workstation) {
        GlobalPos site = GlobalPos.of(level.dimension(), workstation);
        for (Villager villager : level.getEntities(EntityTypes.VILLAGER, Villager::isAlive)) {
            Optional<GlobalPos> job = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE);
            if (job.isPresent() && job.get().equals(site) && !villager.getOffers().isEmpty()) {
                return villager;
            }
        }
        return null;
    }

    private static List<BlockPos> ties(Level level, Villager villager) {
        List<BlockPos> places = new ArrayList<>(3);
        remember(level, villager, MemoryModuleType.HOME, places);
        remember(level, villager, MemoryModuleType.MEETING_POINT, places);
        remember(level, villager, MemoryModuleType.JOB_SITE, places);
        return places;
    }

    private static void remember(Level level, Villager villager, MemoryModuleType<GlobalPos> memory, List<BlockPos> places) {
        villager.getBrain().getMemory(memory).ifPresent(global -> {
            if (global.dimension().equals(level.dimension())) {
                places.add(global.pos());
            }
        });
    }

    private record Tie(Villager villager, Section section) {
    }
}
