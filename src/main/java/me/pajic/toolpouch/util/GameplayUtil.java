package me.pajic.toolpouch.util;

import me.pajic.toolpouch.ToolPouch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.GameType;

import java.util.function.Predicate;

public class GameplayUtil {

	public static final TagKey<Item> TOOL_POUCHES = TagKey.create(Registries.ITEM, ToolPouch.id("tool_pouches"));

	public static boolean isValidContainerHolder(ItemStack stack) {
		return stack.is(TOOL_POUCHES) || isLeggingsWithPouchAttached(stack);
	}

	public static boolean isLeggingsWithPouchAttached(ItemStack stack) {
		return ToolPouch.CONFIG.canAttachToLeggings.get() &&
				stack.is(ItemTags.LEG_ARMOR) &&
				stack.has(DataComponents.CONTAINER);
	}

	public static boolean isHoldingProjectileWeapon(Player player) {
		return player.getMainHandItem().getItem() instanceof ProjectileWeaponItem ||
				player.getOffhandItem().getItem() instanceof ProjectileWeaponItem;
	}

	public static Predicate<ItemStack> getSupportedAmmo(Player player) {
		Item mainHand = player.getMainHandItem().getItem();
		Item offHand = player.getOffhandItem().getItem();
		if (mainHand instanceof ProjectileWeaponItem pwi) return pwi.getSupportedHeldProjectiles();
		if (offHand instanceof ProjectileWeaponItem pwi) return pwi.getSupportedHeldProjectiles();
		return _ -> false;
	}

	public static boolean canUnequipToolPouch(Player player, ItemStack backpack) {
		GameType mode = player.gameMode();
		if (mode != null && mode.isSurvival() && ToolPouch.CONFIG.preventUnequipWhenNotEmpty.get() && isValidContainerHolder(backpack)) {
			return backpack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY) == ItemContainerContents.EMPTY;
		}
		return true;
	}
}
