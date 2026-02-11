package me.pajic.toolpouch.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class PlayerLanternLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

	public PlayerLanternLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
		super(renderer);
	}

	@Override
	public void submit(@NonNull PoseStack poseStack, @NonNull SubmitNodeCollector nodeCollector, int packedLight, AvatarRenderState renderState, float yRot, float xRot) {
		Vec3 offset = !renderState.chestEquipment.isEmpty() || !renderState.legsEquipment.isEmpty() ?
				new Vec3(-0.05F, 0.75F, 0F) : new Vec3(-0.05F, 0.75F, 0.05F);
		getParentModel().body.translateAndRotate(poseStack);
		poseStack.translate(offset);
		poseStack.scale(0.5F, 0.5F, 0.5F);
		poseStack.mulPose(Axis.XP.rotationDegrees(180));
		nodeCollector.submitBlock(
				poseStack,
				Block.byItem(((AvatarRenderStateExtension) renderState).toolpouch$getLantern().getItem()).defaultBlockState(),
				renderState.lightCoords,
				OverlayTexture.NO_OVERLAY,
				renderState.outlineColor
		);
	}
}
