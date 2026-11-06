package net.modcrafters.minecraft.villagermarket.search;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public final class VillageSections {
    private VillageSections() {
    }

    // A claimed bed, bell, or job site makes its chunk section a center. The village is the 3×3×3 of sections
    // around each center. Centers whose neighborhoods overlap belong to one village, and the market joins the
    // village whose neighborhood contains its own section.
    public static Set<Section> reached(Section origin, Set<Section> centers) {
        Set<Section> included = new HashSet<>();
        ArrayDeque<Section> queue = new ArrayDeque<>();
        for (Section center : centers) {
            if (origin.distance(center) <= 1) {
                included.add(center);
                queue.add(center);
            }
        }
        while (!queue.isEmpty()) {
            Section current = queue.removeFirst();
            for (Section center : centers) {
                if (!included.contains(center) && current.distance(center) <= 2) {
                    included.add(center);
                    queue.add(center);
                }
            }
        }
        return included;
    }
}
