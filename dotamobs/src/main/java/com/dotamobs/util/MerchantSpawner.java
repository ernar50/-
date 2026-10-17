package com.dotamobs.util;

import com.dotamobs.entity.DotaMerchant;
import com.dotamobs.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;

/**
 * Villages cannot be targeted by biome spawn rules, so this spawns the merchant itself: every 10 seconds,
 * for each player standing inside a village, there is a 1 in 3 chance that a merchant appears
 * nearby, if there is none within 64 blocks yet. He stays (never despawns) until he is killed.
 */
public final class MerchantSpawner {
    private static final int CHECK_INTERVAL = 200;
    private static final double NO_MERCHANT_RADIUS = 64.0;
    private static final int ATTEMPTS = 12;
    private static final int SPREAD = 16;

    private static int timer = 0;

    private MerchantSpawner() {}

    public static void onServerTick(TickEvent.ServerTickEvent.Post event) {
        if (++timer < CHECK_INTERVAL) {
            return;
        }
        timer = 0;

        for (ServerPlayer player : event.server().getPlayerList().getPlayers()) {
            if (!(player.level() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) {
                continue;
            }
            BlockPos here = player.blockPosition();
            if (!inVillage(level, here)) {
                continue;
            }
            if (!level.getEntitiesOfClass(DotaMerchant.class, new AABB(here).inflate(NO_MERCHANT_RADIUS)).isEmpty()) {
                continue;
            }
            if (level.getRandom().nextInt(3) != 0) {
                continue;
            }
            trySpawn(level, here);
        }
    }

    private static boolean inVillage(ServerLevel level, BlockPos pos) {
        return level.structureManager().getStructureWithPieceAt(pos, StructureTags.VILLAGE).isValid();
    }

    private static void trySpawn(ServerLevel level, BlockPos around) {
        for (int i = 0; i < ATTEMPTS; i++) {
            int x = around.getX() + level.getRandom().nextInt(SPREAD * 2 + 1) - SPREAD;
            int z = around.getZ() + level.getRandom().nextInt(SPREAD * 2 + 1) - SPREAD;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);

            // Near the player's height (not on far-away roofs), in the village, on solid ground.
            if (Math.abs(y - around.getY()) > 4 || !inVillage(level, pos)) {
                continue;
            }
            if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()
                    || level.getBlockState(pos.below()).isAir() || !level.getFluidState(pos.below()).isEmpty()) {
                continue;
            }

            DotaMerchant merchant = new DotaMerchant(ModEntities.MERCHANT.get(), level);
            merchant.setPos(x + 0.5, y, z + 0.5);
            merchant.setYRot(level.getRandom().nextFloat() * 360.0F);
            merchant.setPersistenceRequired();
            level.addFreshEntity(merchant);
            return;
        }
    }
}
