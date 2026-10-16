package com.dotamobs.util;

import com.dotamobs.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * Radiance: while it is in your main hand, every melee hit deals a bit of extra damage and sets the
 * victim and all creatures around the victim on fire for a few seconds.
 */
public final class RadianceEffects {
    /** Extra damage of a hit with Radiance (a bare hand deals 1). */
    private static final float BONUS_DAMAGE = 4.0F;
    /** Burn time, in seconds. */
    private static final float BURN_SECONDS = 3.0F;
    /** Blocks around the victim that catch fire. */
    private static final double RADIUS = 4.0;

    private RadianceEffects() {}

    public static void onHurt(LivingHurtEvent event) {
        Entity direct = event.getSource().getDirectEntity();
        Entity owner = event.getSource().getEntity();
        // Melee only: arrows, tridents, etc. have a different direct entity.
        if (!(owner instanceof LivingEntity attacker) || direct != owner) {
            return;
        }
        if (!attacker.getMainHandItem().is(ModItems.RADIANCE.get())) {
            return;
        }
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level) || victim == attacker) {
            return;
        }

        event.setAmount(event.getAmount() + BONUS_DAMAGE);
        victim.igniteForSeconds(BURN_SECONDS);

        for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class,
                victim.getBoundingBox().inflate(RADIUS), e -> e != attacker && e != victim && e.isAlive())) {
            other.igniteForSeconds(BURN_SECONDS);
        }
        level.sendParticles(ParticleTypes.FLAME, victim.getX(), victim.getY() + 1.0, victim.getZ(),
            25, RADIUS / 3.0, 0.5, RADIUS / 3.0, 0.02);
    }
}
