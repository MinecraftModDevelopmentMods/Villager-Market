package net.ndrei.villagermarket;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Created by CF on 2017-02-22.
 */
public class VillagerMarketNetworkPackage implements IMessage {
    private NBTTagCompound compound;

    @SuppressWarnings("unused")
    public VillagerMarketNetworkPackage() { }

    public VillagerMarketNetworkPackage(NBTTagCompound compound) {
        this.compound = compound;
    }

    public NBTTagCompound getCompound() { return this.compound; }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.compound = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, (this.compound != null) ? this.compound : new NBTTagCompound());
    }
}
