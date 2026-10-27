package com.tvorozhoc.leftarm;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

/**
 * Обёртка над очередью рендера, делающая предметы полупрозрачными.
 * Всё, кроме предметов и моделей, просто передаётся дальше.
 */
public class TranslucentCollector implements SubmitNodeCollector {
	private final SubmitNodeCollector root;
	private final OrderedSubmitNodeCollector delegate;
	private final float opacity;

	public TranslucentCollector(SubmitNodeCollector root, float opacity) {
		this(root, root, opacity);
	}

	private TranslucentCollector(SubmitNodeCollector root, OrderedSubmitNodeCollector delegate, float opacity) {
		this.root = root;
		this.delegate = delegate;
		this.opacity = opacity;
	}

	@Override
	public OrderedSubmitNodeCollector order(int order) {
		return new TranslucentCollector(root, root.order(order), opacity);
	}

	private int withAlpha(int color) {
		return ARGB.color(Math.round(ARGB.alpha(color) * opacity), color);
	}

	private static RenderType translucent(RenderType original, @Nullable TextureAtlasSprite sprite) {
		return sprite != null ? RenderTypes.entityTranslucent(sprite.atlasLocation()) : original;
	}

	// ---- То, что меняем ----

	@Override
	public void submitItem(PoseStack poseStack, ItemDisplayContext context, int light, int overlay, int outlineColor,
						   int[] tintLayers, List<BakedQuad> quads, RenderType renderType, ItemStackRenderState.FoilType foilType) {
		float alpha = opacity;
		delegate.submitCustomGeometry(poseStack, renderType, (pose, consumer) -> {
			for (BakedQuad quad : quads) {
				int color = -1;
				if (quad.isTinted() && quad.tintIndex() < tintLayers.length) {
					color = tintLayers[quad.tintIndex()];
				}
				putQuad(consumer, pose, quad, color, alpha, light, overlay);
			}
		});
	}

	private static void putQuad(VertexConsumer consumer, PoseStack.Pose pose, BakedQuad quad, int color, float alpha, int light, int overlay) {
		consumer.putBulkData(pose, quad,
				ARGB.red(color) / 255f, ARGB.green(color) / 255f, ARGB.blue(color) / 255f,
				ARGB.alpha(color) / 255f * alpha, light, overlay);
	}

	@Override
	public <S> void submitModel(Model<? super S> model, S state, PoseStack poseStack, RenderType renderType, int light, int overlay,
								int tintColor, @Nullable TextureAtlasSprite sprite, int outlineColor,
								ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		delegate.submitModel(model, state, poseStack, translucent(renderType, sprite), light, overlay,
				withAlpha(tintColor), sprite, outlineColor, crumbling);
	}

	@Override
	public void submitModelPart(ModelPart part, PoseStack poseStack, RenderType renderType, int light, int overlay,
								@Nullable TextureAtlasSprite sprite, boolean sheeted, boolean hasFoil, int tintColor,
								ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling, int outlineColor) {
		delegate.submitModelPart(part, poseStack, translucent(renderType, sprite), light, overlay, sprite, sheeted,
				false, withAlpha(tintColor), crumbling, outlineColor);
	}

	// ---- Остальное без изменений ----

	@Override
	public void submitShadow(PoseStack poseStack, float radius, List<EntityRenderState.ShadowPiece> pieces) {
		delegate.submitShadow(poseStack, radius, pieces);
	}

	@Override
	public void submitNameTag(PoseStack poseStack, @Nullable Vec3 pos, int y, Component text, boolean seeThrough, int light,
							  double distance, CameraRenderState camera) {
		delegate.submitNameTag(poseStack, pos, y, text, seeThrough, light, distance, camera);
	}

	@Override
	public void submitText(PoseStack poseStack, float x, float y, FormattedCharSequence text, boolean shadow,
						   Font.DisplayMode mode, int light, int color, int background, int outline) {
		delegate.submitText(poseStack, x, y, text, shadow, mode, light, color, background, outline);
	}

	@Override
	public void submitFlame(PoseStack poseStack, EntityRenderState state, Quaternionf rotation) {
		delegate.submitFlame(poseStack, state, rotation);
	}

	@Override
	public void submitLeash(PoseStack poseStack, EntityRenderState.LeashState leash) {
		delegate.submitLeash(poseStack, leash);
	}

	@Override
	public void submitBlock(PoseStack poseStack, BlockState state, int light, int overlay, int outline) {
		delegate.submitBlock(poseStack, state, light, overlay, outline);
	}

	@Override
	public void submitMovingBlock(PoseStack poseStack, MovingBlockRenderState state) {
		delegate.submitMovingBlock(poseStack, state);
	}

	@Override
	public void submitBlockModel(PoseStack poseStack, RenderType renderType, BlockStateModel model, float r, float g, float b,
								 int light, int overlay, int outline) {
		delegate.submitBlockModel(poseStack, renderType, model, r, g, b, light, overlay, outline);
	}

	@Override
	public void submitCustomGeometry(PoseStack poseStack, RenderType renderType, CustomGeometryRenderer renderer) {
		delegate.submitCustomGeometry(poseStack, renderType, renderer);
	}

	@Override
	public void submitParticleGroup(ParticleGroupRenderer renderer) {
		delegate.submitParticleGroup(renderer);
	}

	@Override
	public void submitBlock(PoseStack poseStack, BlockState state, int light, int overlay, int outline,
							BlockAndTintGetter level, BlockPos pos) {
		delegate.submitBlock(poseStack, state, light, overlay, outline, level, pos);
	}

	@Override
	public void submitBlockStateModel(PoseStack poseStack, Function<ChunkSectionLayer, RenderType> renderTypes,
									  BlockStateModel model, float r, float g, float b, int light, int overlay, int outline,
									  BlockAndTintGetter level, BlockPos pos, BlockState state) {
		delegate.submitBlockStateModel(poseStack, renderTypes, model, r, g, b, light, overlay, outline, level, pos, state);
	}
}
