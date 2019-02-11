package ar.edu.itba.ss.algorithm;

import ar.edu.itba.ss.domain.Area;
import ar.edu.itba.ss.domain.Particle;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import static ar.edu.itba.ss.helper.Numeric.randomBetween;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class ParticlesCreator {

    public static final double MASS = 70;//kg
    public static final double MIN_RADIUS = .5/2;//m
    public static final double MAX_RADIUS = .58/2;//m

    private Area area;

    public ParticlesCreator(Area area) {
        this.area = area;
    }

    public Particle create(Vector2D target, boolean isStreet1, double streetWith) {
        double radius = createRadius();
        Vector2D position = createPosition(radius, isStreet1, streetWith);
        return new Particle(position, MASS, radius, target);
    }

    private double createRadius() {
        return randomBetween(MIN_RADIUS, MAX_RADIUS);
    }

    private Vector2D createPosition(double radius, boolean isStreet1, double streetWith) {
        return createRandomPosition(radius, isStreet1, streetWith);
    }

    public Vector2D createRandomPosition(double radius, boolean isStreet1, double streetWith){
        double crowdRadius = area.getHeight()/2;
        double minHeight = area.getMinY();

        double x ;
        double y;

        if(isStreet1){
            x = randomBetween(area.getWidth()/2. - streetWith/2. + radius, area.getWidth()/2. + streetWith/2. - radius);
            y = randomBetween(area.getHeight()*4./5, area.getHeight() - radius);
        }else {
            x = randomBetween(radius, area.getWidth()*1./5);
            y = randomBetween(area.getHeight()/2. - streetWith/2. + radius, area.getHeight()/2. + streetWith/2. - radius);
        }

        return new Vector2D(x,y);
    }

    private double heightInCircle(double r, double x, double x_0, double y_0) {
        return sqrt(pow(r,2)-pow(x-x_0,2))+y_0;
    }

}
