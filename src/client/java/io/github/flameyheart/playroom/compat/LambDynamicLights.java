package io.github.flameyheart.playroom.compat;

import dev.lambdaurora.lambdynlights.api.DynamicLightHandlers;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;
import io.github.flameyheart.playroom.registry.Entities;

public class LambDynamicLights implements DynamicLightsInitializer {

    @Override
    public void onInitializeDynamicLights() {
        DynamicLightHandlers.registerDynamicLightHandler(Entities.LASER_SHOT, lightSource -> 7);
    }
}
