package net.ndrei.villagermarket;

import com.google.common.collect.Lists;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Mod(modid = VillagerMarketMod.MODID, version = VillagerMarketMod.VERSION)
public class VillagerMarketMod {
    public static final String MODID = "villagermarket";
    public static final String VERSION = "1.0";

    @Mod.Instance
    @SuppressWarnings("unused")
    public static VillagerMarketMod instance;

    public static Logger logger;

    public static CreativeTabs creativeTab = new CreativeTabs("villager_market") {
        @Override
        public ItemStack getIconItemStack() {
            return new ItemStack(Item.getItemFromBlock(VillagerMarketMod.villagerMarket));
        }

        @Override
        public ItemStack getTabIconItem() {
            return this.getIconItemStack();
        }
    };

    private SimpleNetworkWrapper networkWrapper;

    public static VillagerMarketBlock villagerMarket;

    @Mod.EventHandler
    @SuppressWarnings("unused")
    public void preInit(FMLPreInitializationEvent event) {
        VillagerMarketMod.logger = event.getModLog();

        VillagerMarketMod.villagerMarket = new VillagerMarketBlock();
        VillagerMarketMod.villagerMarket.register();
        VillagerMarketMod.villagerMarket.setCreativeTab(VillagerMarketMod.creativeTab);

        if (event.getSide() == Side.CLIENT) {
            VillagerMarketMod.villagerMarket.registerRenderer();
        }

        NetworkRegistry.INSTANCE.registerGuiHandler(VillagerMarketMod.instance, new VillagerMarketGuiHandler());

        this.networkWrapper = new SimpleNetworkWrapper("VM|GUI");
        this.networkWrapper.registerMessage(VillagerMarketNetworkHandler.class, VillagerMarketNetworkPackage.class, 1, Side.CLIENT);
        this.networkWrapper.registerMessage(VillagerMarketNetworkHandler.class, VillagerMarketNetworkPackage.class, 1, Side.SERVER);
    }

    static List<ItemStack> getCombinedInventory(IInventory inventory) {
        List<ItemStack> list = Lists.newArrayList();
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }

            ItemStack match = null;
            for (ItemStack existing : list) {
                if (existing.getItem() == stack.getItem()) {
                    match = existing;
                    break;
                }
            }
            if (match == null) {
                list.add(stack.copy());
            } else {
                match.setCount(match.getCount() + stack.getCount());
            }
        }
        return list;
    }

    static int extractFromCombinedInventory(IInventory inventory, ItemStack stack, int amount) {
        if (stack.isEmpty()) {
            return 0;
        }

        int taken = 0;
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack temp = inventory.getStackInSlot(i);
            if (temp.isEmpty() || (temp.getItem() != stack.getItem())) {
                continue;
            }

            ItemStack takenStack;
            if (temp.getCount() == amount) {
                takenStack = inventory.removeStackFromSlot(i);
            } else {
                takenStack = inventory.decrStackSize(i, Math.min(amount, temp.getCount()));
            }

            taken += takenStack.getCount();
            amount -= takenStack.getCount();
            if (amount <= 0) {
                break;
            }
        }
        return taken;
    }

    static int getAmountOf(List<ItemStack> stacks, ItemStack stack, boolean fullCount, boolean isCombined) {
        int amount = 0;
        if (!stack.isEmpty()&& (stacks != null)) {
            for (ItemStack s : stacks) {
                if (s.isItemEqual(stack)) {
                    amount += s.getCount();
                    if (isCombined) {
                        break;
                    }
                }
            }
        }
        if (fullCount && !stack.isEmpty()) {
            amount /= stack.getCount();
        }
        return amount;
    }

    static void sendMessageToServer(NBTTagCompound message) {
        VillagerMarketMod.instance.networkWrapper.sendToServer(new VillagerMarketNetworkPackage(message));
    }

    static void sendMessageToClient(NBTTagCompound message, EntityPlayerMP player) {
        VillagerMarketMod.instance.networkWrapper.sendTo(new VillagerMarketNetworkPackage(message), player);
    }
}
