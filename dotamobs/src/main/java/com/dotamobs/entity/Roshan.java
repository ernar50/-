package com.dotamobs.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
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

/**
 * Roshan: a huge, very tough guardian of his lair. Twice the size of a player, 150 hearts of health.
 * Slam: every few seconds he stomps the ground, hurting, shoving and slowing everything around him.
 * Bash: his hits sometimes stun the victim for a moment.
 * When he dies he drops the Aegis (a Totem of Undying) and Cheese (see data/dotamobs/loot_table/entities/roshan.json).
 */
public class Roshan extends Monster {
    private static final int SLAM_COOLDOWN = 100;
    private static final double SLAM_RADIUS = 6.0;
    private static final float SLAM_DAMAGE = 8.0F;

    private int slamCooldown = 60;

    public Roshan(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 300.0)
            .add(Attributes.MOVEMENT_SPEED, 0.23)
            .add(Attributes.ATTACK_DAMAGE, 14.0)
            .add(Attributes.ARMOR, 10.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.SCALE, 1.6);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level() instanceof ServerLevel level && this.isAlive()) {
            if (this.slamCooldown > 0) {
                this.slamCooldown--;
            }
            LivingEntity target = this.getTarget();
            if (target != null && this.slamCooldown <= 0 && this.distanceToSqr(target) < 25.0) {
                slam(level);
                this.slamCooldown = SLAM_COOLDOWN;
            }
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        // Bash: a quarter of his hits stun the victim for a second (it cannot move).
        if (hit && target instanceof LivingEntity victim && victim.isAlive() && this.random.nextInt(4) == 0) {
            victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 6));
            victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 1));
            level.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY() + 1.0, victim.getZ(), 20, 0.4, 0.5, 0.4, 0.3);
        }
        return hit;
    }

    /** Stomp: everything within 6 blocks takes damage, is thrown back and slowed. */
    private void slam(ServerLevel level) {
        level.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 0.2, this.getZ(), 40, 3.0, 0.2, 3.0, 0.05);
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 2.0F, 0.6F);
        for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(SLAM_RADIUS),
                e -> e != this && e.isAlive())) {
            other.hurtServer(level, this.damageSources().mobAttack(this), SLAM_DAMAGE);
            other.knockback(1.2, this.getX() - other.getX(), this.getZ() - other.getZ(),
                this.damageSources().generic(), 0.0F, false);
            other.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
        }
    }
}
