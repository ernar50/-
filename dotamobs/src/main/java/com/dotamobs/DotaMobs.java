package com.dotamobs;

import com.dotamobs.entity.Alchemist;
import com.dotamobs.entity.Axe;
import com.dotamobs.entity.Roshan;
import com.dotamobs.entity.CrystalMaiden;
import com.dotamobs.entity.DotaMerchant;
import com.dotamobs.entity.Earthshaker;
import com.dotamobs.entity.Juggernaut;
import com.dotamobs.entity.MonkeyKing;
import com.dotamobs.entity.NightStalker;
import com.dotamobs.entity.Riki;
import com.dotamobs.entity.Sniper;
import com.dotamobs.entity.Zeus;
import com.dotamobs.registry.ModEntities;
import com.dotamobs.registry.ModItems;
import com.dotamobs.registry.ModLoot;
import com.dotamobs.util.MerchantSpawner;
import com.dotamobs.util.RadianceEffects;
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
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DotaMobs.MODID)
public final class DotaMobs {
    public static final String MODID = "dotamobs";

    public DotaMobs(FMLJavaModLoadingContext context) {
        var modBus = context.getModBusGroup();

        ModEntities.ENTITIES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModLoot.MODIFIERS.register(modBus);

        // These events have their own BUS (they are no longer on the mod bus).
        EntityAttributeCreationEvent.BUS.addListener(DotaMobs::onAttributes);
        SpawnPlacementRegisterEvent.BUS.addListener(DotaMobs::onSpawnPlacements);
        BuildCreativeModeTabContentsEvent.BUS.addListener(DotaMobs::onCreativeTab);

        // Radiance: fire around the victim on a melee hit.
        LivingHurtEvent.BUS.addListener(RadianceEffects::onHurt);

        // Runs the short timed jobs (ice melting, hook pulling).
        TickEvent.ServerTickEvent.Post.BUS.addListener(ServerTasks::onServerTick);
        // Puts a merchant into villages.
        TickEvent.ServerTickEvent.Post.BUS.addListener(MerchantSpawner::onServerTick);
    }

    private static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.NIGHT_STALKER.get(), NightStalker.createAttributes().build());
        event.put(ModEntities.ALCHEMIST.get(), Alchemist.createAttributes().build());
        event.put(ModEntities.MONKEY_KING.get(), MonkeyKing.createAttributes().build());
        event.put(ModEntities.CRYSTAL_MAIDEN.get(), CrystalMaiden.createAttributes().build());
        event.put(ModEntities.RIKI.get(), Riki.createAttributes().build());
        event.put(ModEntities.ZEUS.get(), Zeus.createAttributes().build());
        event.put(ModEntities.AXE.get(), Axe.createAttributes().build());
        event.put(ModEntities.ROSHAN.get(), Roshan.createAttributes().build());
        event.put(ModEntities.SNIPER.get(), Sniper.createAttributes().build());
        event.put(ModEntities.JUGGERNAUT.get(), Juggernaut.createAttributes().build());
        event.put(ModEntities.EARTHSHAKER.get(), Earthshaker.createAttributes().build());
        event.put(ModEntities.MERCHANT.get(), DotaMerchant.createAttributes().build());

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

        // Riki: on the ground, day or night.
        event.register(ModEntities.RIKI.get(),
            SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Riki::checkSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);

        // Zeus: on the surface under the open sky, day or night.
        event.register(ModEntities.ZEUS.get(),
            SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Zeus::checkSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);

        // Axe: on the surface under the open sky, day or night.
        event.register(ModEntities.AXE.get(),
            SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Axe::checkSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);

        // Sniper: on the surface under the open sky, day or night.
        event.register(ModEntities.SNIPER.get(),
            SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Sniper::checkSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);

        // Juggernaut: on the surface under the open sky, day or night.
        event.register(ModEntities.JUGGERNAUT.get(),
            SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Juggernaut::checkSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);

        // Earthshaker: on the surface under the open sky, day or night.
        event.register(ModEntities.EARTHSHAKER.get(),
            SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Earthshaker::checkSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
    }

    private static void onCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.NIGHT_STALKER_SPAWN_EGG);
            event.accept(ModItems.ALCHEMIST_SPAWN_EGG);
            event.accept(ModItems.MONKEY_KING_SPAWN_EGG);
            event.accept(ModItems.CRYSTAL_MAIDEN_SPAWN_EGG);
            event.accept(ModItems.RIKI_SPAWN_EGG);
            event.accept(ModItems.ZEUS_SPAWN_EGG);
            event.accept(ModItems.AXE_SPAWN_EGG);
            event.accept(ModItems.SNIPER_SPAWN_EGG);
            event.accept(ModItems.JUGGERNAUT_SPAWN_EGG);
            event.accept(ModItems.EARTHSHAKER_SPAWN_EGG);
            event.accept(ModItems.MERCHANT_SPAWN_EGG);
            event.accept(ModItems.ROSHAN_SPAWN_EGG);
        } else if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.PUDGE_HOOK);
            event.accept(ModItems.BLINK_DAGGER);
            event.accept(ModItems.RADIANCE);
            event.accept(ModItems.EULS_SCEPTER);
            event.accept(ModItems.TOWN_PORTAL_SCROLL);
            event.accept(ModItems.CHEESE);
        }
    }
}
