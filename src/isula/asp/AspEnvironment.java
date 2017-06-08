package isula.asp;

import isula.aco.Environment;
import isula.aco.exception.InvalidInputException;
import isula.tsp.TspEnvironment;
import main.as.AspProblemConfiguration;

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
    private final double initialVisualQuality;
    private final double minThickness;
    private final double maxThickness;




    /**
     * Creates an Environment for the Ants to traverse.
     *
     * @param problemGraph Graph representation of the problem to be solved.
     * @throws InvalidInputException When the problem graph is incorrectly formed.
     */
    public AspEnvironment(double[][] problemGraph, AspProblemConfiguration configurationProvider) throws InvalidInputException {
        super(problemGraph);
        this.numberOfCities = problemGraph.length;
        this.threshold = configurationProvider.getThreshold();
        this.initialVisualQuality = configurationProvider.getInitialVisualQuality();
        this.minThickness = configurationProvider.getMinLayerThickness();
        this.maxThickness = configurationProvider.getMaxLayerThickness();
        logger.info("Number of cities: " + numberOfCities);
    }

    public int getNumberOfCities() {
        return getProblemGraph().length;
    }

    public double getThreshold() {
        return threshold;
    }

    public double getMinThickness() {
        return minThickness;
    }

    public double getMaxThickness() {
        return maxThickness;
    }

    public double getInitialVisualQuality() {
        return initialVisualQuality;
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
