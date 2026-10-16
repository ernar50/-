package com.dotamobs.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.level.Level;

/** A cow that is secretly a Monkey King. */
public class DisguisedCow extends Cow {
    public DisguisedCow(EntityType<? extends Cow> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (MonkeyDisguises.reveal(this, level, source, amount)) {
            return false;
        }
        return super.hurtServer(level, source, amount);
    }
}
