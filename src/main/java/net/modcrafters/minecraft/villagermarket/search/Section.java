package net.modcrafters.minecraft.villagermarket.search;

public record Section(int x, int y, int z) {
    public static Section of(int blockX, int blockY, int blockZ) {
        return new Section(blockX >> 4, blockY >> 4, blockZ >> 4);
    }

    public int distance(Section other) {
        return Math.max(Math.abs(x - other.x), Math.max(Math.abs(y - other.y), Math.abs(z - other.z)));
    }
}
