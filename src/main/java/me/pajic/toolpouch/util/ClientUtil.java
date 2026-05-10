package me.pajic.toolpouch.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;

import java.util.function.Predicate;

public class ClientUtil {

	public static boolean shouldScope = false;
	public static float zoomModifier = 1.0F;

	public static void onClientLevelChange() {
		ToolPouchUtil.ITEM_SUGGESTIONS.clear();
		ToolPouchUtil.ITEM_SUGGESTIONS.addAll(BuiltInRegistries.ITEM.keySet().stream().map(Identifier::toString).toList());
		BuiltInRegistries.ITEM.listTagIds().forEach(tag -> ToolPouchUtil.ITEM_SUGGESTIONS.add("#" + tag.location()));
	}

	public static Predicate<ItemStack> getSupportedLanterns() {
		return stack -> stack.is(ItemTags.LANTERNS) || Block.byItem(stack.getItem()) instanceof LanternBlock;
	}
}
