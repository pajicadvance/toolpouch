package me.pajic.toolpouch.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.component.ModDataComponents;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.equipment.Equippable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;

@Mixin(ItemContainerContents.class)
public class ItemContainerContentsMixin {

	@WrapMethod(method = "addToTooltip")
	private void addAttachedToolPouchToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag flag, DataComponentGetter componentGetter, Operation<Void> original) {
		if (ToolPouch.CONFIG.canAttachToLeggings.get()) {
			Equippable equippable = componentGetter.get(DataComponents.EQUIPPABLE);
			DyedItemColor storedDye = componentGetter.get(ModDataComponents.STORED_TOOL_POUCH_DYE);
			if (equippable != null && equippable.slot() == EquipmentSlot.LEGS) {
				tooltipAdder.accept(
					Component.translatable("text.toolpouch.attachment").withColor(
							storedDye != null ? storedDye.rgb() : -6265536
					)
				);
			}
		}
		original.call(context, tooltipAdder, flag, componentGetter);
	}
}
