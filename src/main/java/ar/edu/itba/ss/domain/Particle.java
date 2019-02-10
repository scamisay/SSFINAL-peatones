package ar.edu.itba.ss.domain;

import ar.edu.itba.ss.algorithm.cim.Cell;
import ar.edu.itba.ss.algorithm.cim.Range;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.pow;
import static java.util.stream.Collectors.toList;

public class Particle {
    Vector2D position;
    private Vector2D velocity;
    private Vector2D force;
    private double mass;
    private double radius;
    private Cell cell;
    private List<Particle> neighbours = new ArrayList<>();
    private Vector2D lastForce;
    private Vector2D prevLastForce = new Vector2D(0,0);
    private Vector2D lastPosition;
    private Vector2D predVelocity = new Vector2D(0,0);
    private static int idCounter=0;
    private int id;
    private boolean first = true;
    boolean isWall=false;
    private Vector2D target;

    public static final double G = 9.80665;// 9.80665 m/s2
    private boolean active = true;

    public Particle(Vector2D position, double mass, double radius, Vector2D target) {
        this.position = position;
        this.mass = mass;
        this.radius = radius;
        this.target = target;
        initParticle();
    }

    public Particle(Vector2D position, Particle particle) {
        this.position = position;
        this.velocity = particle.getVelocity();
        this.force = particle.getForce();
        this.mass = particle.getMass();
        this.radius = particle.getRadius()-0.01;
        this.target = particle.getTarget();
    }

    private void initParticle(){
        this.velocity = new Vector2D(0,0);
        this.force = new Vector2D(0,0);
        lastForce = force;
        lastPosition = position;
        id = idCounter;
        Particle.idCounter++;
        active = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Particle particle = (Particle) o;
        return id == particle.id;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }

    public Vector2D getTarget() {
        return target;
    }

    /***
     * getters and setters start
     */



    public Cell getCell() {
        return cell;
    }

    public int getId() {
        return id;
    }

    public void setCell(Cell cell) {
        this.cell = cell;
    }

    public double getRadius() {
        return radius;
    }

    public Vector2D getLastPosition() {
        return lastPosition;
    }

    public Vector2D getPosition() {
        return position;
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    public Vector2D getForce() {
        return force;
    }

    public double getMass() {
        return mass;
    }

    public List<Particle> getNeighbours() {
        return neighbours;
    }

    public void setForce(Vector2D force) {
        this.force = force;
    }

    public void setVelocity(Vector2D velocity) {
        this.velocity = velocity;
    }

    public void setLastPosition(Vector2D lastPosition) {
        this.lastPosition = lastPosition;
    }

    /***
     * getters and setters end
     */



    public boolean isCloseEnough(Particle particle, double maxDistance) {
        return distanceBorderToBorder(particle) <= maxDistance;
    }

    public Double distanceBorderToBorder(Particle particle){
        return distanceCenterToCenter(particle) - (getRadius() + particle.getRadius());
    }

    private double distanceCenterToCenter(Particle particle) {
        double difx = getPosition().getX() - particle.getPosition().getX();
        double dify = getPosition().getY() - particle.getPosition().getY();
        return Math.sqrt(pow(difx, 2) + pow(dify, 2));
    }

    public boolean isNeighbourCloseEnough(Particle particle, double maxDistance, boolean periodicContourCondition) {
        if (periodicContourCondition) {
            List<Cell> calculated = getCell().calculateNeighbourCells();
            if (!calculated.contains(particle.getCell())) {
                //debo dar la vuelta

                //defino las direcciones en cada una de las componentes
                double newX = getNewX(calculated,particle);
                double newY = getNewY(calculated,particle);



                Particle newParticle = new Particle(new Vector2D(newX, newY), particle);
                return distanceCenterToCenter(newParticle) <= maxDistance;
            }
        }
        return isCloseEnough(particle, maxDistance);
    }

    private double getNewX(List<Cell> calculated, Particle particle) {
        if (!hasRangeX(calculated, particle.getCell().getRangeX())) {
            if (getPosition().getX() - particle.getPosition().getX() > 0) {
                return particle.getPosition().getX() + getCell().getRangeX().getHighest();
            } else {
                return particle.getPosition().getX() - getCell().getRangeX().getHighest();
            }
        }
        return particle.getPosition().getX();
    }
    private double getNewY(List<Cell> calculated, Particle particle) {
        if (!hasRangeY(calculated, particle.getCell().getRangeY())) {
            if (getPosition().getY() - particle.getPosition().getY() > 0) {
                return particle.getPosition().getY() + getCell().getRangeY().getHighest();
            } else {
                return particle.getPosition().getY() - getCell().getRangeY().getHighest();
            }
        }
        return particle.getPosition().getY();

    }

    private boolean hasRangeX(List<Cell> calculated, Range rangex) {
        for (Cell c : calculated) {
            if (c != null && c.getRangeX().equals(rangex)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasRangeY(List<Cell> calculated, Range rangey) {
        for (Cell c : calculated) {
            try {
                if (c != null && c.getRangeY().equals(rangey)) {
                    return true;
                }
            } catch (Exception e) {
                System.out.print(1);
            }

        }
        return false;
    }

    public List<Particle> getOtherParticlesInCell() {
        return getCell().getParticles().stream().filter(p -> !p.equals(this)).collect(toList());
    }

    public void addNeighbour(Particle particle) {
        if (particle == null) {
            throw new IllegalArgumentException("La particula no puede ser nula");
        }
        neighbours.add(particle);
    }

    public double overlap(Particle other) {
        return getPosition().distance(other.getPosition()) - (getRadius() + other.getRadius());
    }

    public double previusOverlap(Particle other) {
        return getLastPosition().distance(other.getLastPosition()) - (getRadius() + other.getRadius());
    }

    private double overlapDerivate(Particle p, double dt) {
        return overlap(p) - previusOverlap(p);
    }

    @Override
    public String toString() {
        return String.format(Locale.US,"%.6f %.6f %.6f %.6f %.6f %.6f %.6f %.6f 1 1",
                position.getX(), position.getY(),
                velocity.getX(), velocity.getY(),
                force.getX(),force.getY(),
                radius,
                velocity.getNorm());
    }

    private Particle createMirroredParticle() {
        Vector2D influence = force.equals(force.getZero())? force: force.normalize();
        //Vector2D mirroredPos = position.add(influence.scalarMultiply(-overlapWithAWall));
        Vector2D mirroredPos = new Vector2D(position.getX(),4.98 /*Bottom padding, paja pasarlo como parametro*/);
        //Vector2D mirroredPosPrev = lastPosition.add(influence.scalarMultiply(overlapWithAWall));

        Particle mirrored = new Particle(mirroredPos, this);

        //cambio el sentido de las fuerzas para la particula espejada
        //mirrored.setForce(force.negate());
        //mirrored.setVelocity(velocity.negate());
        //mirrored.setLastPosition(mirroredPos);

        mirrored.setForce(new Vector2D(0,0));
        mirrored.setVelocity(new Vector2D(0,0));
        mirrored.setLastPosition(new Vector2D(0,0));
        mirrored.isWall=true;
        return mirrored;
    }

    private boolean overlapWithAWall(Vector2D particlePosition, double particleRadius, Room room) {
        //si esta afuera del room o a la altura de la apertura no considero el overlap
        if(/*!room.containsParticle(particlePosition) ||*/ room.isInExitArea(particlePosition.getX())){
            return false;
        }

        Vector2D wall =
                //new Vector2D(room.getLeftWall(), particlePosition.getY()),
                //new Vector2D(room.getRightWall(), particlePosition.getY()),
                new Vector2D(particlePosition.getX(), room.getBottomPadding())
                //new Vector2D(particlePosition.getX(), room.getHeight()+room.getBottomPadding())
        ;

        return particlePosition.distance(wall) - particleRadius < 0;
        /*return walls.stream()
                .mapToDouble( w ->  particlePosition.distance(w) - particleRadius )
                .min().;*/
    }

    private Vector2D getNormalVersor(Particle p) {
        return p.getPosition().subtract(position).normalize();
    }

    public void resetNeighbours() {
        neighbours = new ArrayList<>();
    }

    public double getKineticEnergy(){
        if(Double.isNaN(velocity.getY()) || Double.isNaN(velocity.getX() )){
            return 0;
        }
        return 0.5*mass*velocity.getNormSq();
    }

    public void updateVelocityLF(double dt) {
        velocity = velocity.add(force.scalarMultiply(dt/(3.0*mass))).add(lastForce.scalarMultiply(5.0*dt/(6.0*mass))).
              subtract(prevLastForce.scalarMultiply(dt/(6.0*mass)));
    }

    public void predictVelocity(double dt) {
                predVelocity = velocity.add(force.scalarMultiply((3.0/2) *dt /mass)).subtract(lastForce.scalarMultiply((1.0/2) *dt /mass));

    }

    public void updatePositionLF(double dt) {
        lastPosition = position;
        position = position.add(velocity.scalarMultiply(dt)).add(force.scalarMultiply((2.0/3)*FastMath.pow(dt,2)/mass ))
                       .subtract(lastForce.scalarMultiply((1.0/6)*FastMath.pow(dt,2)/mass));
    }


    public void calculateForceLF(double kN, double gamma, Room room, Double A, Double B,
                                 Double drivenVelocity, Double tau, double dt) {
        /**
         * calculo las particulas que estan en colision
         */
                if (!first) {
                        prevLastForce=lastForce;
                    }
                else {
                        first=false;
                    }
                lastForce = force;
        Set<Particle> collisionsWithParticles = new HashSet<>();

        Vector2D granularForce = new Vector2D(0,0);

        collisionsWithParticles = getNeighbours().stream()
                .distinct()
                .collect(Collectors.toSet());

        if(overlapWithAWall (getPosition(),getRadius(), room) ){
                Particle opositeParticle = createMirroredParticle();
                collisionsWithParticles.add(opositeParticle);
        }
        if(overlapWithLeft (getPosition(),getRadius(), room) ){
            Particle opositeParticle = createLeftMirroredParticle(room);
            collisionsWithParticles.add(opositeParticle);
        }
        if(overlapWithRight (getPosition(),getRadius(), room) ){
            Particle opositeParticle = createRightMirroredParticle(room);
            collisionsWithParticles.add(opositeParticle);
        }
        /**
         * calculo de fuerzas
         */
        granularForce = granularForce.add(calculateGranularForce(collisionsWithParticles, kN, gamma, dt));
        Vector2D socialForce = calculateSocialForce(collisionsWithParticles, A, B);
        Vector2D drivenForce = calculateDrivenForce(drivenVelocity, tau);

        /**
         * sumo todas las fuerzas
         */
        force = granularForce.add(socialForce).add(drivenForce);
    }

    private Particle createLeftMirroredParticle(Room room) {
        Vector2D influence = force.equals(force.getZero())? force: force.normalize();
        //Vector2D mirroredPos = position.add(influence.scalarMultiply(-overlapWithAWall));
        Vector2D mirroredPos = new Vector2D(room.getLeftWall()-0.02,position.getY() /*Bottom padding, paja pasarlo como parametro*/);
        //Vector2D mirroredPosPrev = lastPosition.add(influence.scalarMultiply(overlapWithAWall));

        Particle mirrored = new Particle(mirroredPos, this);

        //cambio el sentido de las fuerzas para la particula espejada
        //mirrored.setForce(force.negate());
        //mirrored.setVelocity(velocity.negate());
        //mirrored.setLastPosition(mirroredPos);

        mirrored.setForce(new Vector2D(0,0));
        mirrored.setVelocity(new Vector2D(0,0));
        mirrored.setLastPosition(new Vector2D(0,0));
        mirrored.isWall=true;
        return mirrored;
    }

    private Particle createRightMirroredParticle(Room room) {
        Vector2D influence = force.equals(force.getZero())? force: force.normalize();
        //Vector2D mirroredPos = position.add(influence.scalarMultiply(-overlapWithAWall));
        Vector2D mirroredPos = new Vector2D(room.getRightWall()+0.02,position.getY() /*Bottom padding, paja pasarlo como parametro*/);
        //Vector2D mirroredPosPrev = lastPosition.add(influence.scalarMultiply(overlapWithAWall));

        Particle mirrored = new Particle(mirroredPos, this);

        //cambio el sentido de las fuerzas para la particula espejada
        //mirrored.setForce(force.negate());
        //mirrored.setVelocity(velocity.negate());
        //mirrored.setLastPosition(mirroredPos);

        mirrored.setForce(new Vector2D(0,0));
        mirrored.setVelocity(new Vector2D(0,0));
        mirrored.setLastPosition(new Vector2D(0,0));
        mirrored.isWall=true;
        return mirrored;
    }

    private boolean overlapWithLeft(Vector2D particlePosition, double particleRadius, Room room) {
        //si esta afuera del room o a la altura de la apertura no considero el overlap
        if(/*!room.containsParticle(particlePosition) ||*/ room.isInExitArea(particlePosition.getX())){
            return false;
        }

        Vector2D wall =
                //new Vector2D(room.getLeftWall(), particlePosition.getY()),
                //new Vector2D(room.getRightWall(), particlePosition.getY()),
                new Vector2D(room.getLeftWall(), particlePosition.getY())
                //new Vector2D(particlePosition.getX(), room.getHeight()+room.getBottomPadding())
                ;

        return particlePosition.distance(wall) - particleRadius < 0;
        /*return walls.stream()
                .mapToDouble( w ->  particlePosition.distance(w) - particleRadius )
                .min().;*/
    }
    private boolean overlapWithRight(Vector2D particlePosition, double particleRadius, Room room) {
        //si esta afuera del room o a la altura de la apertura no considero el overlap
        if(/*!room.containsParticle(particlePosition) ||*/ room.isInExitArea(particlePosition.getX())){
            return false;
        }

        Vector2D wall =
                //new Vector2D(room.getLeftWall(), particlePosition.getY()),
                //new Vector2D(room.getRightWall(), particlePosition.getY()),
                new Vector2D(room.getRightWall(), particlePosition.getY())
                //new Vector2D(particlePosition.getX(), room.getHeight()+room.getBottomPadding())
                ;

        return particlePosition.distance(wall) - particleRadius < 0;
        /*return walls.stream()
                .mapToDouble( w ->  particlePosition.distance(w) - particleRadius )
                .min().;*/
    }

    private boolean isOverlappedWithTarget(Vector2D target) {
        return ( getPosition().distance(target) - getRadius() ) < 0;
    }

    private boolean isBouncing(Particle p) {
        return overlap(p) < previusOverlap(p);
    }

    private Vector2D calculateDrivenForce(Double drivenVelocity, Double tau) {
        Vector2D e_target = getTarget().subtract(position).normalize();
        return e_target.scalarMultiply(drivenVelocity)
                .subtract(predVelocity)
                .scalarMultiply(mass/tau);
    }

    private Vector2D calculateSocialForce(Set<Particle> collisionsWithParticles, Double A, Double B) {
        return collisionsWithParticles.stream()
                .filter(p->overlap(p)>0)
                .filter(p->p.isWall==false)
                .map( p -> getNormalVersor(p).scalarMultiply(-A*Math.exp(-overlap(p)/B)))
                .reduce( (v1,v2) -> v1.add(v2)).orElse(new Vector2D(0,0));
    }

    private Vector2D calculateGranularForce(Set<Particle> collisionsWithParticles, double kN, double gamma, double dt) {
        return collisionsWithParticles.stream()
                .map(p -> getNormalForce(p,kN,gamma, dt)
                ).reduce((v1,v2)->v1.add(v2)).orElse(new Vector2D(0,0));
    }

    private Vector2D getNormalForce(Particle p, double kN, double gamma, double dt) {
        if (overlap(p)>0)
            return new Vector2D(0,0);
        return getNormalVersor(p).scalarMultiply(kN*overlap(p)-gamma*overlapDerivate(p,dt));
    }

    public boolean isOverlapped(Particle p) {
        return overlap(p) < 0;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

