package isula.asp;

import isula.aco.Environment;
import isula.aco.exception.InvalidInputException;
import isula.tsp.TspEnvironment;

import java.util.logging.Logger;

/**
 * Created by kevinhung on 2017/4/14.
 *
 * AspEnvironment turns out to be Adaptive Slicing Problem
 */
public class AspEnvironment extends Environment {
    private static Logger logger = Logger.getLogger(TspEnvironment.class.getName());

    private final int numberOfCities;
    private final double threshold;



    /**
     * Creates an Environment for the Ants to traverse.
     *
     * @param problemGraph Graph representation of the problem to be solved.
     * @throws InvalidInputException When the problem graph is incorrectly formed.
     */
    public AspEnvironment(double[][] problemGraph, double threshold) throws InvalidInputException {
        super(problemGraph);
        this.numberOfCities = problemGraph.length;
        this.threshold = threshold;
        logger.info("Number of cities: " + numberOfCities);
    }

    public int getNumberOfCities() {
        return getProblemGraph().length;
    }

    public double getThreshold() {
        return threshold;
    }

    /**
     * The pheromone matrix in the TSP problem stores a pheromone value per city and per position of this city on
     * the route. That explains the dimensions selected for the pheromone matrix.
     *
     * @return Pheromone matrix instance.
     */
    @Override
    protected double[][] createPheromoneMatrix() {
        int numberOfCities = getNumberOfCities();
        return new double[numberOfCities][numberOfCities];
    }

}
