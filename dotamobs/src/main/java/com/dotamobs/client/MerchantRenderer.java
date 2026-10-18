package com.dotamobs.client;

import com.dotamobs.DotaMobs;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.Identifier;

/** The vanilla villager model and animations, with our own skin (villager texture layout, 64x64). */
public class MerchantRenderer extends VillagerRenderer {
    private static final Identifier TEXTURE =
        Identifier.fromNamespaceAndPath(DotaMobs.MODID, "textures/entity/merchant.png");

    public MerchantRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Identifier getTextureLocation(VillagerRenderState state) {
        return TEXTURE;
    }
}
