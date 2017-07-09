package net.ndrei.villagermarket;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.PacketLoggingHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Created by CF on 2017-02-22.
 */
public class VillagerMarketNetworkHandler implements IMessageHandler<VillagerMarketNetworkPackage, VillagerMarketNetworkPackage> {
    @Override
    public VillagerMarketNetworkPackage onMessage(VillagerMarketNetworkPackage message, MessageContext ctx) {
        if (ctx.side.isClient()) {
            // process client side message
//            Minecraft.getMinecraft().displayGuiScreen(
//                    new VillagerMarketScreen(new VillagerMarketContainer(message.getCompound(), Minecraft.getMinecraft().player))
//            );
            if (Minecraft.getMinecraft().currentScreen instanceof VillagerMarketScreen) {
                ((VillagerMarketScreen) Minecraft.getMinecraft().currentScreen).readServerCompound(message.getCompound());
            }
        } else {
            // processing server side message
            if ((message != null) && (ctx.getServerHandler().player.openContainer instanceof VillagerMarketContainer)) {
                if (message.getCompound().hasKey("LOAD", Constants.NBT.TAG_INT)) {
                    VillagerMarketContainer container = (VillagerMarketContainer) ctx.getServerHandler().player.openContainer;
                    return new VillagerMarketNetworkPackage(container.getNBTForMessage());
                }
                else {
                    ((VillagerMarketContainer) ctx.getServerHandler().player.openContainer).processMessageFromClient(message.getCompound());
                }
            }
        }
        return null;
    }
}
