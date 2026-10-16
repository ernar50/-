package com.dotamobs.registry;

import com.dotamobs.DotaMobs;
import com.dotamobs.loot.AddItemModifier;
import com.mojang.serialization.MapCodec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModLoot {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> MODIFIERS =
        DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, DotaMobs.MODID);

    static {
        MODIFIERS.register("add_item", AddItemModifier.CODEC);
    }

    private ModLoot() {}
}
