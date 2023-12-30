package io.github.flameyheart.playroom.zoom.interpolate;

import java.util.function.Supplier;

public sealed class TimedInterpolator extends LinearInterpolator permits TransitionInterpolator {
    private final Supplier<Double> timeIn;
    private final Supplier<Double> timeOut;

    public TimedInterpolator(Supplier<Double> timeIn, Supplier<Double> timeOut) {
        this.timeIn = timeIn;
        this.timeOut = timeOut;
    }

    @Override
    double getTimeIncrement(boolean zoomingOut, double tickDelta, double targetInterpolation, double currentInterpolation) {
        return tickDelta / (zoomingOut ? timeOut.get() : timeIn.get());
    }

    @Override
    public boolean isSmooth() {
        return (goingIn && timeIn.get() > 0.0) || (!goingIn && timeOut.get() > 0.0);
    }
}
