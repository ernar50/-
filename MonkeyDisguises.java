package com.dotamobs.entity;

import com.dotamobs.registry.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * The disguise is a real, separate animal entity (a subclass of the vanilla cow/pig/sheep that uses
 * the vanilla renderer), so it looks exactly like the animal. The first time a creature hits it,
 * it is swapped back for a Monkey King that targets the attacker.
 */
public final class MonkeyDisguises {
    private MonkeyDisguises() {}

    /** Turn the monkey into a random peaceful farm animal. */
    public static void disguise(MonkeyKing king, ServerLevel level) {
        switch (king.getRandom().nextInt(3)) {
            case 0 -> convert(king, ModEntities.DISGUISED_COW.get());
            case 1 -> convert(king, ModEntities.DISGUISED_PIG.get());
            default -> convert(king, ModEntities.DISGUISED_SHEEP.get());
        }
    }

    private static <T extends Mob> void convert(Mob from, EntityType<T> type) {
        from.convertTo(type, ConversionParams.single(from, false, false), animal -> animal.setPersistenceRequired());
    }

    /**
     * Called by a disguised animal when it takes damage.
     *
     * @return true if the animal was revealed (the caller must then skip the normal damage handling).
     */
    public static boolean reveal(Mob disguised, ServerLevel level, DamageSource source, float amount) {
        // Only being hit by another creature breaks the disguise (not fire, falling, the void...).
        if (!(source.getEntity() instanceof LivingEntity attacker)) {
            return false;
        }

        MonkeyKing king = disguised.convertTo(
            ModEntities.MONKEY_KING.get(),
            ConversionParams.single(disguised, false, false),
            monkey -> monkey.setTarget(attacker));
        if (king == null) {
            return false;
        }

        // The blow that revealed him still lands.
        king.hurtServer(level, source, amount);
        return true;
    }
}
