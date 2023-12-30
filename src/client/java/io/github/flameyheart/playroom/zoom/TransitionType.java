package io.github.flameyheart.playroom.zoom;

import io.github.flameyheart.playroom.util.MathUtil;

public enum TransitionType implements Transition, SettingDisplayName {
    INSTANT("zoomify.transition.instant") {
        public double apply(double t) {
            return t;
        }
    },
    LINEAR("zoomify.transition.linear") {
        public double apply(double t) {
            return t;
        }
    },
    EASE_IN_SINE("zoomify.transition.ease_in_sine") {
        public double apply(double t) {
            return 1 - Math.cos((t * Math.PI) / 2);
        }

        public double inverse(double x) {
            return Math.acos(-(x - 1)) * 2 / Math.PI;
        }
    },
    EASE_OUT_SINE("zoomify.transition.ease_out_sine") {
        public double apply(double t) {
            return Math.sin((t * Math.PI) / 2);
        }

        public double inverse(double x) {
            return Math.asin(x) * 2 / Math.PI;
        }
    },
    EASE_IN_OUT_SINE("zoomify.transition.ease_in_out_sine") {
        public double apply(double t) {
            return -(Math.cos(Math.PI * t) - 1) / 2;
        }
    },
    EASE_IN_QUAD("zoomify.transition.ease_in_quad") {
        public double apply(double t) {
            return t * t;
        }

        public double inverse(double x) {
            return Math.sqrt(x);
        }
    },
    EASE_OUT_QUAD("zoomify.transition.ease_out_quad") {
        public double apply(double t) {
            return 1 - (1 - t) * (1 - t);
        }

        public double inverse(double x) {
            return -(Math.sqrt(-(x - 1)) - 1);
        }
    },
    EASE_IN_OUT_QUAD("zoomify.transition.ease_in_out_quad") {
        public double apply(double t) {
            return t < 0.5 ? 2 * t * t : 1 - Math.pow((-2 * t + 2), 2) / 2;
        }
    },
    EASE_IN_CUBIC("zoomify.transition.ease_in_cubic") {
        public double apply(double t) {
            return Math.pow(t, 3);
        }

        public double inverse(double x) {
            return Math.pow(x, 1 / 3.0);
        }
    },
    EASE_OUT_CUBIC("zoomify.transition.ease_out_cubic") {
        public double apply(double t) {
            return Math.pow(1 - (1 - t), 3);
        }

        public double inverse(double x) {
            return -Math.pow((-x + 1), 1.0 / 3.0) + 1;
        }
    },
    EASE_IN_OUT_CUBIC("zoomify.transition.ease_in_out_cubic") {
        public double apply(double t) {
            return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
        }
    },
    EASE_IN_EXP("zoomify.transition.ease_in_exp") {
        private final double c_log2_1023 = MathUtil.log2(1023.0);

        public double apply(double t) {
            if (t == 0.0) return 0.0;
            else if (t == 1.0) return 1.0;

            return Math.pow(2.0, 10.0 * t - c_log2_1023) - 1 / 1023d;
        }

        public double inverse(double x) {
            if (x == 0.0) return 0.0;
            else if (x == 1.0) return 1.0;

            return Math.log(1023 * x + 1) / (10 * Math.log(2.0));
        }
    },
    EASE_OUT_EXP("zoomify.transition.ease_out_exp") {
        private final double c_log2_1023 = MathUtil.log2(1023.0);
        private final double c_10_ln2 = 10.0 * Math.log(2.0);
        private final double c_ln_1203 = Math.log(1023.0);

        public double apply(double t) {
            if (t == 0.0) return 0.0;
            else if (t == 1.0) return 1.0;

            return 1.0 - Math.pow(2.0, 10.0 - c_log2_1023 - 10.0 * t) + 1 / 1023d;
        }


        public double inverse(double x) {
            if (x == 0.0) return 0.0;
            else if (x == 1.0) return 1.0;

            return -((Math.log(-((1023 * x - 1024) / 1023)) - c_10_ln2 + c_ln_1203) / c_10_ln2);
        }

    },
    EASE_IN_OUT_EXP("zoomify.transition.ease_in_out_exp") {
        private final double c_log2_1023 = MathUtil.log2(1023.0);

        public double apply(double t) {
            if (t == 0.0) return 0.0;
            else if (t == 1.0) return 1.0;
            else if (t < 0.5) return Math.pow(2.0, 20.0 * t - c_log2_1023) - 1 / 1023d;

            return 1.0 - Math.pow(2.0, 10.0 - c_log2_1023 - 10.0 * t) + 1 / 1023d;
        }
    };

    private final String displayName;
    private final Transition transition;

    TransitionType(String displayName) {
        this.displayName = displayName;
        this.transition = this;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public TransitionType opposite() {
        return switch (this) {
            case INSTANT -> INSTANT;
            case LINEAR -> LINEAR;
            case EASE_IN_SINE -> EASE_OUT_SINE;
            case EASE_OUT_SINE -> EASE_IN_SINE;
            case EASE_IN_OUT_SINE -> EASE_IN_OUT_SINE;
            case EASE_IN_QUAD -> EASE_OUT_QUAD;
            case EASE_OUT_QUAD -> EASE_IN_QUAD;
            case EASE_IN_OUT_QUAD -> EASE_IN_OUT_QUAD;
            case EASE_IN_CUBIC -> EASE_OUT_CUBIC;
            case EASE_OUT_CUBIC -> EASE_IN_CUBIC;
            case EASE_IN_OUT_CUBIC -> EASE_IN_OUT_CUBIC;
            case EASE_IN_EXP -> EASE_OUT_EXP;
            case EASE_OUT_EXP -> EASE_IN_EXP;
            case EASE_IN_OUT_EXP -> EASE_IN_OUT_EXP;
        };
    }
}

@FunctionalInterface
interface Transition {
    double apply(double t);

    default double inverse(double x) {
        throw new UnsupportedOperationException();
    }

    default boolean hasInverse() {
        try {
            inverse(0.0);
            return true;
        } catch (UnsupportedOperationException ignored) {
            return false;
        }
    }
}

interface SettingDisplayName {
    String getDisplayName();
}
