package net.ndrei.villagermarket;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Created by CF on 2017-02-22.
 */
public class VillagerMarketNetwork {
    public static class ServerHandler implements IMessageHandler<VillagerMarketPacketServer, IMessage> {
        @Override
        public IMessage onMessage(VillagerMarketPacketServer message, MessageContext ctx) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
                if ((message != null) && (ctx.getServerHandler().player.openContainer instanceof VillagerMarketContainer)) {
                    if (message.getCompound().hasKey("LOAD", Constants.NBT.TAG_INT)) {
                        VillagerMarketContainer container = (VillagerMarketContainer) ctx.getServerHandler().player.openContainer;
                        VillagerMarketMod.sendMessageToClient(container.getNBTForMessage(), ctx.getServerHandler().player);
                    } else {
                        ((VillagerMarketContainer) ctx.getServerHandler().player.openContainer).processMessageFromClient(message.getCompound());
                    }
                }
            });

            return null;
        }
    }

    public static class ClientHandler implements IMessageHandler<VillagerMarketPacketClient, IMessage> {
        @Override
        public IMessage onMessage(VillagerMarketPacketClient message, MessageContext ctx) {
            if (Minecraft.getMinecraft().currentScreen instanceof VillagerMarketScreen) {
                ((VillagerMarketScreen) Minecraft.getMinecraft().currentScreen).readServerCompound(message.getCompound());
            }

            return null;
        }
    }
}
