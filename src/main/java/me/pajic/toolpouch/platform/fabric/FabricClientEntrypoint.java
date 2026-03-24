package me.pajic.toolpouch.platform.fabric;

//? fabric {

import me.pajic.toolpouch.ToolPouch;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import me.pajic.toolpouch.hud.ContextualSelectionWidget;
import me.pajic.toolpouch.hud.InfoOverlays;
import me.pajic.toolpouch.hud.MinimapOverlay;
import me.pajic.toolpouch.keybind.ModKeybinds;
import me.pajic.toolpouch.menu.ModMenuTypes;
import me.pajic.toolpouch.menu.ToolPouchScreen;
import me.pajic.toolpouch.network.ModPayloads;
import me.pajic.toolpouch.network.NetworkClientEvents;
import me.pajic.toolpouch.renderer.PlayerLanternLayer;
import me.pajic.toolpouch.util.ClientUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;

@Entrypoint("client")
public class FabricClientEntrypoint implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ToolPouch.onInitializeClient();
		initToolPouchMenuScreen();
		initKeybinds();
		initOnClientTick();
		initOnClientStart();
		initOnClientWorldChange();
		initNetworking();
		initHudLayers();
		initPlayerLanternRenderer();
	}

	private static void initToolPouchMenuScreen() {
		MenuScreens.register(ModMenuTypes.TOOL_POUCH_MENU, ToolPouchScreen::new);
	}

	private static void initKeybinds() {
		KeyMapping.Category.register(ToolPouch.id("keys"));
		KeyMappingHelper.registerKeyMapping(ModKeybinds.OPEN_TOOL_POUCH);
		KeyMappingHelper.registerKeyMapping(ModKeybinds.OPEN_WIDGET);
		KeyMappingHelper.registerKeyMapping(ModKeybinds.OPEN_ENDER_CHEST);
		KeyMappingHelper.registerKeyMapping(ModKeybinds.USE_SPYGLASS);
		KeyMappingHelper.registerKeyMapping(ModKeybinds.TOGGLE_MINIMAP);
	}

	private static void initOnClientTick() {
		ClientTickEvents.END_CLIENT_TICK.register(ModKeybinds::onClientTick);
	}

	private static void initOnClientStart() {
		ClientLifecycleEvents.CLIENT_STARTED.register(ModKeybinds::onClientStarted);
	}

	private static void initOnClientWorldChange() {
		ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((client, world) -> ClientUtil.onClientLevelChange());
	}

	private static void initNetworking() {
		ClientPlayNetworking.registerGlobalReceiver(
				ModPayloads.S2CSyncShulkerSlot.TYPE,
				(payload, context) -> NetworkClientEvents.syncShulkerSlotToClient(payload.slot())
		);
		ClientPlayNetworking.registerGlobalReceiver(
				ModPayloads.S2CSyncArrowSlot.TYPE,
				(payload, context) -> NetworkClientEvents.syncArrowSlotToClient(payload.slot())
		);
	}

	private static void initHudLayers() {
		HudElementRegistry.addLast(
				ToolPouch.id("info_overlay"),
				(context, tickCounter) -> InfoOverlays.render(context)
		);
		HudElementRegistry.addLast(
				ToolPouch.id("contextual_widget"),
				(context, tickCounter) -> ContextualSelectionWidget.render(context)
		);
		HudElementRegistry.addLast(
				ToolPouch.id("minimap_overlay"),
				(context, tickCounter) -> MinimapOverlay.render(context)
		);
	}

	private static void initPlayerLanternRenderer() {
		LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
			if (entityRenderer instanceof AvatarRenderer<?> avatarRenderer) {
				registrationHelper.register(new PlayerLanternLayer(avatarRenderer));
			}
		});
	}
}
//?}
