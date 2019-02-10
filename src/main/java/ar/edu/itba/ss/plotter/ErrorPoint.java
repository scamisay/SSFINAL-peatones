package ar.edu.itba.ss.plotter;

public class ErrorPoint {
    private double average;
    private double min;
    private double max;

    public ErrorPoint(double average, double min, double max) {
        this.average = average;
        this.min = min;
        this.max = max;
    }

    public double getAverage() {
        return average;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }
}
