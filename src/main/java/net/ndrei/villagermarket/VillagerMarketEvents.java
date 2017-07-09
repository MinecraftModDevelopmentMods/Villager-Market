package net.ndrei.villagermarket;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by CF on 2017-07-09.
 */
public class VillagerMarketEvents {
    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> ev) {
        VillagerMarketMod.villagerMarket.registerBlock(ev.getRegistry());
        VillagerMarketMod.villagerMarket.setCreativeTab(VillagerMarketMod.creativeTab);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> ev) {
        VillagerMarketMod.villagerMarket.registerItem(ev.getRegistry());
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(ModelRegistryEvent ev) {
        VillagerMarketMod.villagerMarket.registerRenderer();
    }
}
