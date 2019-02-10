package ar.edu.itba.ss.plotter;

import ar.edu.itba.ss.domain.CommonScenario;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class CurvaDeDescargaConSlidingWindow {

    private int numberOfParticles;
    private double drivenVelocity;

    private List<List<Vector2D>> manyEgresos = new ArrayList<>();

    public CurvaDeDescargaConSlidingWindow(int numberOfParticles, double drivenVelocity) {
        this.numberOfParticles = numberOfParticles;
        this.drivenVelocity = drivenVelocity;
    }

    public void process(int samples){
        manyEgresos = IntStream.range(0, samples).boxed()
                .map( i -> CommonScenario.simulate(numberOfParticles, numberOfParticles, drivenVelocity).getEgresos())
                .collect(toList());
    }

    /**
     * cantidad de particulas x tiempo(avg, min, max) : (n, (t,t,t))
     */
    public Map<Integer, ErrorPoint> plotAdaptingX(){
        Map<Integer, ErrorPoint> plot = new HashMap<>();
        for(int n = 1; n <= numberOfParticles; n++){
            int index = n-1;
            double avg = manyEgresos.stream().mapToDouble(l -> l.get(index).getX()).average().getAsDouble();
            double min = manyEgresos.stream().mapToDouble(l -> l.get(index).getX()).min().getAsDouble();
            double max = manyEgresos.stream().mapToDouble(l -> l.get(index).getX()).max().getAsDouble();

            ErrorPoint errorPoint = new ErrorPoint( avg, min, max);
            plot.put(n, errorPoint);
        }
        return plot;
    }

    public String printXValues(Map<Integer, ErrorPoint> plot){
        return plot.keySet().stream().sorted()
                .map(x -> x.toString())
                .collect(joining(", "));
    }

    private String printYValues(Map<Integer, ErrorPoint> plot) {
        return plot.keySet().stream().sorted()
                .map(x -> String.format("%.3f",plot.get(x).getAverage()))
                .collect(joining(", "));
    }


    private String printMinMaxValues(Map<Integer, ErrorPoint> plot) {
        return plot.keySet().stream().sorted()
                .map(x -> String.format("[%.3f, %.3f]", plot.get(x).getMin(), plot.get(x).getMax()))
                .collect(joining(", "));
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

    /**
     * @param curvaDeDescaga : (n, t)
     *
     *  @return caudal : (t, q)
     */
    public List<Vector2D> calculateCaudal(int windowSize, List<Vector2D> curvaDeDescaga){
        if(windowSize > curvaDeDescaga.size()){
            throw new RuntimeException("La ventana es demasiado grande");
        }
        return IntStream.range(0, curvaDeDescaga.size()-windowSize).boxed().map(
                i ->
                        new Vector2D(
                                (curvaDeDescaga.get(i).getY()+curvaDeDescaga.get(i+windowSize).getY())/2,
                                windowSize/(curvaDeDescaga.get(i+windowSize).getY()-curvaDeDescaga.get(i).getY())
                        )
        ).collect(toList());
    }

/*
    public static void main(String[] args) {
        CurvaDeDescargaConSlidingWindow cd = new CurvaDeDescargaConSlidingWindow(100,3);
        cd.process(5);
        Map<Integer, ErrorPoint> plot = cd.plotAdaptingX();
        List<Vector2D> curvaDeDescarga = cd.getCurvaDeDescarga(plot);
        List<Vector2D> caudal = cd.calculateCaudal(3, curvaDeDescarga);

        String x = cd.printXValues(plot);
        String y = cd.printYValues(plot);
        String minMax = cd.printMinMaxValues(plot);

        String xCaudal = caudal.stream().map(q -> String.format("%.3f",q.getX())).collect(joining(", "));
        String yCaudal = caudal.stream().map(q -> String.format("%.3f",q.getY())).collect(joining(", "));
        System.out.println(x);
        System.out.println(y);
        System.out.println(minMax);
        System.out.println(xCaudal);
        System.out.println(yCaudal);

    }
*/
    /**
     *
     * @param plottedCD : (n, (t,t,t))
     * @return (n,t)
     */
    private List<Vector2D> getCurvaDeDescarga(Map<Integer, ErrorPoint> plottedCD) {
        return plottedCD.keySet().stream().sorted()
                .map( n -> new Vector2D(n, plottedCD.get(n).getAverage()))
                .collect(toList());
    }

    public List<Vector2D> parseLists(String slx, String sly){
        List<Double> lx = Arrays.stream(slx.split(",")).map(s -> Double.parseDouble(s)).collect(toList());
        List<Double> ly = Arrays.stream(slx.split(",")).map(s -> Double.parseDouble(s)).collect(toList());
        List<Vector2D> l = new ArrayList<>();
        for(int i = 0; i < lx.size(); i++){
            l.add(new Vector2D(lx.get(i),ly.get(i)));
        }
        return l;
    }

    public static void main(String[] args) {
        CurvaDeDescargaConSlidingWindow cd = new CurvaDeDescargaConSlidingWindow(100,3);
        String slx = "0.801, 1.243, 1.417, 1.647, 1.860, 1.999, 2.139, 2.285, 2.392, 2.549, 2.697, 2.819, 2.886, 3.020, 3.130, 3.326, 3.556, 3.677, 3.802, 3.981, 4.068, 4.238, 4.366, 4.507, 4.622, 4.803, 4.948, 5.047, 5.185, 5.382, 5.537, 5.649, 5.843, 5.965, 6.121, 6.258, 6.373, 6.530, 6.734, 6.947, 7.138, 7.222, 7.406, 7.587, 7.747, 7.902, 8.114, 8.263, 8.404, 8.594, 8.756, 8.943, 9.095, 9.247, 9.405, 9.614, 9.777, 9.911, 10.080, 10.187, 10.392, 10.555, 10.796, 11.058, 11.276, 11.502, 11.632, 11.851, 12.011, 12.225, 12.425, 12.619, 12.963, 13.203, 13.557, 13.741, 13.956, 14.201, 14.503, 14.802, 15.085, 15.383, 15.546, 15.877, 16.103, 16.341, 16.787, 17.080, 17.438, 17.796, 18.111, 18.603, 19.242, 19.499, 19.942, 20.271, 21.220, 21.391, 22.667, 22.879";
        String sly = "1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100";
        List<Vector2D> curvaDeDescarga = cd.parseLists(slx,sly);
        List<Vector2D> caudal = cd.calculateCaudal(1, curvaDeDescarga);

        String xCaudal = caudal.stream().map(q -> String.format("%.3f",q.getX())).collect(joining(", "));
        String yCaudal = caudal.stream().map(q -> String.format("%.3f",q.getY())).collect(joining(", "));
        System.out.println(xCaudal);
        System.out.println(yCaudal);

    }
}
