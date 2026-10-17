package com.dotamobs.entity;

import com.dotamobs.util.ServerTasks;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Sniper: shoots from very far away. He first aims for about 1.25 seconds, drawing a thin white line to the
 * spot where you stood when he started aiming. The shot then goes along that line. If you have moved
 * out of the way or hidden behind a block, it misses.
 */
public class Sniper extends Monster implements RangedAttackMob {
    private static final double RANGE = 40.0;
    private static final int AIM_TICKS = 25;
    private static final float DAMAGE = 9.0F;

    public Sniper(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 24.0)
            .add(Attributes.MOVEMENT_SPEED, 0.25)
            .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // One shot every 5 seconds, from up to 32 blocks away.
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 100, 32.0F));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        // The aim point is fixed now. The target can still dodge by moving away.
        Vec3 aim = target.getBoundingBox().getCenter();
        Sniper sniper = this;
        int[] ticks = {0};

        ServerTasks.add(server -> {
            if (!sniper.isAlive() || sniper.isRemoved()) {
                return true;
            }
            if (ticks[0] < AIM_TICKS) {
                ticks[0]++;
                drawLine(level, sniper.getEyePosition(), aim, ParticleTypes.END_ROD, 1.5);
                return false;
            }
            shoot(level, sniper, aim);
            return true;
        });
    }

    private static void shoot(ServerLevel level, Sniper sniper, Vec3 aim) {
        Vec3 eye = sniper.getEyePosition();
        Vec3 dir = aim.subtract(eye);
        if (dir.lengthSqr() < 1.0E-6) {
            return;
        }
        Vec3 end = eye.add(dir.normalize().scale(RANGE));

        // The bullet stops at the first solid block.
        BlockHitResult blockHit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, sniper));
        if (blockHit.getType() != HitResult.Type.MISS) {
            end = blockHit.getLocation();
        }

        // ...or at the first creature on the line.
        LivingEntity victim = null;
        Vec3 hitPoint = end;
        double best = Double.MAX_VALUE;
        AABB search = new AABB(eye, end).inflate(1.0);
        for (Entity entity : level.getEntities(sniper, search, e -> e instanceof LivingEntity && e.isPickable() && !e.isSpectator())) {
            Optional<Vec3> hit = entity.getBoundingBox().inflate(0.3).clip(eye, end);
            if (hit.isPresent()) {
                double distance = eye.distanceToSqr(hit.get());
                if (distance < best) {
                    best = distance;
                    victim = (LivingEntity) entity;
                    hitPoint = hit.get();
                }
            }
        }

        drawLine(level, eye, hitPoint, ParticleTypes.CRIT, 1.0);
        level.playSound(null, sniper.getX(), sniper.getY(), sniper.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.HOSTILE, 2.0F, 0.6F);
        if (victim != null) {
            victim.hurtServer(level, sniper.damageSources().mobAttack(sniper), DAMAGE);
        }
    }

    private static void drawLine(ServerLevel level, Vec3 from, Vec3 to, ParticleOptions particle, double spacing) {
        int steps = (int) Math.max(2, from.distanceTo(to) / spacing);
        for (int i = 0; i <= steps; i++) {
            Vec3 p = from.lerp(to, i / (double) steps);
            level.sendParticles(particle, p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    /** Natural spawn rule: on the surface under the open sky, day or night. */
    public static boolean checkSpawnRules(EntityType<Sniper> type, ServerLevelAccessor level,
                                          EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && level.canSeeSky(pos);
    }
}
