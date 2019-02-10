package ar.edu.itba.ss.plotter;

import ar.edu.itba.ss.domain.CommonScenario;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class CurvaDeDescarga {

    private int numberOfParticles;
    private double drivenVelocity;

    private List<List<Vector2D>> manyEgresos = new ArrayList<>();

    public CurvaDeDescarga(int numberOfParticles, double drivenVelocity) {
        this.numberOfParticles = numberOfParticles;
        this.drivenVelocity = drivenVelocity;
    }

    public void process(int samples){
        manyEgresos = IntStream.range(0, samples).boxed()
                .map( i -> CommonScenario.simulate(numberOfParticles,numberOfParticles, drivenVelocity).getEgresos())
                .collect(toList());
    }

    public Map<Double, ErrorPoint> plotAdaptingX(int bucketsForX){
        double maxTime = findMaxTime(manyEgresos);

        Map<Double, ErrorPoint> plot = new HashMap<>();
        for(Double xValue : getXValues(0, maxTime, bucketsForX)){
            double avg = manyEgresos.stream().mapToLong( l -> countEgresosBefore(l,xValue)).average().getAsDouble();
            double min = manyEgresos.stream().mapToLong( l -> countEgresosBefore(l,xValue)).min().getAsLong();
            double max = manyEgresos.stream().mapToLong( l -> countEgresosBefore(l,xValue)).max().getAsLong();
            ErrorPoint errorPoint = new ErrorPoint( avg, min, max);
            plot.put(xValue, errorPoint);
        }
        return plot;
    }

    public String printXValues(Map<Double, ErrorPoint> plot){
        return plot.keySet().stream().sorted()
                .map(x -> x.toString())
                .collect(Collectors.joining(", "));
    }

    private String printYValues(Map<Double, ErrorPoint> plot) {
        return plot.keySet().stream().sorted()
                .map(x -> plot.get(x).getAverage()+"")
                .collect(Collectors.joining(", "));
    }

    private String printCaudal(Map<Double, ErrorPoint> plot) {
        return plot.keySet().stream().sorted()
                .map(x -> x == 0 ? 0:plot.get(x).getAverage()/x)
                .map( c -> c+"")
                .collect(Collectors.joining(", "));
    }

    private String printMinMaxValues(Map<Double, ErrorPoint> plot) {
        return plot.keySet().stream().sorted()
                .map(x -> String.format("[%.3f, %.3f]", plot.get(x).getMin(), plot.get(x).getMax()))
                .collect(Collectors.joining(", "));
    }

    private Long countEgresosBefore(List<Vector2D> l, Double xValue) {
        return l.stream().filter( v -> v.getX() <= xValue).count();
    }

    private List<Double> getXValues(double minValue, double maxValue,int buckets) {
        double bucketSize = (maxValue - minValue)/ buckets;

        List<Double> values = new ArrayList<>();
        for(double x = minValue; x < maxValue+bucketSize; x+= bucketSize){
            values.add(x);
        }
        return values;
    }

    private double findMaxTime(List<List<Vector2D>> manyEgresos) {
        return manyEgresos.stream()
                .mapToDouble( l ->
                                    l.stream().mapToDouble( v -> v.getX())
                                            .max().getAsDouble()
                                ).max().getAsDouble();
    }


    public static void main(String[] args) {
        CurvaDeDescarga cd = new CurvaDeDescarga(100,3);
        cd.process(1);
        Map<Double, ErrorPoint> plot = cd.plotAdaptingX(9);
        String x = cd.printXValues(plot);
        String y = cd.printYValues(plot);
        String minMax = cd.printMinMaxValues(plot);
        String caudal = cd.printCaudal(plot);
        System.out.println(x);
        System.out.println(y);
        System.out.println(minMax);
        System.out.println(caudal);

    }

}
