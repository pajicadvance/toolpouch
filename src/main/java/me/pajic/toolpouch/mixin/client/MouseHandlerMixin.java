package me.pajic.toolpouch.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import me.pajic.toolpouch.keybind.ScrollHandler;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @WrapOperation(
            method = "onScroll",
            at = @At(
                    value = "INVOKE",
					target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V"
            )
    )
    private void redirectScroll(Inventory instance, int selectedHotbarSlot, Operation<Void> original, @Local(name = "wheel") int wheel) {
        if (ScrollHandler.handleMouseScroll(instance, (int) Math.signum(wheel))) original.call(instance, selectedHotbarSlot);
    }
}
