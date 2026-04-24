package me.pajic.toolpouch.mixin.compat;

//? fabric {

import com.craftycorvid.improvedmaps.ImprovedMapsLifecycleEvents;
import com.craftycorvid.improvedmaps.item.ImprovedMapsItems;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@IfModLoaded("improved-maps")
@Mixin(ImprovedMapsLifecycleEvents.class)
public class ImprovedMapsLifecycleEventsMixin {

	@Shadow
	public static void AtlasPlayerHandTick(ServerPlayer player, ItemStack atlas, EquipmentSlot slot) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	@Inject(
			method = "ImprovedMapsServerTick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/level/ServerPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;",
					ordinal = 0
			)
	)
	private static void tickPouchAtlases(MinecraftServer server, CallbackInfo ci, @Local(name = "player") ServerPlayer player) {
		List<ItemStack> atlases = ToolPouchUtil.getItemsFromToolPouch(player, stack -> stack.is(ImprovedMapsItems.ATLAS));
		for (ItemStack atlas : atlases) AtlasPlayerHandTick(player, atlas, EquipmentSlot.MAINHAND);
	}

	@Inject(
			method = "AtlasPlayerHandTick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/MapItem;getSavedData(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;"
			)
	)
	private static void checkSameAtlas(
			ServerPlayer player,
			ItemStack atlas,
			EquipmentSlot slot,
			CallbackInfo ci,
			@Share("same") LocalBooleanRef same,
			@Share("pouchAtlas") LocalRef<ItemStack> pouchAtlas
	) {
		Optional<ItemStack> opt = ToolPouchUtil.getItemsFromToolPouch(player, stack -> stack.is(ImprovedMapsItems.ATLAS)).stream().findFirst();
		if (opt.isPresent()) {
			ItemStack stack = opt.get();
			if (ItemStack.isSameItemSameComponents(stack, atlas)) {
				same.set(true);
				pouchAtlas.set(stack);
				return;
			}
		}
		same.set(false);
		pouchAtlas.set(ItemStack.EMPTY);
	}

	@Inject(
			method = "AtlasPlayerHandTick",
			at = @At("TAIL")
	)
	private static void replacePouchAtlasOnMapChange(
			ServerPlayer player,
			ItemStack atlas,
			EquipmentSlot slot,
			CallbackInfo ci,
			@Share("same") LocalBooleanRef same,
			@Share("pouchAtlas") LocalRef<ItemStack> pouchAtlas
	) {
		if (same.get() && !ItemStack.isSameItemSameComponents(pouchAtlas.get(), atlas)) {
			ToolPouchUtil.replaceItemInToolPouch(player, atlas, stack -> stack.is(ImprovedMapsItems.ATLAS), 0);
		}
	}
}
//?}
