package com.dotamobs.item;

import java.util.Optional;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
 * Right-click: shoves the creature you are looking at (up to 12 blocks away) far away from you.
 * If nobody is in front of you, or you sneak, it shoves you instead: a quick dash of about 7 blocks forward.
 */
public class ForceStaffItem extends Item {
    private static final double RANGE = 12.0;
    private static final double SELF_DASH = 7.0;
    private static final double PUSH_STRENGTH = 2.5;
    private static final int COOLDOWN = 120;

    public ForceStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = player.getItemInHand(hand);
        Vec3 look = player.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0.0, look.z);
        if (flat.lengthSqr() < 1.0E-6) {
            return InteractionResult.SUCCESS;
        }
        flat = flat.normalize();

        LivingEntity target = player.isShiftKeyDown() ? null : findTarget(level, player);
        if (target != null) {
            // Away from the player, along the direction he is looking.
            target.knockback(PUSH_STRENGTH, -flat.x, -flat.z, player.damageSources().generic(), 0.0F, false);
            serverLevel.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY() + 1.0, target.getZ(), 20, 0.3, 0.5, 0.3, 0.1);
            serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 1.0F, 1.2F);
            player.getCooldowns().addCooldown(stack, COOLDOWN);
            return InteractionResult.SUCCESS;
        }

        // Dash yourself forward: the farthest free spot, never through a wall.
        for (double d = SELF_DASH; d >= 1.0; d -= 1.0) {
            double dx = flat.x * d;
            double dz = flat.z * d;
            AABB moved = player.getBoundingBox().move(dx, 0.0, dz);
            if (level.noCollision(player, moved)) {
                serverLevel.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.5, player.getZ(), 20, 0.3, 0.4, 0.3, 0.05);
                player.teleportTo(player.getX() + dx, player.getY(), player.getZ() + dz);
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 1.0F, 1.2F);
                player.getCooldowns().addCooldown(stack, COOLDOWN);
                return InteractionResult.SUCCESS;
            }
        }
        player.getCooldowns().addCooldown(stack, 10);
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
