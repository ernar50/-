package com.dotamobs.item;

import com.dotamobs.util.ServerTasks;
import java.util.Optional;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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
 * Right-click to throw the hook in the direction you are looking. The first creature it touches
 * (within {@link #RANGE} blocks, not through walls) is dragged towards you.
 */
public class PudgeHookItem extends Item {
    private static final double RANGE = 20.0;
    /** Cooldown after a hit / after a miss, in ticks. */
    private static final int COOLDOWN_HIT = 100;
    private static final int COOLDOWN_MISS = 40;
    /** The pull stops when the victim is this close (blocks) or after this many ticks. */
    private static final double STOP_DISTANCE = 2.0;
    private static final int MAX_PULL_TICKS = 40;

    public PudgeHookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = player.getItemInHand(hand);
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();

        // The hook stops at the first solid block.
        Vec3 end = eye.add(look.scale(RANGE));
        BlockHitResult blockHit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (blockHit.getType() != HitResult.Type.MISS) {
            end = blockHit.getLocation();
        }

        // Closest creature along the line.
        LivingEntity victim = null;
        Vec3 hitPoint = end;
        double best = Double.MAX_VALUE;
        AABB search = player.getBoundingBox().expandTowards(end.subtract(eye)).inflate(1.0);
        for (Entity entity : level.getEntities(player, search, e -> e instanceof LivingEntity && e.isPickable() && !e.isSpectator())) {
            Optional<Vec3> hit = entity.getBoundingBox().inflate(0.3).clip(eye, end);
            if (hit.isPresent()) {
                double distance = eye.distanceToSqr(hit.get());
                if (distance < best) {
                    best = distance;
                    victim = (LivingEntity) entity;
                    hitPoint = hit.get();
                }
            }
        }

        // The chain: a line of particles from the hand to where the hook ended up.
        int steps = (int) Math.max(4, eye.distanceTo(hitPoint) * 2);
        for (int i = 0; i <= steps; i++) {
            Vec3 p = eye.lerp(hitPoint, i / (double) steps);
            serverLevel.sendParticles(ParticleTypes.CRIT, p.x, p.y - 0.2, p.z, 1, 0.0, 0.0, 0.0, 0.0);
        }

        if (victim != null) {
            pull(victim, player);
            player.getCooldowns().addCooldown(stack, COOLDOWN_HIT);
        } else {
            player.getCooldowns().addCooldown(stack, COOLDOWN_MISS);
        }
        return InteractionResult.SUCCESS;
    }

    /** Drags the victim towards the thrower over a few ticks. */
    private static void pull(LivingEntity victim, Player owner) {
        int[] ticks = {0};
        ServerTasks.add(server -> {
            if (!victim.isAlive() || victim.isRemoved() || !owner.isAlive() || victim.level() != owner.level()) {
                return true;
            }
            double dx = owner.getX() - victim.getX();
            double dz = owner.getZ() - victim.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance < STOP_DISTANCE || ++ticks[0] > MAX_PULL_TICKS) {
                return true;
            }
            // knockback() pushes AWAY from the given direction, so the direction is flipped
            // to push the victim TOWARDS the owner. It also keeps the client in sync (players too).
            // In 26.3 knockback() also takes the damage source, the damage and a "comes from an effect" flag.
            victim.knockback(0.25, -dx, -dz, owner.damageSources().generic(), 0.0F, false);
            return false;
        });
    }
}
