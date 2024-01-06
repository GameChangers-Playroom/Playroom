package io.github.flameyheart.playroom.zoom;

import io.github.flameyheart.playroom.zoom.interpolate.Interpolator;
import net.minecraft.util.math.MathHelper;

import java.util.function.Supplier;

public class ZoomHelper {
    private final Interpolator initialInterpolator;

    private final Supplier<Integer> initialZoom;

    private double prevInitialInterpolation = 0.0;
    private double initialInterpolation = 0.0;

    private boolean zoomingLastTick = false;

    private boolean resetting = false;
    private double resetMultiplier = 0.0;

    public ZoomHelper(Interpolator initialInterpolator, Supplier<Integer> initialZoom) {
        this.initialInterpolator = initialInterpolator;
        this.initialZoom = initialZoom;
    }

    public void tick(boolean zooming) {
        tick(zooming, 0.05);
    }

    public void tick(boolean zooming, double lastFrameDuration) {
        tickInitial(zooming, lastFrameDuration);
    }

    private void tickInitial(boolean zooming, double lastFrameDuration) {
        if (zooming && !zoomingLastTick)
            resetting = false;

        double targetZoom = zooming ? 1.0 : 0.0;
        prevInitialInterpolation = initialInterpolation;
        initialInterpolation = initialInterpolator.tickInterpolation(targetZoom, initialInterpolation, lastFrameDuration);
        prevInitialInterpolation = initialInterpolator.modifyPrevInterpolation(prevInitialInterpolation);
        if (!initialInterpolator.isSmooth()) prevInitialInterpolation = initialInterpolation;
        zoomingLastTick = zooming;
    }

    public double getZoomDivisor() {
        return getZoomDivisor(1f);
    }

    public double getZoomDivisor(float tickDelta) {
        double initialMultiplier = getInitialZoomMultiplier(tickDelta);

        double v = 1 / initialMultiplier;
        if (initialInterpolation == 0.0) resetting = false;
        if (!resetting) resetMultiplier = 1 / v;
        return v;
    }

    private double getInitialZoomMultiplier(Float tickDelta) {
        return MathHelper.lerp(
          initialInterpolator.modifyInterpolation(MathHelper.lerp(tickDelta, prevInitialInterpolation, initialInterpolation)),
          1.0, (!resetting) ? 1d / initialZoom.get() : resetMultiplier
        );
    }

    public void reset() {
        if (!resetting) {
            resetting = true;
        }
    }

    public void setToZero() {
        initialInterpolation = 0.0;
        prevInitialInterpolation = 0.0;
        zoomingLastTick = false;
        resetting = false;
    }
}
