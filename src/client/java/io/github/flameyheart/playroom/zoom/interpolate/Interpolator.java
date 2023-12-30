package io.github.flameyheart.playroom.zoom.interpolate;

import java.util.function.Supplier;

public interface Interpolator {
    double tickInterpolation(double targetInterpolation, double currentInterpolation, double tickDelta);

    default double modifyInterpolation(double interpolation) {
        return interpolation;
    }

    default double modifyPrevInterpolation(double interpolation) {
        return interpolation;
    }

    boolean isSmooth();
}

