package ar.edu.itba.ss.execution;

import ar.edu.itba.ss.algorithm.GranularSystem;
import ar.edu.itba.ss.domain.CommonScenario;

public class TestCommonScenario {

    public static void main(String[] args) {
        int numberOfParticles = 20;
        double drivenVelocity = 3;
        GranularSystem system = CommonScenario.simulate(numberOfParticles,numberOfParticles, drivenVelocity);
        System.out.println(system.getEgresos());
    }
}
