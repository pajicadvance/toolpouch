package me.pajic.toolpouch.keybind;

import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.hud.ContextualSelectionWidget;
import me.pajic.toolpouch.network.ModPayloads;
import me.pajic.toolpouch.util.ClientUtil;
import me.pajic.toolpouch.util.GameplayUtil;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ScrollHandler {

    public static int selectedShulkerSlot = 0;
    public static int selectedArrowSlot = 0;

    public static boolean handleMouseScroll(Inventory inventory, int direction) {
        Player player = inventory.player;
        if (ClientUtil.shouldScope) {
            if (direction != 0) {
                ClientUtil.zoomModifier -= direction * (0.1F * ClientUtil.zoomModifier);
                if (ClientUtil.zoomModifier > 10) ClientUtil.zoomModifier = 10;
                else if (ClientUtil.zoomModifier < 0.1) ClientUtil.zoomModifier = 0.1F;
                else player.playSound(SoundEvents.SPYGLASS_STOP_USING);
            }
            return false;
        } else if (GameplayUtil.isHoldingProjectileWeapon(player) && ContextualSelectionWidget.widgetOpen) {
			List<ItemStack> arrows = ToolPouchUtil.getItemsFromToolPouch(player, GameplayUtil.getSupportedAmmo(player));
			if (!arrows.isEmpty()) {
				int size = arrows.size();
				do {
					selectedArrowSlot -= direction;
					if (selectedArrowSlot < 0) selectedArrowSlot = size - 1;
					if (selectedArrowSlot >= size) selectedArrowSlot = 0;
				} while (arrows.get(selectedArrowSlot).isEmpty());
				ToolPouch.xplat().sendToServer(new ModPayloads.C2SSyncArrowSlot(selectedArrowSlot));
				return false;
			}
        } else if (ContextualSelectionWidget.widgetOpen) {
			List<ItemStack> shulkers = ToolPouchUtil.getItemsFromToolPouch(player, stack -> stack.is(ItemTags.SHULKER_BOXES));
			if (!shulkers.isEmpty()) {
				int size = shulkers.size();
				do {
					selectedShulkerSlot -= direction;
					if (selectedShulkerSlot < 0) selectedShulkerSlot = size - 1;
					if (selectedShulkerSlot >= size) selectedShulkerSlot = 0;
				} while (shulkers.get(selectedShulkerSlot).isEmpty());
				ToolPouch.xplat().sendToServer(new ModPayloads.C2SSyncShulkerSlot(selectedShulkerSlot));
				return false;
			}
        }
        return true;
    }
}
