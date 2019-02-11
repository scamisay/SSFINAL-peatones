package ar.edu.itba.ss.helper;

import ar.edu.itba.ss.domain.Particle;
import ar.edu.itba.ss.domain.Room;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Printer {

    private double height;
    private double width;
    private Room room;
    private StringBuilder output = new StringBuilder();

    private static final String FILE_NAME_OVITO = "ovito_"+new SimpleDateFormat("dd_MM_yyyy_HHmmss").format(new Date()) +".xyz";

    public Printer(Room room) {
        this.room = room;
        this.height = room.getScenarioHeight();
        this.width = room.getWidth();
        try{
            File file = new File(FILE_NAME_OVITO);

            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
        }catch (Exception e){
            System.out.println("problemas creando el archivo "+FILE_NAME_OVITO);
        }
    }

    public void printState(double time, List<Particle> particles){
        printForOvito(time, particles);
    }

    private void printForOvito(double time, List<Particle> particles) {
        output.append(printParticles(time,particles));
        //printStringToFile(FILE_NAME_OVITO, printParticles(time, particles));
    }

    public void printAll() {
        printStringToFile(FILE_NAME_OVITO,output.toString());
    }

    private String printParticles(double time, List<Particle> particles) {
        String printedBorders = printSiloBorders();
        int particlesInBorder = printedBorders.split("\n").length;
        return (particles.size()+particlesInBorder+2)+"\n"+
                time + "\n" +
                "0 0 0 0 0 0 0.0001 0 0 0\n"+
                width +" "+height+" 0 0 0 0 0.0001 0 0 0\n"+
                printedBorders +
                particles.stream()
                        .map(Particle::toString)
                        .collect(Collectors.joining("\n")) +"\n";
    }

    private String printSiloBorders() {
        StringBuffer sb = new StringBuffer();
        String format = "%.6f %.6f 0 0 0 0 %.6f 1 0 0";
        double radius = .25;

        double y1 = room.getHeight()/2. + room.getStreet2With()/2.;
        double y2 = room.getHeight()/2. - room.getStreet2With()/2.;

        //------ p1
        //------ p8
        for(double x = 0; x < room.getWidth()/2. - room.getStreet1With()/2.; x+=radius){
            sb.append(String.format(format, x, y1, radius)+"\n");
            sb.append(String.format(format, x, y2, radius)+"\n");
        }

        //------ p4
        //------ p5
        for(double x = room.getWidth()/2. + room.getStreet1With()/2.;
            x < room.getWidth(); x+=radius){
            sb.append(String.format(format, x, y1, radius)+"\n");
            sb.append(String.format(format, x, y2, radius)+"\n");
        }

        double x1 = room.getWidth()/2. + room.getStreet1With()/2.;
        double x2 = room.getWidth()/2. - room.getStreet1With()/2.;


        //| |
        //| |
        //| |
        //p2 p3
        for(double y = room.getHeight()/2.+ room.getStreet2With()/2.;
            y < room.getHeight(); y+=radius){
            sb.append(String.format(format, x1, y, radius)+"\n");
            sb.append(String.format(format, x2, y, radius)+"\n");
        }

        //| |
        //| |
        //| |
        //p7 p6
        for(double y = 0;
            y < room.getHeight()/2. - room.getStreet2With()/2.; y+=radius){
            sb.append(String.format(format, x1, y, radius)+"\n");
            sb.append(String.format(format, x2, y, radius)+"\n");
        }

        return sb.toString();
    }

    private void printStringToFile(String filename, String content){
        try {
            Files.write(Paths.get(filename), content.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            try {
                new File(filename).createNewFile();
                Files.write(Paths.get(filename), content.getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e1) {
                System.out.println("No se pudo crear el archivo "+filename);
            }
        }
    }

}
