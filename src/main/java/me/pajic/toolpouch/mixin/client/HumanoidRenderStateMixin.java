package me.pajic.toolpouch.mixin.client;

import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import me.pajic.toolpouch.renderer.HumanoidRenderStateExtension;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
@Mixin(HumanoidRenderState.class)
public class HumanoidRenderStateMixin implements HumanoidRenderStateExtension {

	@Unique private ItemStack toolpouch$elytra = ItemStack.EMPTY;

	@Override
	public ItemStack toolpouch$getElytra() {
		return toolpouch$elytra;
	}

	@Override
	public void toolpouch$setElytra(ItemStack stack) {
		toolpouch$elytra = stack;
	}
}
