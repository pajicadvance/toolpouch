package me.pajic.toolpouch.menu;

import me.pajic.toolpouch.ToolPouch;
import net.minecraft.world.inventory.MenuType;

public class ModMenuTypes {

	public static final MenuType<ToolPouchMenu> TOOL_POUCH_MENU = ToolPouch.xplat().constructMenu();

	public static void init() {}
}
