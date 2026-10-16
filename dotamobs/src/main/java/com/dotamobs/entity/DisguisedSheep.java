package com.dotamobs.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.level.Level;

/** A sheep that is secretly a Monkey King. */
public class DisguisedSheep extends Sheep {
    public DisguisedSheep(EntityType<? extends Sheep> type, Level level) {
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
