package com.example.liquorclient.utility;

public class AnimationUtils {
    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static double easeIn(double t) {
        return t * t;
    }

    public static double easeOut(double t) {
        return 1 - (1 - t) * (1 - t);
    }

    public static double easeInOut(double t) {
        return t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
    }

    public static double easeOutBounce(double t) {
        if (t < 1 / 2.75) {
            return 7.5625 * t * t;
        } else if (t < 2 / 2.75) {
            t -= 1.5 / 2.75;
            return 7.5625 * t * t + 0.75;
        } else if (t < 2.5 / 2.75) {
            t -= 2.25 / 2.75;
            return 7.5625 * t * t + 0.9375;
        } else {
            t -= 2.625 / 2.75;
            return 7.5625 * t * t + 0.984375;
        }
    }

    public static double easeOutElastic(double t) {
        if (t == 0 || t == 1) return t;
        return Math.pow(2, -10 * t) * Math.sin((t * 10 - 0.75) * (2 * Math.PI) / 3) + 1;
    }

    public static double easeInBack(double t) {
        double c1 = 1.70158;
        return (c1 + 1) * t * t * t - c1 * t * t;
    }

    public static double easeOutBack(double t) {
        double c1 = 1.70158;
        return 1 + (c1 + 1) * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2);
    }

    public static int lerpColor(int color1, int color2, double t) {
        return ColorUtils.blend(color1, color2, (float) t);
    }

    public static class Animation {
        private long startTime;
        private long duration;
        private double from;
        private double to;
        private Easing easing;
        private boolean started;

        public enum Easing {
            LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT, EASE_OUT_BOUNCE, EASE_OUT_ELASTIC
        }

        public Animation(long duration, double from, double to, Easing easing) {
            this.duration = duration;
            this.from = from;
            this.to = to;
            this.easing = easing;
            this.startTime = System.currentTimeMillis();
            this.started = true;
        }

        public Animation(long duration, double from, double to) {
            this(duration, from, to, Easing.EASE_OUT);
        }

        public double getValue() {
            if (!started) return from;
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed >= duration) return to;

            double t = (double) elapsed / duration;
            double eased = switch (easing) {
                case LINEAR -> t;
                case EASE_IN -> easeIn(t);
                case EASE_OUT -> easeOut(t);
                case EASE_IN_OUT -> easeInOut(t);
                case EASE_OUT_BOUNCE -> easeOutBounce(t);
                case EASE_OUT_ELASTIC -> easeOutElastic(t);
            };
            return lerp(from, to, eased);
        }

        public boolean isFinished() {
            return started && System.currentTimeMillis() - startTime >= duration;
        }

        public void reset(double from, double to) {
            this.from = from;
            this.to = to;
            this.startTime = System.currentTimeMillis();
        }

        public void reset(long duration, double from, double to) {
            this.duration = duration;
            this.from = from;
            this.to = to;
            this.startTime = System.currentTimeMillis();
        }

        public double getFrom() { return from; }
        public double getTo() { return to; }
    }

    public static Animation animate(long duration, double from, double to) {
        return new Animation(duration, from, to);
    }

    public static Animation animate(long duration, double from, double to, Animation.Easing easing) {
        return new Animation(duration, from, to, easing);
    }
}
