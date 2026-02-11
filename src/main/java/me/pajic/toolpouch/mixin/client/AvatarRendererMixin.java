package me.pajic.toolpouch.mixin.client;

import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import me.pajic.toolpouch.renderer.AvatarRenderStateExtension;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {

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
			((AvatarRenderStateExtension) avatarRenderState).toolpouch$setLantern(
					lanterns.isEmpty() ? ItemStack.EMPTY : lanterns.getFirst()
			);
		}
	}
}
