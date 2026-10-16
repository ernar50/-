package com.dotamobs.entity.ai;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/**
 * A very high leap towards the current target (about 5-6 blocks up, then down onto the target).
 * Used between {@code minDistance} and {@code maxDistance} blocks away.
 */
public class HighLeapGoal extends Goal {
    private final Mob mob;
    private final double minDistance;
    private final double maxDistance;
    @Nullable
    private LivingEntity target;
    private long nextLeapTime;

    public HighLeapGoal(Mob mob, double minDistance, double maxDistance) {
        this.mob = mob;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isPassenger()) {
            return false;
        }
        this.target = this.mob.getTarget();
        if (this.target == null || !this.target.isAlive() || !this.mob.onGround()) {
            return false;
        }
        if (this.mob.level().getGameTime() < this.nextLeapTime) {
            return false;
        }
        double distanceSqr = this.mob.distanceToSqr(this.target);
        if (distanceSqr < this.minDistance * this.minDistance || distanceSqr > this.maxDistance * this.maxDistance) {
            return false;
        }
        return this.mob.getRandom().nextInt(4) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        // Stay "active" while in the air so path-following does not fight the jump.
        return !this.mob.onGround();
    }

    @Override
    public void start() {
        LivingEntity target = this.target;
        if (target == null) {
            return;
        }

        double dx = target.getX() - this.mob.getX();
        double dz = target.getZ() - this.mob.getZ();
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        if (horizontalDistance < 1.0E-4) {
            return;
        }

        // The flight lasts about 25 ticks and air drag eats into the horizontal speed,
        // so the distance covered is roughly 9 * initial speed.
        double horizontalSpeed = Math.min(1.6, horizontalDistance / 9.0 + 0.05);
        double heightDifference = Math.max(0.0, Math.min(4.0, target.getY() - this.mob.getY()));
        double verticalSpeed = 1.0 + heightDifference * 0.04;

        this.mob.setDeltaMovement(new Vec3(dx / horizontalDistance * horizontalSpeed, verticalSpeed, dz / horizontalDistance * horizontalSpeed));
        this.mob.getNavigation().stop();

        this.nextLeapTime = this.mob.level().getGameTime() + 60 + this.mob.getRandom().nextInt(40);
    }
}
