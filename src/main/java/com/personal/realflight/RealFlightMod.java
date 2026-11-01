package com.personal.realflight;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(RealFlightMod.MOD_ID)
public class RealFlightMod {
    public static final String MOD_ID = "realflight";
    public static final Logger LOGGER = LogManager.getLogger("RealFlight");

    public RealFlightMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FlightConfig.SPEC);
    }
}
