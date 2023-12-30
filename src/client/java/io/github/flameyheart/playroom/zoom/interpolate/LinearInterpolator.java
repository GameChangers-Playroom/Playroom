package io.github.flameyheart.playroom.zoom.interpolate;

public abstract sealed class LinearInterpolator implements Interpolator permits SmoothInterpolator, TimedInterpolator {
    protected boolean goingIn = true;

    public void setGoingIn(boolean goingIn) {
        this.goingIn = goingIn;
    }

    @Override
    public double tickInterpolation(double targetInterpolation, double currentInterpolation, double tickDelta) {
        if (targetInterpolation > currentInterpolation) {
            goingIn = true;
            return Math.min(currentInterpolation + getTimeIncrement(false, tickDelta, targetInterpolation, currentInterpolation), targetInterpolation);
        } else if (targetInterpolation < currentInterpolation) {
            goingIn = false;
            return Math.max(currentInterpolation - getTimeIncrement(true, tickDelta, targetInterpolation, currentInterpolation), targetInterpolation);
        }
        goingIn = true;
        return targetInterpolation;
    }

    @Override
    public boolean isSmooth() {
        return true;
    }

    abstract double getTimeIncrement(boolean zoomingOut, double tickDelta, double targetInterpolation, double currentInterpolation);
}
