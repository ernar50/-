package com.dotamobs.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Right-click to blink forward up to {@link #RANGE} blocks in the direction you are looking.
 * You stop in front of the first wall in the way.
 */
public class BlinkDaggerItem extends Item {
    private static final double RANGE = 12.0;
    private static final int COOLDOWN_SUCCESS = 100;
    private static final int COOLDOWN_FAIL = 10;

    public BlinkDaggerItem(Properties properties) {
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
        double eyeHeight = player.getEyeHeight();

        // How far can we go before a wall?
        double distance = RANGE;
        Vec3 end = eye.add(look.scale(RANGE));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.MISS) {
            distance = eye.distanceTo(hit.getLocation()) - 1.0;
        }

        double startX = player.getX();
        double startY = player.getY();
        double startZ = player.getZ();

        // Try the farthest point first and come back towards the player until a free spot is found.
        boolean success = false;
        for (double d = distance; d >= 2.0; d -= 1.0) {
            Vec3 spot = eye.add(look.scale(d));
            if (player.randomTeleport(spot.x, spot.y - eyeHeight, spot.z, true)) {
                success = true;
                break;
            }
        }

        if (success) {
            serverLevel.sendParticles(ParticleTypes.PORTAL, startX, startY + 1.0, startZ, 30, 0.3, 0.6, 0.3, 0.3);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
            serverLevel.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0, player.getZ(),
                30, 0.3, 0.6, 0.3, 0.3);
            player.getCooldowns().addCooldown(stack, COOLDOWN_SUCCESS);
        } else {
            player.getCooldowns().addCooldown(stack, COOLDOWN_FAIL);
        }
        return InteractionResult.SUCCESS;
    }
}
