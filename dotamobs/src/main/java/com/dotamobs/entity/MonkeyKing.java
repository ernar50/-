package com.dotamobs.entity;

import com.dotamobs.entity.ai.HighLeapGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Lives on trees, jumps very high and hits hard. Now and then, while idle, he turns into a peaceful
 * farm animal (see {@link MonkeyDisguises}) and stays that way until somebody hits him.
 */
public class MonkeyKing extends Monster {
    /** How often (in ticks) an idle monkey rolls the dice for a disguise. */
    private static final int DISGUISE_CHECK_INTERVAL = 100;
    /** The roll is "1 in N". Raise N to make disguises rarer. */
    private static final int DISGUISE_ONE_IN = 40;
    /** A monkey must live at least this long (ticks) before he can disguise. */
    private static final int MIN_AGE_BEFORE_DISGUISE = 400;

    public MonkeyKing(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 32.0)
            .add(Attributes.MOVEMENT_SPEED, 0.33)
            .add(Attributes.ATTACK_DAMAGE, 5.0)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            // Even a plain hop is a big one, and falls from trees do not hurt him.
            .add(Attributes.JUMP_STRENGTH, 0.9)
            .add(Attributes.SAFE_FALL_DISTANCE, 24.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new HighLeapGoal(this, 4.0, 14.0));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level() instanceof ServerLevel serverLevel
            && this.isAlive()
            && this.getTarget() == null
            && this.tickCount > MIN_AGE_BEFORE_DISGUISE
            && this.tickCount % DISGUISE_CHECK_INTERVAL == 0
            && this.random.nextInt(DISGUISE_ONE_IN) == 0) {
            // Replaces this entity with a disguised animal; nothing more to do here afterwards.
            MonkeyDisguises.disguise(this, serverLevel);
        }
    }

    /** At most this many Monkey Kings within {@link #CROWD_RADIUS} blocks of a new spawn. */
    private static final int MAX_NEARBY = 2;
    private static final double CROWD_RADIUS = 40.0;

    /** Natural spawn rule: standing on tree leaves or logs, and not too many monkeys around already. */
    public static boolean checkSpawnRules(EntityType<MonkeyKing> type, ServerLevelAccessor level,
                                          EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        if (!level.getBlockState(pos).isAir()) {
            return false;
        }
        BlockState below = level.getBlockState(pos.below());
        if (!(below.is(BlockTags.LEAVES) || below.is(BlockTags.LOGS))) {
            return false;
        }
        // Checked last because it is the most expensive test. Without it the spawner would keep
        // adding monkeys until the monster cap is full, since they ignore daylight.
        return level.getEntitiesOfClass(MonkeyKing.class, new AABB(pos).inflate(CROWD_RADIUS)).size() < MAX_NEARBY;
    }
}
