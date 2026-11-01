package com.personal.realflight;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

/**
 * AiT already has real world flight (the TARDIS exterior becomes a vehicle you fly around the overworld),
 * but it ships switched off and only lets creative players start it. This turns it on and lets survival
 * players start it from the console's visualiser control, using the same steps AiT's own control does.
 */
@Mod.EventBusSubscriber(modid = RealFlightMod.MOD_ID)
public final class FlightEvents {
    private static final String CONTROL_ENTITY = "dev.amble.ait.core.entities.ConsoleControlEntity";
    private static final Map<UUID, Long> COOLDOWN = new HashMap<>();

    private FlightEvents() {
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        enableFlight();
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        enableFlight();
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide() || event.getHand() != InteractionHand.MAIN_HAND) return;
        if (handle(event.getEntity(), event.getTarget())) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (handle(event.getEntity(), event.getTarget())) event.setCanceled(true);
    }

    /** Sets AiT's rwfEnabled switch. Safe to call over and over. */
    private static void enableFlight() {
        if (!FlightConfig.ENABLE.get()) return;
        try {
            ModList.get().getModObjectById("ait").ifPresent(AitBridge::rememberLoader);
            Class<?> mod = AitBridge.findClass("dev.amble.ait.AITMod");
            Object config = mod.getField("CONFIG").get(null);
            if (config == null) return;
            Field f = config.getClass().getField("rwfEnabled");
            if (!f.getBoolean(config)) {
                f.setBoolean(config, true);
                RealFlightMod.LOGGER.info("Real world flight switched on");
            }
        } catch (Throwable t) {
            RealFlightMod.LOGGER.warn("Could not switch on AiT real world flight: {}", t.toString());
        }
    }

    /** Returns true when the click was the visualiser control and this add-on dealt with it. */
    private static boolean handle(net.minecraft.world.entity.player.Player player, Entity target) {
        if (!FlightConfig.ALLOW_SURVIVAL.get() || !FlightConfig.ENABLE.get()) return false;
        if (!(player instanceof ServerPlayer sp) || player.isCreative()) return false; // creative: AiT handles it
        if (!target.getClass().getName().equals(CONTROL_ENTITY)) return false;

        try {
            AitBridge.rememberLoader(target);
            Object control = AitBridge.call(target, "getControl");
            if (control == null || !"ait:visualiser".equals(String.valueOf(AitBridge.call(control, "id")))) return false;

            // One click can arrive as both a use and an attack; only act once.
            long now = sp.serverLevel().getGameTime();
            Long last = COOLDOWN.get(sp.getUUID());
            if (last != null && now - last < 10) return true;
            COOLDOWN.put(sp.getUUID(), now);

            if (sp.isShiftKeyDown()) return true;
            enableFlight();
            Object tardis = AitBridge.call(AitBridge.call(target, "tardis"), "get");
            start(sp, tardis);
        } catch (Throwable t) {
            RealFlightMod.LOGGER.error("Could not start real world flight", t);
            sp.displayClientMessage(Component.literal("Real world flight failed to start (see latest.log)."), false);
        }
        return true;
    }

    /** Takes off: the same checks and steps as AiT's visualiser control, minus the creative-only rule. */
    static void start(ServerPlayer sp, Object tardis) throws Exception {
        ServerLevel world = sp.serverLevel();
        Object travel = AitBridge.call(tardis, "travel");
        boolean landed = "LANDED".equals(String.valueOf(AitBridge.call(travel, "getState")));
        Class<?> idClass = AitBridge.findClass("dev.amble.ait.core.engine.SubSystem$Id");
        @SuppressWarnings({"unchecked", "rawtypes"})
        Object gravId = Enum.valueOf((Class<? extends Enum>) idClass.asSubclass(Enum.class), "GRAVITATIONAL");
        Object grav = AitBridge.call(AitBridge.call(tardis, "subsystems"), "get", gravId);
        boolean gravOn = Boolean.TRUE.equals(AitBridge.call(grav, "isEnabled"));

        if (!landed) {
            sp.displayClientMessage(Component.literal("The TARDIS has to be landed to take off in real world flight."), true);
            return;
        }
        if (!gravOn) {
            sp.displayClientMessage(Component.literal("The gravitational subsystem is offline."), true);
            return;
        }
        int min = FlightConfig.MIN_SPEED.get();
        if (min > 0 && ((Number) AitBridge.call(travel, "speed")).intValue() < min) {
            AitBridge.call(travel, "speed", min);
        }
        world.playSound(null, sp.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1f, 1f);
        AitBridge.call(AitBridge.call(tardis, "flight"), "enterFlight", sp);
        RealFlightMod.LOGGER.info("{} took off in real world flight", sp.getGameProfile().getName());
    }

    @SubscribeEvent
    public static void onCommands(net.minecraftforge.event.RegisterCommandsEvent event) {
        event.getDispatcher().register(net.minecraft.commands.Commands.literal("realflight").executes(ctx -> {
            ServerPlayer sp = ctx.getSource().getPlayerOrException();
            if (!FlightConfig.ENABLE.get()) {
                sp.displayClientMessage(Component.literal("Real world flight is switched off in the add-on's config."), false);
                return 0;
            }
            try {
                ModList.get().getModObjectById("ait").ifPresent(AitBridge::rememberLoader);
                enableFlight();
                Class<?> world = AitBridge.findClass("dev.amble.ait.core.world.TardisServerWorld");
                if (!world.isInstance(sp.level())) {
                    sp.displayClientMessage(Component.literal("Use this inside your TARDIS."), false);
                    return 0;
                }
                start(sp, AitBridge.call(sp.level(), "getTardis"));
            } catch (Throwable t) {
                RealFlightMod.LOGGER.error("Could not start real world flight", t);
                sp.displayClientMessage(Component.literal("Real world flight failed to start (see latest.log)."), false);
                return 0;
            }
            return 1;
        }));
    }
}
