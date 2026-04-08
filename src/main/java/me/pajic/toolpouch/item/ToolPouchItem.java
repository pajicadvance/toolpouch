package me.pajic.toolpouch.item;

import me.pajic.toolpouch.ToolPouch;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ToolPouchItem extends Item {

	public ToolPouchItem(Properties properties) {
		super(properties);
	}

	@Override
	public @NotNull InteractionResult use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
		return ToolPouch.CONFIG.canOpenWithRightClick.get() ?
				ToolPouch.xplat().openToolPouchScreen(player, player.getItemInHand(hand)) :
				super.use(level, player, hand);
	}
}
