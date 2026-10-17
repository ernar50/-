package com.dotamobs.entity;

import com.dotamobs.util.ServerTasks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Earthshaker.
 * <ul>
 *   <li>Fissure: a crack runs from him towards the target (14 blocks). Everything it touches is thrown
 *       up for a moment, hurt and stunned.</li>
 *   <li>Aftershock: every melee hit also slams the creatures standing next to the victim.</li>
 * </ul>
 */
public class Earthshaker extends Monster {
    private static final int FISSURE_COOLDOWN = 160;
    private static final int FISSURE_LENGTH = 14;
    private static final float FISSURE_DAMAGE = 5.0F;
    private static final double AFTERSHOCK_RADIUS = 3.0;
    private static final float AFTERSHOCK_DAMAGE = 3.0F;

    private int fissureCooldown = 60;

    public Earthshaker(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 55.0)
            .add(Attributes.MOVEMENT_SPEED, 0.26)
            .add(Attributes.ATTACK_DAMAGE, 7.0)
            .add(Attributes.ARMOR, 4.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.4)
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
            if (this.fissureCooldown > 0) {
                this.fissureCooldown--;
            }
            LivingEntity target = this.getTarget();
            if (target != null && this.fissureCooldown <= 0 && this.hasLineOfSight(target)) {
                double distance = this.distanceTo(target);
                if (distance >= 4.0 && distance <= FISSURE_LENGTH) {
                    fissure(level, target);
                    this.fissureCooldown = FISSURE_COOLDOWN;
                }
            }
        }
    }

    /** The crack advances one block per tick, so a quick player can step aside. */
    private void fissure(ServerLevel level, LivingEntity target) {
        Vec3 from = this.position();
        Vec3 toTarget = target.position().subtract(from);
        Vec3 dir = new Vec3(toTarget.x, 0.0, toTarget.z);
        if (dir.lengthSqr() < 1.0E-6) {
            return;
        }
        Vec3 step = dir.normalize();
        Earthshaker self = this;
        List<LivingEntity> alreadyHit = new ArrayList<>();
        int[] distance = {0};

        ServerTasks.add(server -> {
            if (!self.isAlive() || self.isRemoved()) {
                return true;
            }
            distance[0]++;
            Vec3 p = from.add(step.scale(distance[0]));
            level.sendParticles(ParticleTypes.POOF, p.x, p.y + 0.2, p.z, 10, 0.4, 0.2, 0.4, 0.05);
            crackGround(level, p);

            AABB zone = new AABB(p.x - 1.0, p.y - 2.0, p.z - 1.0, p.x + 1.0, p.y + 3.0, p.z + 1.0);
            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, zone, e -> e != self && e.isAlive())) {
                if (alreadyHit.contains(victim)) {
                    continue;
                }
                alreadyHit.add(victim);
                victim.hurtServer(level, self.damageSources().mobAttack(self), FISSURE_DAMAGE);
                // A short upward hop, a soft landing and a stun.
                victim.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 12, 5));
                victim.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0));
                victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 5));
            }
            return distance[0] >= FISSURE_LENGTH;
        });
    }

    /** The fissure digs a trench: the ground under it and soft blocks in its way (up to 2 high) break, no drops. */
    private static void crackGround(ServerLevel level, Vec3 p) {
        BlockPos floor = BlockPos.containing(p.x, p.y - 0.5, p.z);
        for (int dy = 0; dy <= 2; dy++) {
            BlockPos pos = floor.above(dy);
            net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
            if (state.isAir() || level.getBlockEntity(pos) != null) {
                continue;
            }
            float hardness = state.getDestroySpeed(level, pos);
            // Dirt, sand, grass, gravel, leaves, snow... but never stone, ores, bedrock or obsidian.
            if (hardness >= 0.0F && hardness <= 0.9F && state.getFluidState().isEmpty()) {
                level.destroyBlock(pos, false);
            }
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        // Aftershock.
        if (hit) {
            level.sendParticles(ParticleTypes.POOF, target.getX(), target.getY() + 0.2, target.getZ(), 12, 1.0, 0.2, 1.0, 0.05);
            for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(AFTERSHOCK_RADIUS),
                    e -> e != this && e != target && e.isAlive())) {
                other.hurtServer(level, this.damageSources().mobAttack(this), AFTERSHOCK_DAMAGE);
                other.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
            }
        }
        return hit;
    }

    /** Natural spawn rule: on the surface under the open sky, day or night. */
    public static boolean checkSpawnRules(EntityType<Earthshaker> type, ServerLevelAccessor level,
                                          EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && level.canSeeSky(pos);
    }
}
