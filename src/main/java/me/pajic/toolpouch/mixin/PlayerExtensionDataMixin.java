package me.pajic.toolpouch.mixin;

import me.pajic.toolpouch.util.PlayerExtension;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerExtensionDataMixin implements PlayerExtension {

    @Unique private static final EntityDataAccessor<Boolean> TOOLPOUCH_ELYTRA_ENABLED =
            SynchedEntityData.defineId(Player.class, EntityDataSerializers.BOOLEAN);

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void defineElytraEnabled(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(TOOLPOUCH_ELYTRA_ENABLED, true);
    }

    @Override
    public boolean toolpouch$isElytraEnabled() {
        return ((Player) (Object) this).getEntityData().get(TOOLPOUCH_ELYTRA_ENABLED);
    }

    @Override
    public void toolpouch$setElytraEnabled(boolean enabled) {
        ((Player) (Object) this).getEntityData().set(TOOLPOUCH_ELYTRA_ENABLED, enabled);
    }

    @Unique private int toolpouch$shulkerSlot;
    @Unique private int toolpouch$arrowSlot;

    @Override
    public int toolpouch$getShulkerSlot() {
        return toolpouch$shulkerSlot;
    }

    @Override
    public void toolpouch$setShulkerSlot(int value) {
        toolpouch$shulkerSlot = value;
    }

    @Override
    public int toolpouch$getArrowSlot() {
        return toolpouch$arrowSlot;
    }

    @Override
    public void toolpouch$setArrowSlot(int value) {
        toolpouch$arrowSlot = value;
    }

    @Inject(
            method = "addAdditionalSaveData",
            at = @At("TAIL")
    )
    private void addArrowSlot(ValueOutput output, CallbackInfo ci) {
        output.putBoolean("ToolPouchElytraEnabled", toolpouch$isElytraEnabled());
        output.putInt("ShulkerSlot", toolpouch$shulkerSlot);
        output.putInt("ArrowSlot", toolpouch$arrowSlot);
    }

    @Inject(
            method = "readAdditionalSaveData",
            at = @At("TAIL")
    )
    private void readArrowSlot(ValueInput input, CallbackInfo ci) {
        toolpouch$setElytraEnabled(input.getBooleanOr("ToolPouchElytraEnabled", true));
        toolpouch$shulkerSlot = input.getIntOr("ShulkerSlot", 0);
        toolpouch$arrowSlot = input.getIntOr("ArrowSlot", 0);
    }
}
