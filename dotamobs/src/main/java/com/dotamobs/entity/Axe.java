package com.dotamobs.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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
 * Axe: a tough brawler with three tricks.
 * <ul>
 *   <li>Berserker's Call: roars and makes every hostile mob nearby go after the player.</li>
 *   <li>Culling Blade: a hit that leaves the victim on 3 hearts or less finishes them off.</li>
 *   <li>Counter Helix: when hit, he sometimes spins and hurts everything around him.</li>
 * </ul>
 */
public class Axe extends Monster {
    private static final int CALL_COOLDOWN = 240;
    private static final double CALL_RADIUS = 14.0;
    private static final float CULL_HEALTH = 6.0F;
    private static final int HELIX_COOLDOWN = 60;
    private static final double HELIX_RADIUS = 3.0;
    private static final float HELIX_DAMAGE = 4.0F;

    private int callCooldown = 0;
    private int helixCooldown = 0;

    public Axe(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 50.0)
            .add(Attributes.MOVEMENT_SPEED, 0.29)
            .add(Attributes.ATTACK_DAMAGE, 7.0)
            .add(Attributes.ARMOR, 6.0)
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
    public void aiStep() {
        super.aiStep();

        if (this.level() instanceof ServerLevel level && this.isAlive()) {
            if (this.callCooldown > 0) {
                this.callCooldown--;
            }
            if (this.helixCooldown > 0) {
                this.helixCooldown--;
            }
            LivingEntity target = this.getTarget();
            if (target instanceof Player && this.callCooldown <= 0 && this.distanceToSqr(target) < CALL_RADIUS * CALL_RADIUS) {
                berserkersCall(level, target);
                this.callCooldown = CALL_COOLDOWN;
            }
        }
    }

    /** Every other hostile mob close by now hunts the same player. */
    private void berserkersCall(ServerLevel level, LivingEntity player) {
        level.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getY() + 2.0, this.getZ(), 12, 0.5, 0.3, 0.5, 0.0);
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 1.0F, 1.0F);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(CALL_RADIUS),
                e -> e != this && e != player && e instanceof Monster && e.isAlive())) {
            mob.setTarget(player);
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        // Culling Blade: whoever is nearly dead after the hit is finished off.
        if (hit && target instanceof LivingEntity victim && victim.isAlive() && victim.getHealth() <= CULL_HEALTH) {
            level.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY() + 1.0, victim.getZ(), 20, 0.3, 0.5, 0.3, 0.3);
            victim.hurtServer(level, this.damageSources().mobAttack(this), 1000.0F);
        }
        return hit;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        // Counter Helix: a spin that hurts everything around him.
        if (hurt && this.isAlive() && this.helixCooldown <= 0 && source.getEntity() != null && this.random.nextInt(4) == 0) {
            this.helixCooldown = HELIX_COOLDOWN;
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX(), this.getY() + 1.0, this.getZ(), 8, 1.2, 0.3, 1.2, 0.0);
            for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(HELIX_RADIUS),
                    e -> e != this && e.isAlive())) {
                other.hurtServer(level, this.damageSources().mobAttack(this), HELIX_DAMAGE);
                other.knockback(0.5, this.getX() - other.getX(), this.getZ() - other.getZ(),
                    this.damageSources().generic(), 0.0F, false);
            }
        }
        return hurt;
    }

    /** Natural spawn rule: on the surface under the open sky, day or night. */
    public static boolean checkSpawnRules(EntityType<Axe> type, ServerLevelAccessor level,
                                          EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && level.canSeeSky(pos);
    }
}
