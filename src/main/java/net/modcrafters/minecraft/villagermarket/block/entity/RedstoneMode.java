package net.modcrafters.minecraft.villagermarket.block.entity;

import net.minecraft.network.chat.Component;

public enum RedstoneMode {
    IGNORED,
    POWERED,
    UNPOWERED;

    public RedstoneMode next() {
        RedstoneMode[] modes = values();
        return modes[(ordinal() + 1) % modes.length];
    }

    public static RedstoneMode byId(int id) {
        RedstoneMode[] modes = values();
        if (id < 0 || id >= modes.length) {
            return IGNORED;
        }
        return modes[id];
    }

    public Component label() {
        return switch (this) {
            case IGNORED -> Component.translatable("gui.villagermarket.redstone.ignored");
            case POWERED -> Component.translatable("gui.villagermarket.redstone.powered");
            case UNPOWERED -> Component.translatable("gui.villagermarket.redstone.unpowered");
        };
    }
}
