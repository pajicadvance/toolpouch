package me.pajic.toolpouch.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.ToolPouchClient;
import me.pajic.toolpouch.compat.OhmegaCompat;
import me.pajic.toolpouch.hud.ContextualSelectionWidget;
import me.pajic.toolpouch.hud.MinimapOverlay;
import me.pajic.toolpouch.network.ModPayloads;
import me.pajic.toolpouch.util.ClientUtil;
import me.pajic.toolpouch.util.CompatFlags;
import me.pajic.toolpouch.util.GameplayUtil;
import me.pajic.toolpouch.util.ToolPouchUtil;
import me.pajic.toolpouch.compat.TrinketsCompat;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public class ModKeybinds {

    public static final KeyMapping.Category MOD_KEYS = new KeyMapping.Category(ToolPouch.id("keys"));
	private static boolean soundPlayed = false;
	private static int elytraBoostDelay = 0;

	public static final KeyMapping OPEN_TOOL_POUCH = new KeyMapping(
			"key.toolpouch.open_tool_pouch",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_H,
			MOD_KEYS
	);
    public static final KeyMapping USE_SPYGLASS = new KeyMapping(
			"key.toolpouch.use_spyglass",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_C,
			MOD_KEYS
	);
    public static final KeyMapping OPEN_WIDGET = new KeyMapping(
			"key.toolpouch.open_widget",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_X,
			MOD_KEYS
	);
    public static final KeyMapping OPEN_ENDER_CHEST = new KeyMapping(
			"key.toolpouch.open_ender_chest",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_V,
			MOD_KEYS
	);
	public static final KeyMapping TOGGLE_MINIMAP = new KeyMapping(
			"key.toolpouch.toggle_minimap",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_M,
			MOD_KEYS
	);

    public static void onClientTick(Minecraft client) {
		LocalPlayer player = client.player;
		if (player != null && client.level != null) {
			if (!CompatFlags.ZOOMIFY_LOADED) {
				if (USE_SPYGLASS.isDown() && ToolPouchUtil.toolPouchHasItem(player, stack -> stack.is(Items.SPYGLASS))) {
					if (!soundPlayed) {
						player.playSound(SoundEvents.SPYGLASS_USE);
						ToolPouch.xplat().sendToServer(new ModPayloads.C2SPlaySoundPayload(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.SPYGLASS_USE)));
						soundPlayed = true;
					}
					ClientUtil.shouldScope = true;
				} else {
					if (soundPlayed) {
						player.playSound(SoundEvents.SPYGLASS_STOP_USING);
						ToolPouch.xplat().sendToServer(new ModPayloads.C2SPlaySoundPayload(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.SPYGLASS_STOP_USING)));
						soundPlayed = false;
					}
					ClientUtil.shouldScope = false;
				}
			}
			boolean isShulkerWidget = !GameplayUtil.isHoldingProjectileWeapon(player);
			if (!ToolPouchClient.CONFIG.quickSelect.get()) {
				if (OPEN_WIDGET.consumeClick()) {
					if (!ContextualSelectionWidget.widgetOpen)
						ContextualSelectionWidget.widgetOpen = true;
					else {
						if (isShulkerWidget) {
							ToolPouch.xplat().sendToServer(new ModPayloads.C2SOpenShulkerBoxPayload(ScrollHandler.selectedShulkerSlot));
							player.playSound(SoundEvents.SHULKER_BOX_OPEN);
						}
						ContextualSelectionWidget.widgetOpen = false;
					}
				}
			} else if (!OPEN_WIDGET.isDown()) {
				if (ContextualSelectionWidget.widgetOpen) {
					if (isShulkerWidget) {
						ToolPouch.xplat().sendToServer(new ModPayloads.C2SOpenShulkerBoxPayload(ScrollHandler.selectedShulkerSlot));
						player.playSound(SoundEvents.SHULKER_BOX_OPEN);
					}
					ContextualSelectionWidget.widgetOpen = false;
				}
			}
			if (OPEN_ENDER_CHEST.consumeClick()) {
				if (ToolPouchUtil.toolPouchHasItem(player, stack -> stack.is(Items.ENDER_CHEST))) {
					player.playSound(SoundEvents.ENDER_CHEST_OPEN);
					ToolPouch.xplat().sendToServer(new ModPayloads.C2SOpenEnderContainerPayload());
				}
			}
			if (OPEN_TOOL_POUCH.consumeClick()) {
				ItemStack legsItem = ItemStack.EMPTY;
				// 2 - open from trinket/accessory API slot
				// 1 - open from leg slot
				// 0 - open from inventory
				int openMethod = -1;
				if (CompatFlags.TRINKETS_LOADED) legsItem = TrinketsCompat.tryGetTrinketToolPouch(player);
				if (CompatFlags.OHMEGA_LOADED) legsItem = OhmegaCompat.tryGetOhmegaToolPouch(player);
				if (!legsItem.isEmpty()) openMethod = 2;
				else {
					legsItem = player.getItemBySlot(EquipmentSlot.LEGS);
					if (GameplayUtil.isValidContainerHolder(legsItem)) openMethod = 1;
					else if (ToolPouch.CONFIG.canOpenFromInventory.get()) {
						if (player.getInventory().getNonEquipmentItems().stream().anyMatch(stack -> stack.is(GameplayUtil.TOOL_POUCHES))) {
							openMethod = 0;
						}
					}
				}
				if (openMethod != -1) {
					player.playSound(SoundEvents.BUNDLE_INSERT);
					ToolPouch.xplat().sendToServer(new ModPayloads.C2SOpenToolPouchPayload(openMethod));
				}
			}
			if (TOGGLE_MINIMAP.consumeClick()) {
				MinimapOverlay.minimapOn = !MinimapOverlay.minimapOn;
			}
			if (
					elytraBoostDelay == 0 && player.isFallFlying() && player.input.keyPresses.jump() &&
					ToolPouchUtil.toolPouchHasItem(player, stack -> stack.is(Items.FIREWORK_ROCKET))
			) {
				ToolPouch.xplat().sendToServer(new ModPayloads.C2SElytraBoostFromPouchPayload());
				elytraBoostDelay = 20;
			}
			if (elytraBoostDelay > 0) elytraBoostDelay--;
		}
    }

	public static void onClientStarted(Minecraft client) {
		Options options = client.options;
		if (options.keyLoadHotbarActivator.same(OPEN_WIDGET)) {
			options.keyLoadHotbarActivator.setKey(InputConstants.UNKNOWN);
		}
		if (options.keySaveHotbarActivator.same(USE_SPYGLASS)) {
			options.keySaveHotbarActivator.setKey(InputConstants.UNKNOWN);
		}
	}
}
