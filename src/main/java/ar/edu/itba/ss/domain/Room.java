package ar.edu.itba.ss.domain;

import ar.edu.itba.ss.algorithm.ParticlesCreator;
import ar.edu.itba.ss.algorithm.cim.CellIndexMethod;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class Room {

    private static final int MAX_CREATION_TRIES = 1000;

    private final double width;
    private final double height;
    private final double street1With;
    private final double street2With;
    private final double exitOpeningSize;
    private Area insideSiloArea;
    private final double topPadding;
    private final double bottomPadding;
    private List<Particle> particles;
    private double kN = 1.2e5;//N/m.
    private double kT = 1e3;//N/m.
    private double gamma = 1e3;
    private double A = 2000;
    private double B = 0.08;
    private double TAU = 0.5;//s

    private double drivenVelocity;
    public Vector2D targetStreet1;
    public Vector2D targetStreet2;


    //Cota superior para M: L/(2 * rMax)/4 > M
    private static final int M = 4;

    // L > W > D
    public Room(double width, double height, double exitOpeningSize, double topPadding,
                double bottomPadding, double drivenVelocity, double street1With, double street2With) {
        this.width = width;
        this.height = height;
        this.exitOpeningSize = exitOpeningSize;
        this.topPadding = topPadding;
        this.bottomPadding = bottomPadding;
        particles = new ArrayList<>();
        insideSiloArea = new Area(0,bottomPadding+height,width,bottomPadding);
        this.drivenVelocity = drivenVelocity;
        targetStreet1 = new Vector2D(width/2, 0);
        targetStreet2 = new Vector2D(width, height/2);
        this.street1With = street1With;
        this.street2With = street2With;
    }

    public void fillStreet(int particleNumbersForStreet1, int particleNumbersForStreet2) {
        ParticlesCreator filler = new ParticlesCreator(insideSiloArea);
        for(int i = 0; i < particleNumbersForStreet1; i++){
            if(!addOne(filler, targetStreet1, true, street1With)){
                break;
            }
        }
        for(int i = 0; i < particleNumbersForStreet2; i++){
            if(!addOne(filler, targetStreet2, false, street2With)){
                break;
            }
        }
    }

    public double getExitStart(){
        return (width / 2) - (exitOpeningSize / 2);
    }

    public double getExitOpeningSize() {
        return exitOpeningSize;
    }

    public double getExitEnd(){
        return getExitStart() + exitOpeningSize;
    }

    public boolean isInExitArea(double x){
        double exitStart = getExitStart();
        double exitEnd = getExitEnd();
        return (exitStart <= x) && (x <= exitEnd);
    }

    private CellIndexMethod instantiateCIM(List<Particle> particles){
        return new CellIndexMethod(M, insideSiloArea.getHeight(),
                ParticlesCreator.MAX_RADIUS*2., particles, false);
    }

    private boolean addOne(ParticlesCreator filler, Vector2D target, boolean isStreet1, double streetWith){
        for(int intent = 1 ; intent <= MAX_CREATION_TRIES; intent++){

            List<Particle> pAux = new ArrayList<>(this.particles);
            Particle particle = filler.create(target, isStreet1, streetWith);
            pAux.add(particle);

            CellIndexMethod cim = instantiateCIM(pAux);
            cim.calculate();

            if(isThereRoomForParticle(particle)){
                addParticle(particle);
                return true;
            }
        }
        return false;
    }

    private boolean isThereRoomForParticle(Particle particle) {
        return particle.getNeighbours().stream()
                .noneMatch( p ->  particle.isOverlapped(p));
    }

    private void addParticle(Particle particle) {
        particles.add(particle);
    }

    public List<Particle> getParticles() {
        return particles;
    }

    public double getHeight() {
        return height;
    }

    public double getScenarioHeight(){
        return height + bottomPadding + topPadding;
    }

    public double getWidth() {
        return width;
    }

    public double getBottomPadding() {
        return bottomPadding;
    }

    public void evolveLeapFrog(double dt) {
        CellIndexMethod cim = instantiateCIM(particles);
        cim.calculate();
        particles.forEach( p -> p.updatePositionLF(dt));
        particles.forEach( p -> p.predictVelocity(dt));
        particles.forEach( p -> p.calculateForceLF(kN, gamma, this,A,B, drivenVelocity, TAU, dt));
        particles.forEach( p -> p.updateVelocityLF(dt));
    }

    public double getLeftWall() {
        return insideSiloArea.getMinX();
    }

    public double getRightWall() {
        return insideSiloArea.getWidth();
    }

    public boolean hasEscaped(Particle particle) {
        boolean ret = (particle.getPosition().getY() < getBottomPadding())
                &&
                (particle.getLastPosition().getY() >= getBottomPadding());
        if (ret) {
            particle.position = Vector2D.POSITIVE_INFINITY;
            //particles.remove(particle);
        }
        return ret;
    }

    public double getKineticEnergy() {
        return particles.stream().mapToDouble( p -> p.getKineticEnergy()).sum();
    }

    private List<Particle> particlesRecentlyFallen = new ArrayList<>();

    public long numberOfparticlesHaveEscaped() {
        Iterator<Particle> it = particlesRecentlyFallen.iterator();
        while(it.hasNext()){
            Particle particle = it.next();
            if(particle.getPosition().getY() > getBottomPadding()){
                it.remove();
            }
        }
        List<Particle> newFallen = particles.stream()
                .filter( p -> !particlesRecentlyFallen.contains(p))
                .filter( p -> p.getPosition().getY() < getBottomPadding())
                .collect(Collectors.toList());
        particlesRecentlyFallen.addAll(newFallen);
        return newFallen.size();
    }

    public List<Vector2D> getParticlesHaveJustEscaped(double dt) {
        return particles.stream()
                .filter(p->hasEscaped(p))
                .map(p-> new Vector2D(dt,p.getId()))
                .collect(Collectors.toList());
    }

    public boolean isSomeoneLeftToEscape() {
        return particles.stream().filter( p -> (p.getPosition().getY() + 4*p.getRadius()) >= getBottomPadding()).count() > 0;
    }

    public double getStreet1With() {
        return street1With;
    }

    public double getStreet2With() {
        return street2With;
    }

    public double getLeftStreet() {
        return getWidth()/2. - getStreet1With()/2.;
    }

    public double getRightStreet() {
        return getWidth()/2. + getStreet1With()/2.;
    }
}
