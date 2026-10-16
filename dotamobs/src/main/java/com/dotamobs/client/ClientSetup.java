package com.dotamobs.client;

import com.dotamobs.DotaMobs;
import com.dotamobs.entity.Alchemist;
import com.dotamobs.entity.CrystalMaiden;
import com.dotamobs.entity.MonkeyKing;
import com.dotamobs.entity.NightStalker;
import com.dotamobs.entity.Riki;
import com.dotamobs.entity.Zeus;
import com.dotamobs.registry.ModEntities;
import net.minecraft.client.renderer.entity.CowRenderer;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.client.renderer.entity.SheepRenderer;
import net.minecraft.resources.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DotaMobs.MODID, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {}

    private static Identifier skin(String name) {
        // Put your 64x64 player skin PNG at: src/main/resources/assets/dotamobs/textures/entity/<name>.png
        return Identifier.fromNamespaceAndPath(DotaMobs.MODID, "textures/entity/" + name + ".png");
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.NIGHT_STALKER.get(),
            context -> new DotaMobRenderer<NightStalker>(context, skin("night_stalker"), 0.5F));
        event.registerEntityRenderer(ModEntities.ALCHEMIST.get(),
            context -> new DotaMobRenderer<Alchemist>(context, skin("alchemist"), 0.5F));
        event.registerEntityRenderer(ModEntities.MONKEY_KING.get(),
            context -> new DotaMobRenderer<MonkeyKing>(context, skin("monkey_king"), 0.5F));
        event.registerEntityRenderer(ModEntities.CRYSTAL_MAIDEN.get(),
            context -> new DotaMobRenderer<CrystalMaiden>(context, skin("crystal_maiden"), 0.5F));
        event.registerEntityRenderer(ModEntities.RIKI.get(),
            context -> new DotaMobRenderer<Riki>(context, skin("riki"), 0.5F));
        event.registerEntityRenderer(ModEntities.ZEUS.get(),
            context -> new DotaMobRenderer<Zeus>(context, skin("zeus"), 0.5F));

        // Disguises use the exact vanilla renderers, so they are visually identical to real animals.
        event.registerEntityRenderer(ModEntities.DISGUISED_COW.get(), CowRenderer::new);
        event.registerEntityRenderer(ModEntities.DISGUISED_PIG.get(), PigRenderer::new);
        event.registerEntityRenderer(ModEntities.DISGUISED_SHEEP.get(), SheepRenderer::new);
    }
}
