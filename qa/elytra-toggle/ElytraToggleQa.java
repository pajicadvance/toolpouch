package qa;

import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.network.NetworkEvents;
import me.pajic.toolpouch.util.PlayerExtension;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

/** Exercises production mixins in a dedicated server, without claiming GUI or packet coverage. */
public final class ElytraToggleQa implements ModInitializer {
    private final List<String> evidence = new ArrayList<>();
    private int checks, drain;
    private boolean finished;

    private void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
        checks++;
    }

    private ServerPlayer player(ServerLevel level) {
        var profile = new GameProfile(UUID.randomUUID(), "ElytraToggleQA");
        var player = new ServerPlayer(level.getServer(), level, profile, ClientInformation.createDefault());
        player.connection = new ServerGamePacketListenerImpl(level.getServer(),
                new Connection(PacketFlow.SERVERBOUND), player, CommonListenerCookie.createInitial(profile, false)) {
            @Override public void send(Packet<?> packet) {}
        };
        player.setPos(0, 100, 0);
        player.setOnGround(false);
        return player;
    }

    private PlayerExtension state(ServerPlayer player) { return (PlayerExtension) player; }

    private boolean canGlide(ServerPlayer player) throws Exception {
        Method method = LivingEntity.class.getDeclaredMethod("canGlide");
        method.setAccessible(true);
        return (boolean) method.invoke(player);
    }

    private void flightWearTick(ServerPlayer player) throws Exception {
        Field ticks = LivingEntity.class.getDeclaredField("fallFlyTicks");
        ticks.setAccessible(true);
        ticks.setInt(player, 19);
        Method method = LivingEntity.class.getDeclaredMethod("updateFallFlying");
        method.setAccessible(true);
        method.invoke(player);
    }

    private ItemStack pouch(ServerPlayer player, boolean leggings, boolean broken) {
        var holder = new ItemStack(leggings ? Items.IRON_LEGGINGS : me.pajic.toolpouch.item.ModItems.TOOL_POUCH);
        var contents = NonNullList.withSize(16, ItemStack.EMPTY);
        var elytra = new ItemStack(Items.ELYTRA);
        if (broken) elytra.setDamageValue(elytra.getMaxDamage() - 1);
        contents.set(3, elytra);
        contents.set(9, new ItemStack(Items.CLOCK));
        holder.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contents));
        if (leggings) player.setItemSlot(EquipmentSlot.LEGS, holder);
        else player.getInventory().setItem(12, holder);
        return holder;
    }

    private int damage(ItemStack holder) {
        return holder.get(DataComponents.CONTAINER).itemCopies().toList().get(3).getDamageValue();
    }

    private void flight(ServerLevel level, boolean leggings) throws Exception {
        var player = player(level);
        var holder = pouch(player, leggings, false);
        check(state(player).toolpouch$isElytraEnabled(), "new player defaults enabled");
        check(canGlide(player), "enabled pouch grants actual canGlide");
        player.setOnGround(true);
        check(!canGlide(player), "enabled pouch respects ground restriction");
        player.setOnGround(false);
        flightWearTick(player);
        check(damage(holder) == 1, "enabled pouch receives flight wear");
        var snapshot = holder.get(DataComponents.CONTAINER);
        NetworkEvents.toggleElytra(player);
        check(!state(player).toolpouch$isElytraEnabled(), "server toggle disables");
        check(!canGlide(player), "disabled pouch denies actual canGlide");
        check(ToolPouchUtil.getElytraFromToolPouch(player, false) == null, "disabled functional lookup excludes elytra");
        check(ToolPouchUtil.getElytraFromToolPouch(player, true) != null, "disabled cosmetic lookup retains elytra");
        player.startFallFlying();
        flightWearTick(player);
        check(!player.isFallFlying(), "disabled pouch ends ongoing flight on next update");
        check(snapshot.equals(holder.get(DataComponents.CONTAINER)), "disabled flight leaves all pouch contents intact");
        var chest = new ItemStack(Items.ELYTRA);
        player.setItemSlot(EquipmentSlot.CHEST, chest);
        check(canGlide(player), "disabled pouch preserves vanilla chest flight");
        flightWearTick(player);
        check(chest.getDamageValue() == 1, "disabled pouch preserves vanilla chest durability wear");
        check(snapshot.equals(holder.get(DataComponents.CONTAINER)), "vanilla chest flight does not damage disabled pouch elytra");
        player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        NetworkEvents.toggleElytra(player);
        check(canGlide(player), "second server toggle restores pouch flight");
        ToolPouch.CONFIG.allowUseFromInventory.accept(false);
        check(canGlide(player) == leggings, "inventory policy respected after toggling");
        ToolPouch.CONFIG.allowUseFromInventory.accept(true);
        evidence.add("PASS " + (leggings ? "leggings" : "inventory") + ": actual flight, midflight disable, cosmetic lookup, contents, durability, inventory policy");
    }

    private void persistence(ServerLevel level) throws Exception {
        var original = player(level);
        state(original).toolpouch$setElytraEnabled(false);
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        original.saveWithoutId(output);
        var saved = output.buildResult();
        check(!saved.getBooleanOr("ToolPouchElytraEnabled", true), "disabled state saved to player data");
        var loaded = player(level);
        loaded.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
        check(!state(loaded).toolpouch$isElytraEnabled(), "disabled state survives save/load");
        saved.remove("ToolPouchElytraEnabled");
        loaded.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
        check(state(loaded).toolpouch$isElytraEnabled(), "legacy save missing setting defaults enabled");
        for (boolean alive : List.of(false, true)) {
            var replacement = player(level);
            replacement.restoreFrom(original, alive);
            check(!state(replacement).toolpouch$isElytraEnabled(), "disabled state copied by restoreFrom alive=" + alive);
        }
        NetworkEvents.toggleElytra(original);
        var replacement = player(level);
        state(replacement).toolpouch$setElytraEnabled(false);
        replacement.restoreFrom(original, false);
        check(state(replacement).toolpouch$isElytraEnabled(), "enabled state also copied on respawn");
        evidence.add("PASS persistence: player save/load, legacy defaults, death/transfer restore");
    }

    private void isolationAndBroken(ServerLevel level) throws Exception {
        var first = player(level);
        var second = player(level);
        pouch(first, false, false);
        pouch(second, false, false);
        NetworkEvents.toggleElytra(first);
        check(!canGlide(first) && canGlide(second), "one player's toggle cannot affect another");
        var broken = player(level);
        pouch(broken, false, true);
        check(!canGlide(broken), "enabled broken elytra cannot fly");
        NetworkEvents.toggleElytra(broken);
        NetworkEvents.toggleElytra(broken);
        check(!canGlide(broken), "toggling does not repair broken elytra");
        var empty = player(level);
        NetworkEvents.toggleElytra(empty);
        check(!state(empty).toolpouch$isElytraEnabled() && !canGlide(empty), "toggle without pouch is safe");
        pouch(empty, false, false);
        check(!canGlide(empty), "new pouch respects existing player preference");
        evidence.add("PASS player isolation, broken elytra, toggling before acquiring a pouch");
    }

    @Override public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> { if (finished && ++drain >= 3) server.halt(false); });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            try {
                ToolPouch.CONFIG.allowUseFromInventory.accept(true);
                flight(server.overworld(), false);
                flight(server.overworld(), true);
                persistence(server.overworld());
                isolationAndBroken(server.overworld());
                evidence.addFirst("PASS " + checks + " dedicated-server assertions; no GUI/network claims");
                Files.write(Path.of("result.txt"), evidence);
                evidence.forEach(System.out::println);
            } catch (Throwable failure) {
                failure.printStackTrace();
                try { Files.writeString(Path.of("result.txt"), "FAIL " + failure + "\n" + String.join("\n", evidence)); }
                catch (Exception nested) { throw new RuntimeException(nested); }
            } finally { finished = true; }
        });
    }
}
