package me.pajic.toolpouch.renderer;

import net.minecraft.world.item.ItemStack;

public interface AvatarRenderStateExtension {
	ItemStack toolpouch$getLantern();
	void toolpouch$setLantern(ItemStack stack);
}
