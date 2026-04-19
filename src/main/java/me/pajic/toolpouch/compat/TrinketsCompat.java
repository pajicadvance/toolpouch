package me.pajic.toolpouch.compat;

import eu.pb4.trinkets.api.TrinketsApi;
import me.pajic.toolpouch.item.ToolPouchItem;
import me.pajic.toolpouch.item.ToolPouchTrinketItem;
import me.pajic.toolpouch.util.GameplayUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class TrinketsCompat {

	public static ItemStack tryGetTrinketToolPouch(LivingEntity entity) {
		var list = TrinketsApi.getAttachment(entity).getEquipped(stack -> stack.is(GameplayUtil.TOOL_POUCHES));
		if (!list.isEmpty()) return list.getFirst().getB();
		return ItemStack.EMPTY;
	}

	public static ToolPouchItem makeTrinketToolPouch(Item.Properties properties) {
		return new ToolPouchTrinketItem(properties);
	}
}
