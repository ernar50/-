package com.dotamobs.item;

import java.util.Optional;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Right-click: lifts the creature you are looking at (up to 12 blocks away) into a cyclone for 2 seconds.
 * It floats up, cannot move and lands softly. If nobody is in front of you, or you sneak,
 * it works on yourself: you rise and cannot be hurt for those 2 seconds.
 */
public class EulsScepterItem extends Item {
    private static final double RANGE = 12.0;
    private static final int COOLDOWN = 200;
    private static final int CYCLONE_TICKS = 40;

    public EulsScepterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = player.getItemInHand(hand);

        LivingEntity target = player.isShiftKeyDown() ? null : findTarget(level, player);
        LivingEntity affected = target != null ? target : player;

        affected.addEffect(new MobEffectInstance(MobEffects.LEVITATION, CYCLONE_TICKS, 2));
        affected.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, CYCLONE_TICKS + 120, 0));
        if (affected == player) {
            // Invulnerable while in the cyclone (Resistance V = no damage).
            affected.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, CYCLONE_TICKS, 4));
        } else {
            // Disabled: it cannot walk away while it spins.
            affected.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, CYCLONE_TICKS, 5));
        }
        serverLevel.sendParticles(ParticleTypes.CLOUD, affected.getX(), affected.getY() + 0.5, affected.getZ(),
            25, 0.4, 0.8, 0.4, 0.05);

        player.getCooldowns().addCooldown(stack, COOLDOWN);
        return InteractionResult.SUCCESS;
    }

    /** The closest creature along the line of sight, not through walls. */
    private static LivingEntity findTarget(Level level, Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(RANGE));
        BlockHitResult blockHit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (blockHit.getType() != HitResult.Type.MISS) {
            end = blockHit.getLocation();
        }

        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        AABB search = player.getBoundingBox().expandTowards(end.subtract(eye)).inflate(1.0);
        for (Entity entity : level.getEntities(player, search, e -> e instanceof LivingEntity && e.isPickable() && !e.isSpectator())) {
            Optional<Vec3> hit = entity.getBoundingBox().inflate(0.3).clip(eye, end);
            if (hit.isPresent()) {
                double distance = eye.distanceToSqr(hit.get());
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = (LivingEntity) entity;
                }
            }
        }
        return best;
    }
}
