package io.github.crepper710.air_conditioning.containers;

import com.simibubi.create.repack.registrate.util.entry.ContainerEntry;
import io.github.crepper710.air_conditioning.AirConditioning;
import io.github.crepper710.air_conditioning.block.air_conditioning.AirConditioningContainer;
import io.github.crepper710.air_conditioning.block.air_conditioning.AirConditioningScreen;

public class ModContainers {
    public static final ContainerEntry<AirConditioningContainer> AIR_CONDITIONING;

    public static void register() {}

    static {
        AIR_CONDITIONING = AirConditioning.registrate().container("air_conditioning_container", AirConditioningContainer::new, () -> AirConditioningScreen::new).register();
    }
}
