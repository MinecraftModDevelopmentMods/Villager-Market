package net.modcrafters.minecraft.villagermarket.search;

import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.state.BlockState;

public final class JobSites {
    private JobSites() {
    }

    public static boolean workstation(BlockState state) {
        return PoiTypes.forState(state).filter(type -> type.is(PoiTypeTags.ACQUIRABLE_JOB_SITE)).isPresent();
    }
}
