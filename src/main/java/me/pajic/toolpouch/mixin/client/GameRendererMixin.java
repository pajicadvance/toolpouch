package me.pajic.toolpouch.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import me.pajic.toolpouch.ToolPouchClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow @Final private Minecraft minecraft;
    @Shadow private float fovModifier;

    @ModifyExpressionValue(
            method = "tickFov",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;clamp(FFF)F"
            )
    )
    private float uncapSpyglassZoomLevel(float original) {
        if (ToolPouchClient.CONFIG.scrollableZoom.get() && minecraft.player != null && minecraft.player.isScoping()) {
            return fovModifier;
        }
        return original;
    }
}
