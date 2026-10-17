package com.dotamobs.item;

import com.dotamobs.util.ServerTasks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * Right-click and stand still for 3 seconds: the scroll teleports you home (to your bed or respawn anchor;
 * if you have none, to the middle of the world) and is used up. Moving cancels it.
 */
public class TownPortalScrollItem extends Item {
    private static final int CHANNEL_TICKS = 60;
    /** How far (squared, in blocks) you may drift while reading before it is cancelled. */
    private static final double MAX_DRIFT_SQR = 1.0;
    private static final int COOLDOWN = 70;

    public TownPortalScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = player.getItemInHand(hand);
        Vec3 start = player.position();
        int[] ticks = {0};
        player.getCooldowns().addCooldown(stack, COOLDOWN);
        player.displayClientMessage(Component.translatable("msg.dotamobs.tp_start"), true);

        ServerTasks.add(server -> {
            if (!serverPlayer.isAlive() || serverPlayer.isRemoved()) {
                return true;
            }
            if (serverPlayer.position().distanceToSqr(start) > MAX_DRIFT_SQR) {
                serverPlayer.displayClientMessage(Component.translatable("msg.dotamobs.tp_cancel"), true);
                return true;
            }
            ticks[0]++;
            serverLevel.sendParticles(ParticleTypes.PORTAL, serverPlayer.getX(), serverPlayer.getY() + 1.0, serverPlayer.getZ(),
                6, 0.4, 0.8, 0.4, 0.5);
            if (ticks[0] < CHANNEL_TICKS) {
                return false;
            }

            teleportHome(serverLevel, serverPlayer);
            if (!serverPlayer.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return true;
        });
        return InteractionResult.SUCCESS;
    }

    private static void teleportHome(ServerLevel level, ServerPlayer player) {
        BlockPos home = null;
        var respawn = player.getRespawnConfig();
        if (respawn != null && respawn.respawnData().dimension() == level.dimension()) {
            // The bed (or anchor) block; stand on top of it.
            home = respawn.respawnData().pos().above();
        }
        if (home == null) {
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, 0, 0);
            home = new BlockPos(0, y, 0);
        }
        level.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.4, 0.8, 0.4, 0.5);
        player.teleportTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5);
        player.displayClientMessage(Component.translatable("msg.dotamobs.tp_done"), true);
    }
}
