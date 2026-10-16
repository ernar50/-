package com.dotamobs.registry;

import com.dotamobs.DotaMobs;
import com.dotamobs.item.PudgeHookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, DotaMobs.MODID);

    // Spawn egg colours are defined in assets/dotamobs/items/<name>.json (data-driven tints).
    public static final RegistryObject<Item> NIGHT_STALKER_SPAWN_EGG = ITEMS.register("night_stalker_spawn_egg",
        () -> new SpawnEggItem(new Item.Properties()
            .setId(ITEMS.key("night_stalker_spawn_egg"))
            .spawnEgg(ModEntities.NIGHT_STALKER.get())));

    public static final RegistryObject<Item> ALCHEMIST_SPAWN_EGG = ITEMS.register("alchemist_spawn_egg",
        () -> new SpawnEggItem(new Item.Properties()
            .setId(ITEMS.key("alchemist_spawn_egg"))
            .spawnEgg(ModEntities.ALCHEMIST.get())));

    public static final RegistryObject<Item> MONKEY_KING_SPAWN_EGG = ITEMS.register("monkey_king_spawn_egg",
        () -> new SpawnEggItem(new Item.Properties()
            .setId(ITEMS.key("monkey_king_spawn_egg"))
            .spawnEgg(ModEntities.MONKEY_KING.get())));

    public static final RegistryObject<Item> CRYSTAL_MAIDEN_SPAWN_EGG = ITEMS.register("crystal_maiden_spawn_egg",
        () -> new SpawnEggItem(new Item.Properties()
            .setId(ITEMS.key("crystal_maiden_spawn_egg"))
            .spawnEgg(ModEntities.CRYSTAL_MAIDEN.get())));

    public static final RegistryObject<Item> PUDGE_HOOK = ITEMS.register("pudge_hook",
        () -> new PudgeHookItem(new Item.Properties()
            .setId(ITEMS.key("pudge_hook"))
            .stacksTo(1)));

    private ModItems() {}
}
