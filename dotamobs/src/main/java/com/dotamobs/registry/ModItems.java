package com.dotamobs.registry;

import com.dotamobs.DotaMobs;
import com.dotamobs.item.BlinkDaggerItem;
import com.dotamobs.item.EulsScepterItem;
import com.dotamobs.item.PudgeHookItem;
import com.dotamobs.item.TownPortalScrollItem;
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

    public static final RegistryObject<Item> RIKI_SPAWN_EGG = ITEMS.register("riki_spawn_egg",
        () -> new SpawnEggItem(new Item.Properties()
            .setId(ITEMS.key("riki_spawn_egg"))
            .spawnEgg(ModEntities.RIKI.get())));

    public static final RegistryObject<Item> ZEUS_SPAWN_EGG = ITEMS.register("zeus_spawn_egg",
        () -> new SpawnEggItem(new Item.Properties()
            .setId(ITEMS.key("zeus_spawn_egg"))
            .spawnEgg(ModEntities.ZEUS.get())));

    public static final RegistryObject<Item> BLINK_DAGGER = ITEMS.register("blink_dagger",
        () -> new BlinkDaggerItem(new Item.Properties()
            .setId(ITEMS.key("blink_dagger"))
            .stacksTo(1)));

    // Its effect (fire around the victim on a melee hit) lives in util/RadianceEffects.java.
    public static final RegistryObject<Item> RADIANCE = ITEMS.register("radiance",
        () -> new Item(new Item.Properties()
            .setId(ITEMS.key("radiance"))
            .stacksTo(1)));

    public static final RegistryObject<Item> AXE_SPAWN_EGG = ITEMS.register("axe_spawn_egg",
        () -> new SpawnEggItem(new Item.Properties()
            .setId(ITEMS.key("axe_spawn_egg"))
            .spawnEgg(ModEntities.AXE.get())));

    public static final RegistryObject<Item> SNIPER_SPAWN_EGG = ITEMS.register("sniper_spawn_egg",
        () -> new SpawnEggItem(new Item.Properties()
            .setId(ITEMS.key("sniper_spawn_egg"))
            .spawnEgg(ModEntities.SNIPER.get())));

    public static final RegistryObject<Item> JUGGERNAUT_SPAWN_EGG = ITEMS.register("juggernaut_spawn_egg",
        () -> new SpawnEggItem(new Item.Properties()
            .setId(ITEMS.key("juggernaut_spawn_egg"))
            .spawnEgg(ModEntities.JUGGERNAUT.get())));

    public static final RegistryObject<Item> EARTHSHAKER_SPAWN_EGG = ITEMS.register("earthshaker_spawn_egg",
        () -> new SpawnEggItem(new Item.Properties()
            .setId(ITEMS.key("earthshaker_spawn_egg"))
            .spawnEgg(ModEntities.EARTHSHAKER.get())));

    public static final RegistryObject<Item> MERCHANT_SPAWN_EGG = ITEMS.register("merchant_spawn_egg",
        () -> new SpawnEggItem(new Item.Properties()
            .setId(ITEMS.key("merchant_spawn_egg"))
            .spawnEgg(ModEntities.MERCHANT.get())));

    public static final RegistryObject<Item> EULS_SCEPTER = ITEMS.register("euls_scepter",
        () -> new EulsScepterItem(new Item.Properties()
            .setId(ITEMS.key("euls_scepter"))
            .stacksTo(1)));

    public static final RegistryObject<Item> TOWN_PORTAL_SCROLL = ITEMS.register("town_portal_scroll",
        () -> new TownPortalScrollItem(new Item.Properties()
            .setId(ITEMS.key("town_portal_scroll"))
            .stacksTo(16)));

    public static final RegistryObject<Item> PUDGE_HOOK = ITEMS.register("pudge_hook",
        () -> new PudgeHookItem(new Item.Properties()
            .setId(ITEMS.key("pudge_hook"))
            .stacksTo(1)));

    private ModItems() {}
}
