package com.dotamobs.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;

/**
 * Surrounds a creature with ice for a few seconds. Packed ice is used on purpose: unlike normal ice
 * it does not turn into water when broken or melted, so the world is left exactly as it was.
 */
public final class IceCages {
    /** How long the ice stays, in ticks (100 = 5 seconds). */
    private static final int LIFETIME_TICKS = 100;

    private IceCages() {}

    public static void freezeAround(ServerLevel level, LivingEntity target) {
        BlockPos feet = target.blockPosition();
        List<BlockPos> placed = new ArrayList<>();

        for (int dy = 0; dy <= 2; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    boolean center = dx == 0 && dz == 0;
                    boolean wall = !center && dy <= 1;   // ring around the legs and the head
                    boolean roof = center && dy == 2;    // a block above the head
                    if (!wall && !roof) {
                        continue;
                    }
                    BlockPos pos = feet.offset(dx, dy, dz);
                    // Only into free air: never destroys or replaces anything that is already there.
                    if (level.getBlockState(pos).isAir()) {
                        level.setBlockAndUpdate(pos, Blocks.PACKED_ICE.defaultBlockState());
                        placed.add(pos);
                    }
                }
            }
        }

        if (placed.isEmpty()) {
            return;
        }

        int[] ticksLeft = {LIFETIME_TICKS};
        ServerTasks.add(server -> {
            if (--ticksLeft[0] > 0) {
                return false;
            }
            // Remove only what is still our ice, and only in chunks that are loaded.
            placed.removeIf(pos -> {
                if (!level.hasChunkAt(pos)) {
                    return false;
                }
                if (level.getBlockState(pos).is(Blocks.PACKED_ICE)) {
                    level.removeBlock(pos, false);
                }
                return true;
            });
            // If some chunks were not loaded, try again next tick.
            return placed.isEmpty();
        });
    }
}
