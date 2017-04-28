package main.as;

import isula.aco.ConfigurationProvider;

/**
 * Created by kevinhung on 2017/4/18.
 */
public class AspProblemConfiguration implements ConfigurationProvider {

    private double initialPheromoneValue;

    /**
     * In the algorithm described in the book, the initial pheromone value was a function of the quality of a
     * random solution. That logic is included in this constructor.
     *
     * @param problemRepresentation TSP coordinate information.
     */
    public AspProblemConfiguration(double[][] problemRepresentation) {
//        HashMap<Integer, Integer> initialLayerThicknessMap = new HashMap<>();
        int numberOfLayers = problemRepresentation.length;
//
//        for (int layerIndex = 0; layerIndex < numberOfLayers; layerIndex += 1) {
//            initialLayerThicknessMap.put(layerIndex, 1);
//        }

        double randomQuality = 0.0;
        for (int i = 0; i < numberOfLayers; i++) {
            randomQuality += problemRepresentation[i][1];
        }

        // TODO after making sure of calculation of VS, remember to deal with which need to be diameter
        this.initialPheromoneValue =  randomQuality / numberOfLayers;
    }


    @Override
    public int getNumberOfAnts() {
        return 1;
    }

    @Override
    public double getEvaporationRatio() {
        return 1-0.6;
    }

    @Override
    public int getNumberOfIterations() {
        return 1;
    }

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
        return 1;
    }

    /**
     * Maximum thickness for each layer
     */
    public double getMaxLayerThickness() { return 0.1; }

    /**
     * Minimum thickness for each layer
     */
    public double getMinLayerThickness() { return 0.01; }

    /**
     * A threshold for reducing the proportion of visual quality
     */
    public double getThreshold() { return 0.1; }
}
