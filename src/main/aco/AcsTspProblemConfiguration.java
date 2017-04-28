package main.aco;

import isula.aco.algorithms.acs.AcsConfigurationProvider;
import isula.tsp.AntForTsp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by kevinhung on 2017/4/10.
 */

/**
 * This class contains the algorithm configuration of the Ant Colony System algorithm described in
 * Section 6.4 of the Clever Algorithms book by Jason Brownlee.
 */
public class AcsTspProblemConfiguration implements AcsConfigurationProvider {
    private double initialPheromoneValue;


    /**
     * In the algorithm described in the book, the initial pheromone value was a function of the quality of a
     * random solution. That logic is included in this constructor.
     *
     * @param problemRepresentation TSP coordinate information.
     */
    public AcsTspProblemConfiguration(double[][] problemRepresentation) {
        List<Integer> randomSolution = new ArrayList<>();
        int numberOfCities = problemRepresentation.length;

        for (int cityIndex = 0; cityIndex < numberOfCities; cityIndex += 1) {
            randomSolution.add(cityIndex);
        }

        Collections.shuffle(randomSolution);

        double randomQuality = AntForTsp.getTotalDistance(
                randomSolution.toArray(new Integer[randomSolution.size()]),
                problemRepresentation);

        this.initialPheromoneValue = 1.0 / (numberOfCities * randomQuality);
    }

    /**
     * Returns the pheromone coefficient for the local pheromone update procedure.
     *
     * @return Local pheromone coefficient.
     */
    public double getLocalPheromoneCoefficient() {
        return 0.1;
    }

    @Override
    public int getNumberOfAnts() {
        return 1;
    } //10

    @Override
    public double getEvaporationRatio() {
        return 1 - 0.1;
    }

    @Override
    public int getNumberOfIterations() {
        return 1;
    } //100

    @Override
    public double getInitialPheromoneValue() {
        return initialPheromoneValue;
    }

    @Override
    public double getHeuristicImportance() {
        return 2.5;
    }

    @Override
    public double getPheromoneImportance() {
        return 1.0;
    }

    @Override
    public double getBestChoiceProbability() {
        return 0.9;
    }
}
