package me.pajic.toolpouch.platform;

import me.pajic.toolpouch.menu.ToolPouchMenu;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public interface Platform {

	boolean isModLoaded(String modId);

	boolean isDevelopmentEnvironment();

	default boolean isDebug() {
		return isDevelopmentEnvironment();
	}

	void sendToServer(CustomPacketPayload payload);

	void sendToClient(ServerPlayer player, CustomPacketPayload payload);

	MenuType<ToolPouchMenu> constructMenu();

	InteractionResult openToolPouchScreen(Player player, ItemStack backpack);

	String getTagTranslationKey(TagKey<?> tagKey);
}
