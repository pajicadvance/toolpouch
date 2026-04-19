package me.pajic.toolpouch.item;

import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.compat.OhmegaCompat;
import me.pajic.toolpouch.util.CompatFlags;
import me.pajic.toolpouch.compat.TrinketsCompat;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemContainerContents;

public class ModItems {

	public static final Item TOOL_POUCH = makeToolPouch(new Item.Properties()
			.stacksTo(1)
			.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
			.setId(ResourceKey.create(Registries.ITEM, ToolPouch.id("tool_pouch"))));

	public static final Item NETHERITE_TOOL_POUCH = makeToolPouch(new Item.Properties()
			.stacksTo(1)
			.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
			.fireResistant()
			.setId(ResourceKey.create(Registries.ITEM, ToolPouch.id("netherite_tool_pouch"))));

	private static ToolPouchItem makeToolPouch(Item.Properties properties) {
		if (CompatFlags.TRINKETS_LOADED) return TrinketsCompat.makeTrinketToolPouch(properties);
		if (CompatFlags.OHMEGA_LOADED) return OhmegaCompat.makeOhmegaToolPouch(properties);
		return new ToolPouchItem(properties);
	}

	public static void init() {}
}
