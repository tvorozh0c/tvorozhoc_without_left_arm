package com.tvorozhoc.leftarm;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Простые розовые ушки на голове у всех игроков. */
public class PinkEarsLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
	private static final Identifier TEXTURE =
			Identifier.fromNamespaceAndPath(LeftArmClient.MOD_ID, "textures/entity/ears.png");

	private final ModelPart ears = createEars();

	public PinkEarsLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
		super(parent);
	}

	private static ModelPart createEars() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		// Ухо = основание 3x3x1 + кончик 1x1x1, слегка наклонено наружу. Голова занимает y от -8 до 0.
		CubeListBuilder ear = CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1.5f, -3f, -0.5f, 3, 3, 1)
				.texOffs(0, 8).addBox(-0.5f, -4f, -0.5f, 1, 1, 1);
		root.addOrReplaceChild("left_ear", ear, PartPose.offsetAndRotation(-2.5f, -8f, 0f, 0f, 0f, -0.3f));
		root.addOrReplaceChild("right_ear", ear, PartPose.offsetAndRotation(2.5f, -8f, 0f, 0f, 0f, 0.3f));
		return LayerDefinition.create(mesh, 16, 16).bakeRoot();
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state,
					   float yRot, float xRot) {
		if (!LeftArmConfig.get().pinkEars || state.isInvisible) {
			return;
		}
		poseStack.pushPose();
		getParentModel().root().translateAndRotate(poseStack);
		getParentModel().head.translateAndRotate(poseStack);
		collector.submitModelPart(ears, poseStack, RenderTypes.entityCutoutNoCull(TEXTURE), light,
				LivingEntityRenderer.getOverlayCoords(state, 0f), null);
		poseStack.popPose();
	}
}
