package io.github.flameyheart.playroom.zoom;

import io.github.flameyheart.playroom.PlayroomClient;
import io.github.flameyheart.playroom.zoom.interpolate.Interpolator;
import net.minecraft.util.math.MathHelper;

import java.util.function.Supplier;

public class ZoomHelper {
    private final Interpolator initialInterpolator;
    private final Interpolator scrollInterpolator;

    private final Supplier<Integer> initialZoom;
    private final Supplier<Integer> scrollZoomAmount;
    public final Supplier<Integer> maxScrollTiers;
    private final Supplier<Boolean> linearLikeSteps;

    private double prevInitialInterpolation = 0.0;
    private double initialInterpolation = 0.0;

    private boolean zoomingLastTick = false;

    private double prevScrollInterpolation = 0.0;
    private double scrollInterpolation = 0.0;
    private int lastScrollTier = 0;

    private boolean resetting = false;
    private double resetMultiplier = 0.0;

    public ZoomHelper(Interpolator initialInterpolator, Interpolator scrollInterpolator, Supplier<Integer> initialZoom, Supplier<Integer> scrollZoomAmount, Supplier<Integer> maxScrollTiers, Supplier<Boolean> linearLikeSteps) {
        this.initialInterpolator = initialInterpolator;
        this.scrollInterpolator = scrollInterpolator;
        this.initialZoom = initialZoom;
        this.scrollZoomAmount = scrollZoomAmount;
        this.maxScrollTiers = maxScrollTiers;
        this.linearLikeSteps = linearLikeSteps;
    }

    public void tick(boolean zooming, int scrollTiers) {
        tick(zooming, scrollTiers, 0.05);
    }

    public void tick(boolean zooming, int scrollTiers, double lastFrameDuration) {
        tickInitial(zooming, lastFrameDuration);
        tickScroll(scrollTiers, lastFrameDuration);
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

    private void tickScroll(int scrollTiers, double lastFrameDuration) {
        if (scrollTiers > lastScrollTier) resetting = false;

        var targetZoom = (maxScrollTiers.get() > 0) ? scrollTiers / PlayroomClient.MAX_SCROLL_TIERS : 0.0;
        if (linearLikeSteps.get()) {
            double curvature = 0.3;
            double exp = 1 / (1 - curvature);
            targetZoom = 2 * (Math.pow(targetZoom, exp) / (Math.pow(targetZoom, exp) + Math.pow((2 - targetZoom), exp)));
        }

        prevScrollInterpolation = scrollInterpolation;
        scrollInterpolation = scrollInterpolator.tickInterpolation(targetZoom, scrollInterpolation, lastFrameDuration);
        prevScrollInterpolation = scrollInterpolator.modifyPrevInterpolation(prevScrollInterpolation);
        if (!initialInterpolator.isSmooth())
            prevInitialInterpolation = initialInterpolation;
        lastScrollTier = scrollTiers;
    }

    public double getZoomDivisor() {
        return getZoomDivisor(1f);
    }

    public double getZoomDivisor(float tickDelta) {
        double initialMultiplier = getInitialZoomMultiplier(tickDelta);
        double scrollDivisor = getScrollZoomDivisor(tickDelta);

        double v = 1 / initialMultiplier + scrollDivisor;
        if (initialInterpolation == 0.0 && scrollInterpolation == 0.0) resetting = false;
        if (!resetting) resetMultiplier = 1 / v;
        return v;
    }

    private double getInitialZoomMultiplier(Float tickDelta) {
        return MathHelper.lerp(
          initialInterpolator.modifyInterpolation(MathHelper.lerp(tickDelta, prevInitialInterpolation, initialInterpolation)),
          1.0, (!resetting) ? 1d / initialZoom.get() : resetMultiplier
        );
    }

    private double getScrollZoomDivisor(Float tickDelta) {
        if (resetting) return 0.0;
        return MathHelper.lerp(scrollInterpolator.modifyInterpolation(
          MathHelper.lerp(tickDelta, prevScrollInterpolation, scrollInterpolation)), 0.0,
          PlayroomClient.MAX_SCROLL_TIERS * (scrollZoomAmount.get() * 3.0)
        );
    }

    public void reset() {
        if (!resetting && scrollInterpolation > 0.0) {
            resetting = true;
            scrollInterpolation = 0.0;
            prevScrollInterpolation = 0.0;
        }
    }

    public void setToZero(boolean initial, Boolean scroll) {
        if (initial) {
            initialInterpolation = 0.0;
            prevInitialInterpolation = 0.0;
            zoomingLastTick = false;
        }
        if (scroll) {
            scrollInterpolation = 0.0;
            prevScrollInterpolation = 0.0;
            lastScrollTier = 0;
        }
        resetting = false;
    }

    public void skipInitial() {
        initialInterpolation = 1.0;
        prevInitialInterpolation = 1.0;
    }
}
