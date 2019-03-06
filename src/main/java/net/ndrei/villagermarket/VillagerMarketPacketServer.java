package net.ndrei.villagermarket;

import net.minecraft.nbt.NBTTagCompound;

public class VillagerMarketPacketServer extends VillagerMarketPacket {
    @SuppressWarnings("unused")
    public VillagerMarketPacketServer() { }

    public VillagerMarketPacketServer (NBTTagCompound compound) {
        this.compound = compound;
    }
}
