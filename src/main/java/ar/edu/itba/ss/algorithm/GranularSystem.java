package ar.edu.itba.ss.algorithm;

import ar.edu.itba.ss.domain.Room;
import ar.edu.itba.ss.helper.Printer;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.ArrayList;
import java.util.List;

public class GranularSystem {

    private double dt;
    private long dt2;
    private int particleNumbersForStreet1;
    private int particleNumbersForStreet2;

    private Room room;
    private Printer printer;

    private boolean updateStatisticalValues;

    //(tiempo de egreso, particula)
    private List<Vector2D> egresos = new ArrayList<>();

    private static final double SLIDING_WINDOW = .2;

    public GranularSystem(double dt, long dt2, Room room, int particleNumbersForStreet1,
                          int particleNumbersForStreet2) {
        this.dt = dt;
        this.dt2 = dt2;
        this.room = room;
        this.particleNumbersForStreet1 = particleNumbersForStreet1;
        this.particleNumbersForStreet2 = particleNumbersForStreet2;
    }

    public void recordStatistics(){
        updateStatisticalValues = true;
    }

    public void setPrintable(){
        printer = new Printer(room);
    }

    public void simulate(){
        room.fillStreet(particleNumbersForStreet1, particleNumbersForStreet2);

        double t = 0;
        long i = 0;

        /*for (; room.isSomeoneLeftToEscape() && (t < 300) &&
                ((particleNumbersForStreet1 - egresos.size())>0); t+=dt, i++ ){*/
        for (; i<2.5e5; t+=dt, i++ ){
            if (i % dt2 == 0 ) {
                if(printer != null){
                    printer.printState(t, room.getParticles());
                    if((i*2e5)%555 == 0){
                        System.out.println(particleNumbersForStreet1 - egresos.size() +" t="+t);
                    }
                }

            }
            if(updateStatisticalValues ){
                //updateEscapes(t);
            }
            room.evolveLeapFrog(dt);

        }
        printer.printAll();
    }

    public List<Vector2D> getEgresos() {
        return egresos;
    }

    private void updateEscapes(double t) {
        List<Vector2D> escaped = room.getParticlesHaveJustEscaped(t);
        if(!escaped.isEmpty()){
            egresos.addAll(escaped);
        }
    }


}
