package me.pajic.toolpouch;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.api.RegisterType;
import me.pajic.toolpouch.config.ModClientConfig;
import net.minecraft.resources.Identifier;

public class ToolPouchClient {

	public static final Identifier CONFIG_RL = ToolPouch.id("client_config");
	public static ModClientConfig CONFIG = ConfigApiJava.registerAndLoadConfig(ModClientConfig::new, RegisterType.CLIENT);

	public static void init() {}
}
