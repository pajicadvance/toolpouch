package me.pajic.toolpouch.mixin.client;

import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import me.pajic.toolpouch.renderer.AvatarRenderStateExtension;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin<AvatarlikeEntity extends Avatar & ClientAvatarEntity> extends LivingEntityRenderer<AvatarlikeEntity, AvatarRenderState, PlayerModel> {

	@Unique private static BlockModelResolver blockModelResolver;

	public AvatarRendererMixin(EntityRendererProvider.Context context, PlayerModel model, float shadow) {
		super(context, model, shadow);
	}

	@Inject(
			method = "<init>",
			at = @At("TAIL")
	)
	private static void onInit(EntityRendererProvider.Context context, boolean slimSteve, CallbackInfo ci) {
		blockModelResolver = context.getBlockModelResolver();
	}

	@Inject(
			method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
			at = @At("HEAD")
	)
	private <AvatarlikeEntity extends Avatar & ClientAvatarEntity> void extendRenderState(
			AvatarlikeEntity avatar, AvatarRenderState avatarRenderState, float f, CallbackInfo ci
	) {
		if (avatar instanceof Player player) {
			List<ItemStack> lanterns = ToolPouchUtil.getItemsFromToolPouch(
					player, stack -> stack.is(ItemTags.LANTERNS)
			);
			ItemStack lantern = lanterns.isEmpty() ? ItemStack.EMPTY : lanterns.getFirst();
			blockModelResolver.update(
					((AvatarRenderStateExtension) avatarRenderState).toolpouch$getLantern(),
					Block.byItem(lantern.getItem()).defaultBlockState(),
					BlockDisplayContext.create()
			);
		}
	}
}
