package com.dotamobs;

import com.dotamobs.entity.Alchemist;
import com.dotamobs.entity.CrystalMaiden;
import com.dotamobs.entity.MonkeyKing;
import com.dotamobs.entity.NightStalker;
import com.dotamobs.registry.ModEntities;
import com.dotamobs.registry.ModItems;
import com.dotamobs.util.ServerTasks;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DotaMobs.MODID)
public final class DotaMobs {
    public static final String MODID = "dotamobs";

    public DotaMobs(FMLJavaModLoadingContext context) {
        var modBus = context.getModBusGroup();

        ModEntities.ENTITIES.register(modBus);
        ModItems.ITEMS.register(modBus);

        // These events have their own BUS (they are no longer on the mod bus).
        EntityAttributeCreationEvent.BUS.addListener(DotaMobs::onAttributes);
        SpawnPlacementRegisterEvent.BUS.addListener(DotaMobs::onSpawnPlacements);
        BuildCreativeModeTabContentsEvent.BUS.addListener(DotaMobs::onCreativeTab);

        // Runs the short timed jobs (ice melting, hook pulling).
        TickEvent.ServerTickEvent.Post.BUS.addListener(ServerTasks::onServerTick);
    }

    private static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.NIGHT_STALKER.get(), NightStalker.createAttributes().build());
        event.put(ModEntities.ALCHEMIST.get(), Alchemist.createAttributes().build());
        event.put(ModEntities.MONKEY_KING.get(), MonkeyKing.createAttributes().build());
        event.put(ModEntities.CRYSTAL_MAIDEN.get(), CrystalMaiden.createAttributes().build());

        // The disguised animals behave exactly like the vanilla ones.
        event.put(ModEntities.DISGUISED_COW.get(), Cow.createAttributes().build());
        event.put(ModEntities.DISGUISED_PIG.get(), Pig.createAttributes().build());
        event.put(ModEntities.DISGUISED_SHEEP.get(), Sheep.createAttributes().build());
    }

    private static void onSpawnPlacements(SpawnPlacementRegisterEvent event) {
        // Night Stalker: on the surface, only at night.
        event.register(ModEntities.NIGHT_STALKER.get(),
            SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            NightStalker::checkSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);

        // Alchemist: only inside mineshafts.
        event.register(ModEntities.ALCHEMIST.get(),
            SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Alchemist::checkSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);

        // Monkey King: on top of tree leaves / logs. MOTION_BLOCKING counts leaves as ground,
        // so spawns land on the canopy instead of below it.
        event.register(ModEntities.MONKEY_KING.get(),
            SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING,
            MonkeyKing::checkSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);

        // Crystal Maiden: on snow, day or night.
        event.register(ModEntities.CRYSTAL_MAIDEN.get(),
            SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            CrystalMaiden::checkSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
    }

    private static void onCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.NIGHT_STALKER_SPAWN_EGG);
            event.accept(ModItems.ALCHEMIST_SPAWN_EGG);
            event.accept(ModItems.MONKEY_KING_SPAWN_EGG);
            event.accept(ModItems.CRYSTAL_MAIDEN_SPAWN_EGG);
        } else if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.PUDGE_HOOK);
        }
    }
}
