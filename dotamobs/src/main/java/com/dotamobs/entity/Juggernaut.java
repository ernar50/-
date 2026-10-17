package com.dotamobs.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
 * Juggernaut: when an enemy gets close he starts Blade Fury. For 4 seconds he spins, cannot be hurt and
 * cuts everything within 3 blocks. When he is below half health he also calls a healing ward that
 * restores about 10 health over 5 seconds.
 */
public class Juggernaut extends Monster {
    private static final int FURY_TICKS = 80;
    private static final int FURY_COOLDOWN = 400;
    private static final double FURY_RADIUS = 3.2;
    private static final float FURY_DAMAGE = 3.0F;
    private static final int HEAL_TICKS = 100;
    private static final int HEAL_COOLDOWN = 600;

    private int furyTicks = 0;
    private int furyCooldown = 200;
    private int healTicks = 0;
    private int healCooldown = 0;

    public Juggernaut(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 60.0)
            .add(Attributes.MOVEMENT_SPEED, 0.27)
            .add(Attributes.ATTACK_DAMAGE, 6.0)
            .add(Attributes.ARMOR, 4.0)
            .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!(this.level() instanceof ServerLevel level) || !this.isAlive()) {
            return;
        }
        if (this.furyCooldown > 0) {
            this.furyCooldown--;
        }
        if (this.healCooldown > 0) {
            this.healCooldown--;
        }

        // --- Blade Fury ---
        LivingEntity target = this.getTarget();
        if (this.furyTicks == 0 && this.furyCooldown <= 0 && target != null && this.distanceToSqr(target) < 25.0) {
            this.furyTicks = FURY_TICKS;
            this.furyCooldown = FURY_COOLDOWN;
        }
        if (this.furyTicks > 0) {
            this.furyTicks--;
            this.setYRot(this.getYRot() + 40.0F);
            if (this.tickCount % 3 == 0) {
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX(), this.getY() + 1.0, this.getZ(), 2, 1.2, 0.3, 1.2, 0.0);
            }
            if (this.tickCount % 10 == 0) {
                for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(FURY_RADIUS),
                        e -> e != this && e.isAlive())) {
                    other.hurtServer(level, this.damageSources().mobAttack(this), FURY_DAMAGE);
                }
            }
        }

        // --- Healing ward ---
        if (this.healTicks == 0 && this.furyTicks == 0 && this.healCooldown <= 0
                && this.getHealth() < this.getMaxHealth() * 0.5F) {
            this.healTicks = HEAL_TICKS;
            this.healCooldown = HEAL_COOLDOWN;
        }
        if (this.healTicks > 0) {
            this.healTicks--;
            if (this.tickCount % 20 == 0) {
                this.heal(2.0F);
                level.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 2.0, this.getZ(), 4, 0.4, 0.3, 0.4, 0.0);
            }
        }
    }

    /** During Blade Fury he cannot be hurt. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.furyTicks > 0) {
            return false;
        }
        return super.hurtServer(level, source, amount);
    }

    /** Natural spawn rule: on the surface under the open sky, day or night. */
    public static boolean checkSpawnRules(EntityType<Juggernaut> type, ServerLevelAccessor level,
                                          EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && level.canSeeSky(pos);
    }
}
