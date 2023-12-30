package io.github.flameyheart.playroom.zoom.interpolate;

public class InstantInterpolator implements Interpolator {
    @Override
    public double tickInterpolation(double targetInterpolation, double currentInterpolation, double tickDelta) {
        return targetInterpolation;
    }

    @Override
    public boolean isSmooth() {
        return false;
    }
}
