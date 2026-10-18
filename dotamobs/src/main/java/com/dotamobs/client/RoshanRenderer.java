package com.dotamobs.client;

import com.dotamobs.DotaMobs;
import com.dotamobs.entity.Roshan;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/** Draws Roshan with his own model (see {@link RoshanModel}). */
public class RoshanRenderer extends MobRenderer<Roshan, LivingEntityRenderState, RoshanModel> {
    private static final Identifier TEXTURE =
        Identifier.fromNamespaceAndPath(DotaMobs.MODID, "textures/entity/roshan.png");

    public RoshanRenderer(EntityRendererProvider.Context context) {
        super(context, new RoshanModel(context.bakeLayer(RoshanModel.LAYER)), 1.6F);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }
}
