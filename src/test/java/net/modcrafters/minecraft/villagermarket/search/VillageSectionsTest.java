package net.modcrafters.minecraft.villagermarket.search;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VillageSectionsTest {
    @Test
    void aJobSiteInTheMarketsSectionIsReached() {
        Section origin = new Section(0, 0, 0);
        assertEquals(Set.of(origin), VillageSections.reached(origin, Set.of(origin)));
    }

    @Test
    void aJobSiteOneSectionAwayIsReached() {
        Section origin = new Section(0, 0, 0);
        Section beside = new Section(1, 0, 0);
        assertEquals(Set.of(beside), VillageSections.reached(origin, Set.of(beside)));
    }

    @Test
    void aJobSiteTwoSectionsAwayIsOut() {
        Section origin = new Section(0, 0, 0);
        Section away = new Section(2, 0, 0);
        assertTrue(VillageSections.reached(origin, Set.of(away)).isEmpty());
    }

    @Test
    void overlappingNeighborhoodsJoinAHall() {
        Section origin = new Section(0, 4, 1);
        Section near = new Section(1, 4, 1);
        Section next = new Section(3, 4, 1);
        assertEquals(Set.of(near, next), VillageSections.reached(origin, Set.of(near, next)));
    }

    @Test
    void aGapOfThreeSectionsStaysASeparateHall() {
        Section origin = new Section(0, 0, 0);
        Section near = new Section(1, 0, 0);
        Section far = new Section(4, 0, 0);
        assertEquals(Set.of(near), VillageSections.reached(origin, Set.of(near, far)));
    }

    @Test
    void aRowOfTouchingSectionsReachesTheFarEnd() {
        Section origin = new Section(0, 0, 0);
        Set<Section> hall = Set.of(
                new Section(0, 0, 0),
                new Section(1, 0, 0),
                new Section(2, 0, 0),
                new Section(3, 0, 0),
                new Section(4, 0, 0));
        assertEquals(hall, VillageSections.reached(origin, hall));
    }

    @Test
    void negativeSectionsUseTheSameStep() {
        Section origin = new Section(-1, -1, 0);
        Section across = new Section(1, -1, 0);
        assertTrue(VillageSections.reached(origin, Set.of(across)).isEmpty());
        assertEquals(Set.of(origin), VillageSections.reached(origin, Set.of(origin)));
    }

    @Test
    void blockCoordinatesLandOnSixteenBlockSections() {
        assertEquals(new Section(0, 0, -1), Section.of(15, 0, -1));
        assertEquals(new Section(-1, 1, 2), Section.of(-16, 16, 32));
    }
}
