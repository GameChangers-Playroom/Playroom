package io.github.flameyheart.playroom.zoom.interpolate;

import io.github.flameyheart.playroom.zoom.TransitionType;

import java.util.function.Supplier;

public final class TransitionInterpolator extends TimedInterpolator {
    private final Supplier<TransitionType> transitionIn;
    private final Supplier<TransitionType> transitionOut;
    private TransitionType activeTransition;
    private TransitionType inactiveTransition;
    private double prevTargetInterpolation = 0.0;
    private boolean justSwappedTransition = false;

    public TransitionInterpolator(Supplier<TransitionType> transitionIn, Supplier<TransitionType> transitionOut, Supplier<Double> timeIn, Supplier<Double> timeOut) {
        super(timeIn, timeOut);
        this.transitionIn = transitionIn;
        this.transitionOut = transitionOut;
        activeTransition = transitionIn.get();
        inactiveTransition = transitionOut.get();
    }

    @Override
    public double tickInterpolation(double targetInterpolation, double currentInterpolation, double tickDelta) {
        var currentInterpolationMod = currentInterpolation;

        if (targetInterpolation > currentInterpolation) {
            activeTransition = transitionIn.get();
            inactiveTransition = transitionOut.get();

            if (prevTargetInterpolation < targetInterpolation && activeTransition.hasInverse()) {
                justSwappedTransition = true;
                currentInterpolationMod = activeTransition.inverse(inactiveTransition.apply(currentInterpolationMod));
            }
        } else if (targetInterpolation < currentInterpolation) {
            activeTransition = transitionOut.get();
            inactiveTransition = transitionIn.get();

            if (prevTargetInterpolation > targetInterpolation && activeTransition.hasInverse()) {
                justSwappedTransition = true;
                currentInterpolationMod = activeTransition.inverse(inactiveTransition.apply(currentInterpolationMod));
            }
        }

        prevTargetInterpolation = targetInterpolation;

        if (activeTransition == TransitionType.INSTANT) {
            return targetInterpolation;
        }

        return super.tickInterpolation(targetInterpolation, currentInterpolationMod, tickDelta);
    }

    @Override
    public double modifyInterpolation(double interpolation) {
        return activeTransition.apply(interpolation);
    }

    @Override
    public double modifyPrevInterpolation(double interpolation) {
        if (justSwappedTransition) {
            justSwappedTransition = false;
            return activeTransition.inverse(inactiveTransition.apply(interpolation));
        }
        return interpolation;
    }

    @Override
    public boolean isSmooth() {
        return !justSwappedTransition && super.isSmooth() && activeTransition != TransitionType.INSTANT;
    }
}
