package me.pajic.toolpouch.mixin.compat;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.lodestar.aileron.Aileron;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@IfModLoaded("aileron")
@Mixin(Aileron.class)
public class AileronMixin {

    @ModifyReturnValue(
            method = "getElytra",
            at = @At("RETURN")
    )
    private static ItemStack useToolPouchElytra(
			ItemStack original,
			@Local(argsOnly = true, name = "entity") LivingEntity entity
	) {
		if (entity instanceof Player player) {
			ItemStack elytra = ToolPouchUtil.getElytraFromToolPouch(player, false);
			if (!elytra.isEmpty()) return elytra;
		}
        return original;
    }
}
