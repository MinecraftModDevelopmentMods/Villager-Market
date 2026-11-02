package net.modcrafters.minecraft.villagermarket.trade;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.ArrayList;
import java.util.List;

public record ListedOffer(
        int villagerId,
        int offerIndex,
        String groupKey,
        ItemStack costA,
        ItemStack costB,
        ItemStack result,
        int usesLeft,
        int maxUses
) {
    public static final StreamCodec<RegistryFriendlyByteBuf, ListedOffer> STREAM_CODEC = StreamCodec.of(
            (buf, offer) -> {
                ByteBufCodecs.INT.encode(buf, offer.villagerId);
                ByteBufCodecs.INT.encode(buf, offer.offerIndex);
                ByteBufCodecs.STRING_UTF8.encode(buf, offer.groupKey);
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, offer.costA);
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, offer.costB);
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, offer.result);
                ByteBufCodecs.INT.encode(buf, offer.usesLeft);
                ByteBufCodecs.INT.encode(buf, offer.maxUses);
            },
            buf -> new ListedOffer(
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ItemStack.OPTIONAL_STREAM_CODEC.decode(buf),
                    ItemStack.OPTIONAL_STREAM_CODEC.decode(buf),
                    ItemStack.OPTIONAL_STREAM_CODEC.decode(buf),
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.INT.decode(buf)
            )
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, List<ListedOffer>> LIST_CODEC = STREAM_CODEC.apply(ByteBufCodecs.list());

    public static List<ListedOffer> from(Villager villager) {
        String customName = villager.hasCustomName() ? villager.getCustomName().getString() : null;
        //? if >=26.1 {
        String professionId = villager.getVillagerData().profession().unwrapKey()
                .map(key -> key.identifier().toString())
                .orElse("minecraft:none");
        //?} else {
        /*Identifier profession = BuiltInRegistries.VILLAGER_PROFESSION.getKey(villager.getVillagerData().getProfession());
        String professionId = profession == null ? "minecraft:none" : profession.toString();
        *///?}
        String group = TradeGroups.groupKey(customName, professionId);
        MerchantOffers offers = villager.getOffers();
        List<ListedOffer> listed = new ArrayList<>();
        for (int index = 0; index < offers.size(); index++) {
            MerchantOffer offer = offers.get(index);
            int maxUses = offer.getMaxUses();
            int usesLeft = Math.max(0, maxUses - offer.getUses());
            listed.add(new ListedOffer(
                    villager.getId(),
                    index,
                    group,
                    offer.getCostA().copy(),
                    offer.getCostB().copy(),
                    offer.getResult().copy(),
                    usesLeft,
                    maxUses
            ));
        }
        return listed;
    }
}
