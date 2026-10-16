package com.dotamobs.registry;

import com.dotamobs.DotaMobs;
import com.dotamobs.entity.Alchemist;
import com.dotamobs.entity.CrystalMaiden;
import com.dotamobs.entity.DisguisedCow;
import com.dotamobs.entity.DisguisedPig;
import com.dotamobs.entity.DisguisedSheep;
import com.dotamobs.entity.MonkeyKing;
import com.dotamobs.entity.NightStalker;
import com.dotamobs.entity.Riki;
import com.dotamobs.entity.Zeus;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, DotaMobs.MODID);

    public static final RegistryObject<EntityType<NightStalker>> NIGHT_STALKER = ENTITIES.register("night_stalker",
        () -> EntityType.Builder.of(NightStalker::new, MobCategory.MONSTER)
            .sized(0.6F, 1.95F)
            .clientTrackingRange(8)
            .build(ENTITIES.key("night_stalker")));

    public static final RegistryObject<EntityType<Alchemist>> ALCHEMIST = ENTITIES.register("alchemist",
        () -> EntityType.Builder.of(Alchemist::new, MobCategory.MONSTER)
            .sized(0.7F, 1.8F)
            .clientTrackingRange(8)
            .build(ENTITIES.key("alchemist")));

    public static final RegistryObject<EntityType<CrystalMaiden>> CRYSTAL_MAIDEN = ENTITIES.register("crystal_maiden",
        () -> EntityType.Builder.of(CrystalMaiden::new, MobCategory.MONSTER)
            .sized(0.6F, 1.95F)
            .clientTrackingRange(10)
            .build(ENTITIES.key("crystal_maiden")));

    // MONSTER category on purpose: animals (CREATURE) almost only appear while a chunk is first generated,
    // so a tree-dweller would never show up in terrain you have already explored. MONSTER mobs are
    // spawned continuously around the player. The population is limited in MonkeyKing.checkSpawnRules.
    public static final RegistryObject<EntityType<MonkeyKing>> MONKEY_KING = ENTITIES.register("monkey_king",
        () -> EntityType.Builder.of(MonkeyKing::new, MobCategory.MONSTER)
            .sized(0.6F, 1.7F)
            .clientTrackingRange(10)
            .build(ENTITIES.key("monkey_king")));

    public static final RegistryObject<EntityType<Riki>> RIKI = ENTITIES.register("riki",
        () -> EntityType.Builder.of(Riki::new, MobCategory.MONSTER)
            .sized(0.6F, 1.8F)
            .clientTrackingRange(8)
            .build(ENTITIES.key("riki")));

    public static final RegistryObject<EntityType<Zeus>> ZEUS = ENTITIES.register("zeus",
        () -> EntityType.Builder.of(Zeus::new, MobCategory.MONSTER)
            .sized(0.6F, 1.95F)
            .clientTrackingRange(10)
            .build(ENTITIES.key("zeus")));

    // Peaceful look-alikes the Monkey King turns into. Never spawned naturally.
    public static final RegistryObject<EntityType<DisguisedCow>> DISGUISED_COW = ENTITIES.register("disguised_cow",
        () -> EntityType.Builder.of(DisguisedCow::new, MobCategory.CREATURE)
            .sized(0.9F, 1.4F)
            .clientTrackingRange(10)
            .build(ENTITIES.key("disguised_cow")));

    public static final RegistryObject<EntityType<DisguisedPig>> DISGUISED_PIG = ENTITIES.register("disguised_pig",
        () -> EntityType.Builder.of(DisguisedPig::new, MobCategory.CREATURE)
            .sized(0.9F, 0.9F)
            .clientTrackingRange(10)
            .build(ENTITIES.key("disguised_pig")));

    public static final RegistryObject<EntityType<DisguisedSheep>> DISGUISED_SHEEP = ENTITIES.register("disguised_sheep",
        () -> EntityType.Builder.of(DisguisedSheep::new, MobCategory.CREATURE)
            .sized(0.9F, 1.3F)
            .clientTrackingRange(10)
            .build(ENTITIES.key("disguised_sheep")));

    private ModEntities() {}
}
