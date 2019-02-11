package ar.edu.itba.ss.execution;

import ar.edu.itba.ss.algorithm.GranularSystem;
import ar.edu.itba.ss.domain.Room;

public class Main {

    //Dimensiones de la habitacion
    private static final double WIDTH = 50;
    private static final double HEIGHT = 30;
    private static final double EXIT_WIDTH = 10;

    //tiempos de simulacion
    private static final double DT = 5e-5;
    private static final long DT2 = (long)5e2;

    private static final int PARTICLE_NUMBER_ST1 = 50;
    private static final int PARTICLE_NUMBER_ST2 = 40;

    public static void main(String[] args) {

        double topPadding = 0;
        double bottomPadding = 5;
        double drivenVelocity = 5;
        Room room = new Room(WIDTH, HEIGHT, EXIT_WIDTH, topPadding,bottomPadding,drivenVelocity, 5, 5);
        GranularSystem system = new GranularSystem(DT, DT2, room, PARTICLE_NUMBER_ST1, PARTICLE_NUMBER_ST2);
        system.setPrintable();
        system.recordStatistics();
        system.simulate();
        System.out.println(1);
    }

}
