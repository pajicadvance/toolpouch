package me.pajic.toolpouch.mixin;

import me.pajic.toolpouch.util.PlayerExtension;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void copyElytraPreference(ServerPlayer oldPlayer, boolean keepEverything, CallbackInfo ci) {
        ((PlayerExtension) this).toolpouch$setElytraEnabled(((PlayerExtension) oldPlayer).toolpouch$isElytraEnabled());
    }
}
