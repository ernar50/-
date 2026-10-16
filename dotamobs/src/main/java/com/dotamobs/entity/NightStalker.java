package com.dotamobs.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.clock.ClockTimeMarkers;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * Hostile mob that spawns at night. Every time he hits a player, day turns into night.
 */
public class NightStalker extends Monster {
    // The overworld day is 24000 ticks long; the night is roughly ticks 13000..23000 of it.
    private static final long DAY_LENGTH = 24000L;
    private static final long NIGHT_START = 13000L;
    private static final long NIGHT_END = 23000L;

    public NightStalker(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 30.0)
            .add(Attributes.MOVEMENT_SPEED, 0.28)
            .add(Attributes.ATTACK_DAMAGE, 5.0)
            .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof Player) {
            makeNight(level);
        }
        return hit;
    }

    /** By day a spawn attempt succeeds 1 time in N. Raise N to make daytime spawns rarer. */
    private static final int DAY_SPAWN_ONE_IN = 10;

    /** True while it is night on the overworld clock. */
    public static boolean isNight(ServerLevel level) {
        var clock = level.registryAccess().getOrThrow(WorldClocks.OVERWORLD);
        long timeOfDay = level.clockManager().getInstance(clock).totalTicks() % DAY_LENGTH;
        return timeOfDay >= NIGHT_START && timeOfDay < NIGHT_END;
    }

    private static void makeNight(ServerLevel level) {
        if (level.dimension() != Level.OVERWORLD) {
            return;
        }
        // Already night: do nothing, otherwise every hit would rewind the clock.
        if (isNight(level)) {
            return;
        }
        var clock = level.registryAccess().getOrThrow(WorldClocks.OVERWORLD);
        level.clockManager().moveToTimeMarker(clock, ClockTimeMarkers.NIGHT);
    }

    /** Natural spawn rule: outside, under the open sky, at night, and rarely by day. */
    public static boolean checkSpawnRules(EntityType<NightStalker> type, ServerLevelAccessor level,
                                          EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        if (!level.canSeeSky(pos)) {
            return false;
        }
        // At night always; by day only on a small share of spawn attempts, so he is rare.
        return isNight(level.getLevel()) || random.nextInt(DAY_SPAWN_ONE_IN) == 0;
    }
}
