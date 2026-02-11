package me.pajic.toolpouch.item;

import me.pajic.toolpouch.ToolPouch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.component.ItemContainerContents;

public class ToolPouchItem extends BaseToolPouchItem {

	public ToolPouchItem() {
		super(new Properties()
				.stacksTo(1)
				.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
				.setId(ResourceKey.create(Registries.ITEM, ToolPouch.id("tool_pouch")))
		);
	}
}
