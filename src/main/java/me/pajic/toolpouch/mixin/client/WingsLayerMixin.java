package me.pajic.toolpouch.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import me.pajic.toolpouch.renderer.HumanoidRenderStateExtension;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
@Mixin(WingsLayer.class)
public class WingsLayerMixin {

	@ModifyExpressionValue(
			method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;chestEquipment:Lnet/minecraft/world/item/ItemStack;",
					opcode = Opcodes.GETFIELD
			)
	)
	private ItemStack renderToolPouchElytra(ItemStack original, @Local(argsOnly = true) HumanoidRenderState renderState) {
		ItemStack toolPouchElytra = ((HumanoidRenderStateExtension) renderState).toolpouch$getElytra();
		if (!toolPouchElytra.isEmpty()) return toolPouchElytra;
		return original;
	}
}
