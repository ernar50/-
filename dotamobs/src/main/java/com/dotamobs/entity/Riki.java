package com.dotamobs.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
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
import net.minecraft.world.phys.Vec3;

/**
 * Riki: invisible almost all the time (only a faint wisp of smoke at his feet gives him away).
 * He becomes visible for a short while after attacking or being hit. A hit from behind
 * (a "backstab") does much more damage.
 */
public class Riki extends Monster {
    /** How long (ticks) he stays visible after attacking / being hit. */
    private static final int VISIBLE_AFTER_ATTACK = 40;
    private static final int VISIBLE_AFTER_HURT = 60;
    /** Damage multiplier for a hit in the back. */
    private static final double BACKSTAB_MULTIPLIER = 2.5;

    private int visibleTicks = 0;

    public Riki(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 24.0)
            .add(Attributes.MOVEMENT_SPEED, 0.32)
            .add(Attributes.ATTACK_DAMAGE, 4.0)
            .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level() instanceof ServerLevel serverLevel && this.isAlive()) {
            if (this.visibleTicks > 0) {
                this.visibleTicks--;
            }
            boolean hidden = this.visibleTicks <= 0;
            this.setInvisible(hidden);

            // A faint trail so a careful player can still spot him.
            if (hidden && this.tickCount % 12 == 0) {
                serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.1, this.getZ(),
                    1, 0.2, 0.02, 0.2, 0.0);
            }
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        AttributeInstance damage = this.getAttribute(Attributes.ATTACK_DAMAGE);
        double normal = damage == null ? 0.0 : damage.getBaseValue();
        boolean backstab = damage != null && isBehind(target);
        if (backstab) {
            damage.setBaseValue(normal * BACKSTAB_MULTIPLIER);
        }

        boolean hit = super.doHurtTarget(level, target);

        if (damage != null) {
            damage.setBaseValue(normal);
        }
        if (hit) {
            this.visibleTicks = VISIBLE_AFTER_ATTACK;
            if (backstab) {
                level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(),
                    12, 0.3, 0.4, 0.3, 0.2);
            }
        }
        return hit;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt) {
            this.visibleTicks = VISIBLE_AFTER_HURT;
        }
        return hurt;
    }

    /** True when Riki stands behind the target (the target is looking away from him). */
    private boolean isBehind(Entity target) {
        Vec3 facing = target.getLookAngle();
        double fx = facing.x;
        double fz = facing.z;
        double tx = this.getX() - target.getX();
        double tz = this.getZ() - target.getZ();
        double facingLength = Math.sqrt(fx * fx + fz * fz);
        double toLength = Math.sqrt(tx * tx + tz * tz);
        if (facingLength < 1.0E-4 || toLength < 1.0E-4) {
            return false;
        }
        double dot = (fx * tx + fz * tz) / (facingLength * toLength);
        return dot < -0.3;
    }

    /** Natural spawn rule: anywhere on the ground, day or night (the biome modifier keeps him rare). */
    public static boolean checkSpawnRules(EntityType<Riki> type, ServerLevelAccessor level,
                                          EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL;
    }
}
