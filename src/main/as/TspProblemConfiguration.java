package main.as;

import isula.aco.ConfigurationProvider;
import isula.tsp.AntForTsp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by kevinhung on 2017/4/10.
 */


/**
 * This class contains the algorithm configuration of the Ant System algorithm described in
 * Section 6.3 of the Clever Algorithms book by Jason Brownlee.
 */
public class TspProblemConfiguration implements ConfigurationProvider {

    private double initialPheromoneValue;


    /**
     * In the algorithm described in the book, the initial pheromone value was a function of the quality of a
     * random solution. That logic is included in this constructor.
     *
     * @param problemRepresentation TSP coordinate information.
     */
    public TspProblemConfiguration(double[][] problemRepresentation) {
        List<Integer> randomSolution = new ArrayList<>();
        int numberOfCities = problemRepresentation.length;

        for (int cityIndex = 0; cityIndex < numberOfCities; cityIndex += 1) {
            randomSolution.add(cityIndex);
        }

        Collections.shuffle(randomSolution);

        double randomQuality = AntForTsp.getTotalDistance(
                randomSolution.toArray(new Integer[randomSolution.size()]),
                problemRepresentation);
        this.initialPheromoneValue = numberOfCities / randomQuality;
    }

    @Override
    public int getNumberOfAnts() {
        return 30;
    } //30

    @Override
    public double getEvaporationRatio() {
        return 1 - 0.6;
    }

    @Override
    public int getNumberOfIterations() {
        return 50;
    } //50

    @Override
    public double getInitialPheromoneValue() {
        return this.initialPheromoneValue;
    }

    @Override
    public double getHeuristicImportance() {
        return 2.5;
    }

    @Override
    public double getPheromoneImportance() {
        return 1.0;
    }
}
