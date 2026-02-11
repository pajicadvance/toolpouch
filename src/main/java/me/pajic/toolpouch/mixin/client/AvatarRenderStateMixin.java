package me.pajic.toolpouch.mixin.client;

import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import me.pajic.toolpouch.renderer.AvatarRenderStateExtension;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements AvatarRenderStateExtension {

	@Unique private ItemStack toolpouch$lantern = ItemStack.EMPTY;

	@Override
	public ItemStack toolpouch$getLantern() {
		return toolpouch$lantern;
	}

	@Override
	public void toolpouch$setLantern(ItemStack stack) {
		toolpouch$lantern = stack;
	}
}
