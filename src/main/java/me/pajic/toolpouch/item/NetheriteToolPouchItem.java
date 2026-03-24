package me.pajic.toolpouch.item;

import me.pajic.toolpouch.ToolPouch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.component.ItemContainerContents;

public class NetheriteToolPouchItem extends BaseToolPouchItem {

	public NetheriteToolPouchItem() {
		super(new Properties()
				.stacksTo(1)
				.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
				.fireResistant()
				.setId(ResourceKey.create(Registries.ITEM, ToolPouch.id("netherite_tool_pouch")))
		);
	}
}
