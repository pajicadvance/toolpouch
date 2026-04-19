package me.pajic.toolpouch.util;

import me.pajic.toolpouch.ToolPouch;

public class CompatFlags {
	public static final boolean IMMERSIVE_OVERLAYS_LOADED = ToolPouch.xplat().isModLoaded("immersiveoverlays");
	public static final boolean RAISED_LOADED = ToolPouch.xplat().isModLoaded("raised");
	public static final boolean SBT_LOADED = ToolPouch.xplat().isModLoaded("shulkerboxtooltip");
	public static final boolean TRINKETS_LOADED = ToolPouch.xplat().isModLoaded("trinkets_updated");
	public static final boolean OHMEGA_LOADED = ToolPouch.xplat().isModLoaded("ohmega");
}
