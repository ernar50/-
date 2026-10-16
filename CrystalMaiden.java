package com.dotamobs.entity;

import com.dotamobs.util.IceCages;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Lives in snowy biomes. Attacks from range with a snowball: it hurts a little, slows the victim
 * down, covers them in frost and, for a few seconds, walls them in with ice.
 */
public class CrystalMaiden extends Monster implements RangedAttackMob {
    /** Damage of a snowball that hits. */
    private static final float HIT_DAMAGE = 2.0F;

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
        // Shoots every ~3 seconds from up to 16 blocks away.
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 60, 16.0F));
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

        // A real snowball: it flies in an arc, is stopped by blocks and by any creature in the way,
        // and only does its frost effects when it actually hits something alive.
        CrystalMaiden maiden = this;
        Snowball ball = new Snowball(level, this, new ItemStack(Items.SNOWBALL)) {
            @Override
            protected void onHitEntity(EntityHitResult hit) {
                super.onHitEntity(hit);
                if (hit.getEntity() instanceof LivingEntity victim && victim != maiden && victim.isAlive()) {
                    victim.hurtServer(level, maiden.damageSources().mobProjectile(this, maiden), HIT_DAMAGE);
                    victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 2));
                    victim.setTicksFrozen(200);
                    IceCages.freezeAround(level, victim);
                }
            }
        };

        double dx = target.getX() - this.getX();
        double dy = target.getEyeY() - 1.1 - ball.getY();
        double dz = target.getZ() - this.getZ();
        double arc = Math.sqrt(dx * dx + dz * dz) * 0.2;
        // Speed 1.4 and a little inaccuracy: a moving player can dodge it.
        ball.shoot(dx, dy + arc, dz, 1.4F, 8.0F);
        level.addFreshEntity(ball);
    }

    /** Natural spawn rule: under the open sky, day or night (snow biomes are picked by the biome modifier). */
    public static boolean checkSpawnRules(EntityType<CrystalMaiden> type, ServerLevelAccessor level,
                                          EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && level.canSeeSky(pos);
    }
}
