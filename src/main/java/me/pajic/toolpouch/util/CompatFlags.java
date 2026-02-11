package me.pajic.toolpouch.util;

import me.pajic.toolpouch.ToolPouch;

public class CompatFlags {
	public static final boolean IMMERSIVE_OVERLAYS_LOADED = ToolPouch.xplat().isModLoaded("immersiveoverlays");
	public static final boolean RAISED_LOADED = ToolPouch.xplat().isModLoaded("raised");
}
