package net.ndrei.villagermarket;

import net.minecraft.nbt.NBTTagCompound;

public class VillagerMarketPacketClient extends VillagerMarketPacket {
    @SuppressWarnings("unused")
    public VillagerMarketPacketClient() { }

    public VillagerMarketPacketClient (NBTTagCompound compound) {
        this.compound = compound;
    }

}
