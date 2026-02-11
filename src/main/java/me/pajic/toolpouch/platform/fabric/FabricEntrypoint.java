package me.pajic.toolpouch.platform.fabric;

//? fabric {

import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.component.ModDataComponents;
import me.pajic.toolpouch.item.ModItems;
import me.pajic.toolpouch.menu.ModMenuTypes;
import me.pajic.toolpouch.network.ModPayloads;
import me.pajic.toolpouch.network.NetworkEvents;
import me.pajic.toolpouch.recipe.ModRecipes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;

@SuppressWarnings("unused")
public class FabricEntrypoint implements ModInitializer {

	@Override
	public void onInitialize() {
		initRegistry();
		initCauldronInteraction();
		initCreativeTabs();
		initNetworking();
	}

	private static void initRegistry() {
		ModDataComponents.init();
		Registry.register(
				BuiltInRegistries.DATA_COMPONENT_TYPE,
				ToolPouch.id("stored_dye"),
				ModDataComponents.STORED_TOOL_POUCH_DYE
		);
		Registry.register(
				BuiltInRegistries.DATA_COMPONENT_TYPE,
				ToolPouch.id("is_netherite_pouch"),
				ModDataComponents.IS_NETHERITE_POUCH
		);
		ModMenuTypes.init();
		Registry.register(
				BuiltInRegistries.MENU,
				ToolPouch.id("tool_pouch_menu"),
				ModMenuTypes.TOOL_POUCH_MENU
		);
		ModItems.init();
		Registry.register(
				BuiltInRegistries.ITEM,
				ToolPouch.id("tool_pouch"),
				ModItems.TOOL_POUCH
		);
		Registry.register(
				BuiltInRegistries.ITEM,
				ToolPouch.id("netherite_tool_pouch"),
				ModItems.NETHERITE_TOOL_POUCH
		);
		Registry.register(
				BuiltInRegistries.RECIPE_SERIALIZER,
				ToolPouch.id("crafting_special_attach_tool_pouch"),
				ModRecipes.ATTACH_TOOL_POUCH
		);
		Registry.register(
				BuiltInRegistries.RECIPE_SERIALIZER,
				ToolPouch.id("crafting_special_detach_tool_pouch"),
				ModRecipes.DETACH_TOOL_POUCH
		);
	}

	private static void initCauldronInteraction() {
		CauldronInteraction.WATER.map().put(ModItems.TOOL_POUCH, CauldronInteraction::dyedItemIteration);
		CauldronInteraction.WATER.map().put(ModItems.NETHERITE_TOOL_POUCH, CauldronInteraction::dyedItemIteration);
	}

	private static void initCreativeTabs() {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries ->
				entries.addBefore(Items.COMPASS, ModItems.TOOL_POUCH, ModItems.NETHERITE_TOOL_POUCH)
		);
	}

	private static void initNetworking() {
		ModPayloads.init();
		PayloadTypeRegistry.playC2S().register(ModPayloads.C2SOpenToolPouchPayload.TYPE, ModPayloads.C2SOpenToolPouchPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ModPayloads.C2SOpenShulkerBoxPayload.TYPE, ModPayloads.C2SOpenShulkerBoxPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ModPayloads.C2SOpenEnderContainerPayload.TYPE, ModPayloads.C2SOpenEnderContainerPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ModPayloads.C2SSyncShulkerSlot.TYPE, ModPayloads.C2SSyncShulkerSlot.CODEC);
		PayloadTypeRegistry.playS2C().register(ModPayloads.S2CSyncShulkerSlot.TYPE, ModPayloads.S2CSyncShulkerSlot.CODEC);
		PayloadTypeRegistry.playC2S().register(ModPayloads.C2SSyncArrowSlot.TYPE, ModPayloads.C2SSyncArrowSlot.CODEC);
		PayloadTypeRegistry.playS2C().register(ModPayloads.S2CSyncArrowSlot.TYPE, ModPayloads.S2CSyncArrowSlot.CODEC);
		PayloadTypeRegistry.playC2S().register(ModPayloads.C2SElytraBoostFromPouchPayload.TYPE, ModPayloads.C2SElytraBoostFromPouchPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(
				ModPayloads.C2SOpenToolPouchPayload.TYPE,
				(payload, context) -> NetworkEvents.tryOpenToolPouch(context.player(), payload.fromLeggingsSlot())
		);
		ServerPlayNetworking.registerGlobalReceiver(
				ModPayloads.C2SOpenShulkerBoxPayload.TYPE,
				(payload, context) -> NetworkEvents.openShulkerBox(context.player(), payload.index())
		);
		ServerPlayNetworking.registerGlobalReceiver(
				ModPayloads.C2SOpenEnderContainerPayload.TYPE,
				(payload, context) -> NetworkEvents.openEnderContainer(context.player())
		);
		ServerPlayNetworking.registerGlobalReceiver(
				ModPayloads.C2SSyncShulkerSlot.TYPE,
				(payload, context) -> NetworkEvents.syncShulkerSlotToServer(context.player(), payload.slot())
		);
		ServerPlayNetworking.registerGlobalReceiver(
				ModPayloads.C2SSyncArrowSlot.TYPE,
				(payload, context) -> NetworkEvents.syncArrowSlotToServer(context.player(), payload.slot())
		);
		ServerPlayNetworking.registerGlobalReceiver(
				ModPayloads.C2SElytraBoostFromPouchPayload.TYPE,
				(payload, context) -> NetworkEvents.elytraBoostFromPouch(context.player())
		);
	}
}
//?}
