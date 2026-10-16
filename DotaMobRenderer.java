package com.dotamobs.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;

/**
 * Renders a mob with the standard player model and a 64x64 player skin texture, so any
 * player skin PNG can be dropped in as the mob's look.
 */
public class DotaMobRenderer<T extends Mob> extends HumanoidMobRenderer<T, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {
    private final Identifier texture;

    public DotaMobRenderer(EntityRendererProvider.Context context, Identifier texture, float shadowRadius) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), shadowRadius);
        this.texture = texture;
    }

    @Override
    public HumanoidRenderState createRenderState() {
        return new HumanoidRenderState();
    }

    @Override
    public Identifier getTextureLocation(HumanoidRenderState state) {
        return this.texture;
    }
}
