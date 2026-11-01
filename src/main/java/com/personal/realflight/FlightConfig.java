package com.personal.realflight;

import net.minecraftforge.common.ForgeConfigSpec;

public final class FlightConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLE;
    public static final ForgeConfigSpec.BooleanValue ALLOW_SURVIVAL;
    public static final ForgeConfigSpec.IntValue MIN_SPEED;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        ENABLE = b
                .comment("Switch on AiT's real world flight (it is off by default in AiT's own ait-server config).")
                .define("enableRealWorldFlight", true);
        ALLOW_SURVIVAL = b
                .comment("Let players who are not in creative mode start real world flight from the console's visualiser control.",
                        "AiT itself only allows creative players.")
                .define("allowSurvival", true);
        MIN_SPEED = b
                .comment("AiT scales your flying speed by the TARDIS throttle, so at throttle 0 the TARDIS only rises and falls.",
                        "If the throttle is below this when you take off, it is raised to this. 0 leaves the throttle alone.")
                .defineInRange("minimumThrottleOnTakeoff", 1, 0, 7);
        SPEC = b.build();
    }

    private FlightConfig() {
    }
}
