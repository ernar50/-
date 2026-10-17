package com.dotamobs.entity;

import com.dotamobs.registry.ModItems;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

/**
 * A villager that lives in villages and sells Dota items for gold ingots.
 * Right-click opens the normal villager trading window; the shop list is {@link #CATALOG}.
 */
public class DotaMerchant extends Villager {
    private record Offer(Supplier<Item> item, int price) {}

    /** What he sells and for how many gold ingots. Edit this list to change the shop. */
    private static final List<Offer> CATALOG = List.of(
        new Offer(ModItems.TOWN_PORTAL_SCROLL, 3),
        new Offer(ModItems.PUDGE_HOOK, 12),
        new Offer(ModItems.EULS_SCEPTER, 14),
        new Offer(ModItems.BLINK_DAGGER, 16),
        new Offer(ModItems.RADIANCE, 24)
    );

    private MerchantOffers shop;

    public DotaMerchant(EntityType<? extends Villager> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 30.0)
            .add(Attributes.MOVEMENT_SPEED, 0.5)
            .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    /** Always our own shop, whatever job the villager brain thinks he has. */
    @Override
    public MerchantOffers getOffers() {
        if (this.shop == null) {
            this.shop = new MerchantOffers();
            for (Offer offer : CATALOG) {
                this.shop.add(new MerchantOffer(
                    new ItemCost(Items.GOLD_INGOT, offer.price()),
                    new ItemStack(offer.item().get()),
                    9999, 0, 0.0F));
            }
        }
        return this.shop;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.isAlive() && !this.isTrading()) {
            if (!this.level().isClientSide()) {
                this.setTradingPlayer(player);
                this.openTradingScreen(player, this.getDisplayName(), 1);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
