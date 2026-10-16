package com.dotamobs.entity;

import com.dotamobs.util.ServerTasks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;

/**
 * Zeus: calls down lightning on his target from up to 20 blocks away. First sparks appear on the ground
 * under the victim, and the bolt falls about 0.75 seconds later, so a player who keeps moving can dodge it.
 * The bolt is "visual only": it hurts creatures but does not set the world on fire.
 */
public class Zeus extends Monster implements RangedAttackMob {
    /** Delay between the warning sparks and the bolt, in ticks. */
    private static final int WARNING_TICKS = 15;
    private static final float BOLT_DAMAGE = 6.0F;
    /** Half-width (blocks) of the area hit by the bolt. */
    private static final double BOLT_RADIUS = 1.5;

    public Zeus(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 40.0)
            .add(Attributes.MOVEMENT_SPEED, 0.26)
            .add(Attributes.FOLLOW_RANGE, 28.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.3);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // One bolt every 4 seconds, from up to 20 blocks away.
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 80, 20.0F));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9));
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

        // The bolt lands where the target stands NOW, not where it will be.
        double x = target.getX();
        double y = target.getY();
        double z = target.getZ();
        Zeus zeus = this;
        int[] ticks = {0};

        ServerTasks.add(server -> {
            if (ticks[0] < WARNING_TICKS) {
                ticks[0]++;
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y + 0.1, z, 6, 0.6, 0.05, 0.6, 0.0);
                return false;
            }
            strike(level, zeus, x, y, z);
            return true;
        });
    }

    private static void strike(ServerLevel level, Zeus zeus, double x, double y, double z) {
        LightningBolt bolt = new LightningBolt(EntityTypes.LIGHTNING_BOLT, level);
        bolt.setPos(x, y, z);
        bolt.setVisualOnly(true);
        level.addFreshEntity(bolt);

        AABB area = new AABB(x - BOLT_RADIUS, y - 1.0, z - BOLT_RADIUS, x + BOLT_RADIUS, y + 3.0, z + BOLT_RADIUS);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != zeus && e.isAlive())) {
            victim.hurtServer(level, zeus.damageSources().mobAttack(zeus), BOLT_DAMAGE);
            victim.igniteForSeconds(2.0F);
        }
    }

    /** Natural spawn rule: under the open sky, day or night. */
    public static boolean checkSpawnRules(EntityType<Zeus> type, ServerLevelAccessor level,
                                          EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && level.canSeeSky(pos);
    }
}
