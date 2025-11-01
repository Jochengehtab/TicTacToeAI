package src.SPSA;

import java.util.Locale;

public class Param {
    public final String name;
    final int minValue, maxValue;
    final double step;
    final double startVal;
    double value;

    public Param(String name, double value, int minValue, int maxValue, double step) {
        if (step <= 0) throw new IllegalArgumentException("step must be > 0");
        this.name = name;
        this.value = value;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.step = step;
        this.startVal = value;
    }

    public int get() {
        return (int) Math.round(value);
    }

    public void update(double amt) {
        value = Math.min(Math.max(value + amt, minValue), maxValue);
    }

    public String deltaStr() {
        if (value > startVal) return "+" + String.format(Locale.ROOT, "%.3f", value - startVal);
        if (value < startVal) return "-" + String.format(Locale.ROOT, "%.3f", startVal - value);
        return "+-0";
    }

    @Override
    public String toString() {
        return name + "=" + get() + " (" + deltaStr() + ") [" + minValue + "," + maxValue + "]";
    }
}