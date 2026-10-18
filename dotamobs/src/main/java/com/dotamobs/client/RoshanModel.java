package com.dotamobs.client;

import com.dotamobs.DotaMobs;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Roshan's own model: a big hunched stone brute with long arms, small wings and a yellow stone on its forehead.
 * The cubes and the texture (textures/entity/roshan.png, 128x128) were generated together.
 */
public class RoshanModel extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER =
        new ModelLayerLocation(Identifier.fromNamespaceAndPath(DotaMobs.MODID, "roshan"), "main");

    private final ModelPart head;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart leftWing;
    private final ModelPart rightWing;

    public RoshanModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-10F, -17F, -7F, 20F, 18F, 13F)
                .texOffs(66, 0).addBox(-2F, -21F, -3F, 4F, 5F, 4F)
                .texOffs(82, 0).addBox(-2F, -20F, 2F, 4F, 5F, 4F)
                .texOffs(98, 0).addBox(-7F, -19F, -1F, 3F, 4F, 3F)
                .texOffs(110, 0).addBox(4F, -19F, -1F, 3F, 4F, 3F),
            PartPose.offsetAndRotation(0F, 15F, 0F, 0.45F, 0F, 0F));
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 31).addBox(-5F, -5F, -9F, 10F, 9F, 10F)
                .texOffs(40, 31).addBox(-2F, -4F, -9.6F, 4F, 4F, 1F),
            PartPose.offsetAndRotation(0F, -1F, -9F, 0.15F, 0F, 0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(50, 31).addBox(-4F, 0F, -4F, 8F, 18F, 8F)
                .texOffs(82, 31).addBox(-5F, 13F, -5F, 10F, 8F, 10F),
            PartPose.offsetAndRotation(13F, 2F, -5F, -0.1F, 0F, 0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(0, 57).addBox(-4F, 0F, -4F, 8F, 18F, 8F)
                .texOffs(32, 57).addBox(-5F, 13F, -5F, 10F, 8F, 10F),
            PartPose.offsetAndRotation(-13F, 2F, -5F, -0.1F, 0F, 0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(72, 57).addBox(-4.5F, 0F, -4.5F, 9F, 9F, 9F),
            PartPose.offsetAndRotation(6.5F, 15F, 1F, 0F, 0F, 0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 83).addBox(-4.5F, 0F, -4.5F, 9F, 9F, 9F),
            PartPose.offsetAndRotation(-6.5F, 15F, 1F, 0F, 0F, 0F));
        root.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(36, 83).addBox(0F, -8F, 0F, 1F, 14F, 10F),
            PartPose.offsetAndRotation(5F, -1F, 4F, 0.1F, 0.4F, 0.5F));
        root.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(58, 83).addBox(-1F, -8F, 0F, 1F, 14F, 10F),
            PartPose.offsetAndRotation(-5F, -1F, 4F, 0.1F, -0.4F, -0.5F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        float pos = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0F);
        this.leftLeg.xRot = Mth.cos(pos * 0.6662F) * 1.2F * speed;
        this.rightLeg.xRot = Mth.cos(pos * 0.6662F + (float) Math.PI) * 1.2F * speed;
        this.leftArm.xRot = -0.1F + Mth.cos(pos * 0.6662F + (float) Math.PI) * 0.9F * speed;
        this.rightArm.xRot = -0.1F + Mth.cos(pos * 0.6662F) * 0.9F * speed;
        float flap = Mth.sin(state.ageInTicks * 0.25F) * 0.15F;
        this.leftWing.zRot = 0.5F + flap;
        this.rightWing.zRot = -0.5F - flap;
        this.head.yRot = state.yRot * ((float) Math.PI / 180.0F);
        this.head.xRot = 0.15F + state.xRot * ((float) Math.PI / 180.0F);
    }
}
