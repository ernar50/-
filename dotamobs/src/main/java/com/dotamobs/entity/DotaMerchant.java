package com.dotamobs.entity;

import com.dotamobs.registry.ModItems;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * A friendly merchant that lives in villages and sells Dota items for gold ingots.
 * <ul>
 *   <li>The current item and its price are shown in his name tag.</li>
 *   <li>Sneak + right-click: show the next item.</li>
 *   <li>Right-click while holding enough gold ingots: buy the item.</li>
 * </ul>
 */
public class DotaMerchant extends PathfinderMob {
    private record Offer(Supplier<Item> item, int price) {}

    /** What he sells and for how many gold ingots. Edit this list to change the shop. */
    private static final List<Offer> CATALOG = List.of(
        new Offer(ModItems.TOWN_PORTAL_SCROLL, 3),
        new Offer(ModItems.PUDGE_HOOK, 12),
        new Offer(ModItems.EULS_SCEPTER, 14),
        new Offer(ModItems.BLINK_DAGGER, 16),
        new Offer(ModItems.RADIANCE, 24)
    );

    private int offerIndex = -1;

    public DotaMerchant(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 30.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3)
            .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // The first offer depends on his id, so merchants differ but each keeps the same one after a reload.
        if (this.offerIndex < 0 && !this.level().isClientSide()) {
            this.offerIndex = Math.floorMod(this.getUUID().hashCode(), CATALOG.size());
            updateName();
        }
    }

    private Offer current() {
        return CATALOG.get(Math.max(0, this.offerIndex) % CATALOG.size());
    }

    private void updateName() {
        Offer offer = current();
        ItemStack shown = new ItemStack(offer.item().get());
        this.setCustomName(Component.translatable("merchant.dotamobs.title", shown.getHoverName(), offer.price()));
        this.setCustomNameVisible(true);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!(this.level() instanceof ServerLevel level)) {
            return InteractionResult.SUCCESS;
        }
        if (this.offerIndex < 0) {
            this.offerIndex = Math.floorMod(this.getUUID().hashCode(), CATALOG.size());
        }

        // Sneak + right-click: next item.
        if (player.isShiftKeyDown()) {
            this.offerIndex = (this.offerIndex + 1) % CATALOG.size();
            updateName();
            return InteractionResult.SUCCESS;
        }

        Offer offer = current();
        ItemStack gold = player.getMainHandItem().is(Items.GOLD_INGOT) ? player.getMainHandItem() : player.getOffhandItem();
        if (gold.is(Items.GOLD_INGOT) && gold.getCount() >= offer.price()) {
            gold.shrink(offer.price());
            ItemStack reward = new ItemStack(offer.item().get());
            Component name = reward.getHoverName();
            if (!player.getInventory().add(reward)) {
                level.addFreshEntity(new ItemEntity(level, player.getX(), player.getY() + 0.5, player.getZ(), reward));
            }
            player.sendSystemMessage(Component.translatable("merchant.dotamobs.bought", name));
        } else {
            ItemStack shown = new ItemStack(offer.item().get());
            player.sendSystemMessage(Component.translatable("merchant.dotamobs.need", offer.price(), shown.getHoverName()));
        }
        return InteractionResult.SUCCESS;
    }
}
