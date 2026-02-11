package me.pajic.toolpouch.renderer;

import net.minecraft.world.item.ItemStack;

public interface HumanoidRenderStateExtension {
	ItemStack toolpouch$getElytra();
	void toolpouch$setElytra(ItemStack stack);
}
