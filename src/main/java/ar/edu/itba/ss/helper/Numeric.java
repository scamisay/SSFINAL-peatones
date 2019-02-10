package ar.edu.itba.ss.helper;

import org.apache.commons.math3.util.FastMath;

public class Numeric {
    public static double randomBetween(double a, double b){
        if(b < a){
            return FastMath.random()*(a-b) + b;
        }
        return FastMath.random()*(b-a) + a;

    }
}
