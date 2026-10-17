package com.dotamobs.entity;

import com.dotamobs.util.IceCages;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

/**
 * Lives in snowy biomes. Attacks from range with a frost bolt: it hurts a little, slows the victim
 * down, covers them in frost and, for a few seconds, walls them in with ice.
 */
public class CrystalMaiden extends Monster implements RangedAttackMob {
    public CrystalMaiden(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 28.0)
            .add(Attributes.MOVEMENT_SPEED, 0.25)
            .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Shoots every ~2.5 seconds from up to 16 blocks away.
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 50, 16.0F));
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

        // A trail of snowflakes from her to the target, so the shot is visible.
        Vec3 from = this.getEyePosition();
        Vec3 to = target.getBoundingBox().getCenter();
        for (int i = 0; i <= 20; i++) {
            Vec3 p = from.lerp(to, i / 20.0);
            level.sendParticles(ParticleTypes.SNOWFLAKE, p.x, p.y, p.z, 2, 0.05, 0.05, 0.05, 0.0);
        }

        target.hurtServer(level, this.damageSources().mobAttack(this), 3.0F);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 2));
        target.setTicksFrozen(200);
        IceCages.freezeAround(level, target);
    }

    /** Natural spawn rule: under the open sky, day or night (snow biomes are picked by the biome modifier). */
    public static boolean checkSpawnRules(EntityType<CrystalMaiden> type, ServerLevelAccessor level,
                                          EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && level.canSeeSky(pos);
    }
}
