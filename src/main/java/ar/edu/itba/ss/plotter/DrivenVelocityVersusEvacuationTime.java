package ar.edu.itba.ss.plotter;

import ar.edu.itba.ss.domain.CommonScenario;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class DrivenVelocityVersusEvacuationTime {

    private int numberOfParticles;
    private List<Double> drivenVelocities;


    public DrivenVelocityVersusEvacuationTime(int numberOfParticles, List<Double> drivenVelocities) {
        this.numberOfParticles = numberOfParticles;
        this.drivenVelocities = drivenVelocities;
    }

    public Map<Double, ErrorPoint> plot(int samples){
        Map<Double, ErrorPoint> plot = new HashMap<>();
        for(Double drivenVelocity : drivenVelocities){
            List<List<Vector2D>> manyEgresos = IntStream.range(0, samples).boxed()
                    .map( i -> CommonScenario.simulate(numberOfParticles, numberOfParticles, drivenVelocity).getEgresos())
                    .collect(toList());
            List<Double> evacuationTimes = manyEgresos.stream().map( l -> l.stream().mapToDouble( v->v.getX()).max().getAsDouble()).collect(toList());
            double avg = evacuationTimes.stream().mapToDouble(t->t).average().getAsDouble();
            double min = evacuationTimes.stream().mapToDouble(t->t).min().getAsDouble();
            double max = evacuationTimes.stream().mapToDouble(t->t).max().getAsDouble();
            ErrorPoint errorPoint = new ErrorPoint( avg, min, max);
            plot.put(drivenVelocity, errorPoint);
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

    private String printMinMaxValues(Map<Double, ErrorPoint> plot) {
        return plot.keySet().stream().sorted()
                .map(x -> String.format("[%.3f, %.3f]", plot.get(x).getMin(), plot.get(x).getMax()))
                .collect(Collectors.joining(", "));
    }



    public static void main(String[] args) {
        List<Double> drivenVelocities = Arrays.asList(0.80000 ,  1.84000 ,  2.88000 ,  3.92000 ,  4.96000 ,  6.00000);
        DrivenVelocityVersusEvacuationTime cd = new DrivenVelocityVersusEvacuationTime(100,drivenVelocities);
        Map<Double, ErrorPoint> plot = cd.plot(3);
        String x = cd.printXValues(plot);
        String y = cd.printYValues(plot);
        String minMax = cd.printMinMaxValues(plot);
        System.out.println(x);
        System.out.println(y);
        System.out.println(minMax);

    }

}
