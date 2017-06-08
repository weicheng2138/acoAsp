package isula.aco.algorithms.antsystem;

import isula.aco.AntPolicy;
import isula.aco.AntPolicyType;
import isula.aco.ConfigurationProvider;
import isula.aco.Environment;
import isula.aco.exception.ConfigurationException;

import java.util.*;

/**
 * Created by kevinhung on 2017/4/21.
 */
public class NodeMergingSelection<C, E extends Environment>
        extends AntPolicy<C, E>{

    public NodeMergingSelection() {
        super(AntPolicyType.MERGING_SELECTION);
    }

    @Override
    public boolean applyPolicy(E environment, ConfigurationProvider configurationProvider) {
        Random random = new Random();
        C nextNode = null;

        // value will just between 0~1
        double value = random.nextDouble();
        double total = 0;

        // powerful usage of Hashmap
        HashMap<C, Double> componentsWithProbabilities = this
                .getComponentsWithProbabilities(environment, configurationProvider);

        if (componentsWithProbabilities.size() > 0) {
            Iterator<Map.Entry<C, Double>> componentWithProbabilitiesIterator = componentsWithProbabilities
                    .entrySet().iterator();
            while (componentWithProbabilitiesIterator.hasNext()) {
                Map.Entry<C, Double> componentWithProbability = componentWithProbabilitiesIterator
                        .next();

                Double probability = componentWithProbability.getValue();
                if (probability.isNaN()) {
                    throw new ConfigurationException("The probability for component " + componentWithProbability.getKey() +
                            " is not a number.");
                }

                total += probability;

                // Merge action is over here!!!
                if (total >= value) {
                    nextNode = componentWithProbability.getKey();
                    getAnt().mergeLayer((Integer) nextNode);

                    return true;
                }
            }
        }


        return false;
    }

    /**
     * Gets a probabilities vector, containing probabilities to move to each node
     * according to pheromone matrix.
     *
     * @param environment           Environment that ants are traversing.
     * @param configurationProvider Configuration provider.
     * @return Probabilities for the adjacent nodes.
     */
    public HashMap<C, Double> getComponentsWithProbabilities(E environment,
                                                             ConfigurationProvider configurationProvider) {
        HashMap<C, Double> componentsWithProbabilities = new HashMap<C, Double>();

//        System.out.println("getComponentsWithProbabilities: getVisualQualityArray -> " + Arrays.toString(getAnt().getVisualQualityArray()));
//        System.out.println("getComponentsWithProbabilities: getLayerThicknessMap -> " + getAnt().getLayerThicknessMap());


        double denominator = Double.MIN_VALUE;
        for (C possibleMove : getAnt().getMergingNeighbourhood(environment)) {
//            System.out.println("possibleMove -> " + possibleMove);

            // heuristicTimesPheromone -> { η*τ }
            Double heuristicTimesPheromone = getHeuristicTimesPheromone(
                    environment, configurationProvider, possibleMove);
            denominator += heuristicTimesPheromone;

            // giving hashmap with valid exact city index that may head to
            componentsWithProbabilities.put(possibleMove, 0.0);

        }
//        System.out.println("neighborhood number -> " + getAnt().getMergingNeighbourhood(environment).size());
//        System.out.println("denominator -> " + denominator);




        Double totalProbability = 0.0;
        Iterator<Map.Entry<C, Double>> componentWithProbabilitiesIterator = componentsWithProbabilities
                .entrySet().iterator();

//        System.out.println("componentsWithProbabilities size -> " + componentsWithProbabilities.size());
        while (componentWithProbabilitiesIterator.hasNext()) {
            Map.Entry<C, Double> componentWithProbability = componentWithProbabilitiesIterator
                    .next();
            C component = componentWithProbability.getKey();
//            System.out.println("component -> " + component);

            Double numerator = getHeuristicTimesPheromone(
                    environment, configurationProvider, component);

//            System.out.println("numerator -> " + numerator);
            Double probability = numerator / denominator;
//            System.out.println("-------------------------------------------------------" + probability);
            totalProbability += probability;


            componentWithProbability.setValue(probability);
        }

        // Cannot find any layer to merge
        if (componentsWithProbabilities.size() < 1) {
            return componentsWithProbabilities;
        }
        double delta = 0.001;
        if (Math.abs(totalProbability - 1.0) > delta) {
            throw new ConfigurationException("The sum of probabilities for the possible components is " +
                    totalProbability + ". We expect this value to be closer to 1.");
        }

        return componentsWithProbabilities;
    }


    private Double getHeuristicTimesPheromone(E environment,
                                              ConfigurationProvider configurationProvider, C possibleMove) {
        // heuristicValue is a distance between
        // the last city (component) of so far that ant go through (current solution)
        // and possibleMove
        // which means { 1/d } -> { η }
        Double heuristicValue = getAnt().getHeuristicValue(
                possibleMove,
                getAnt().getCurrentIndex(),
                environment
        );

        // pheromoneTrailValue: { τ }
        Double pheromoneTrailValue = getAnt().getPheromoneTrailValue(
                possibleMove,
                getAnt().getCurrentIndex(),
                environment);

        // one of the { η*τ }
        return Math.pow(heuristicValue,
                configurationProvider.getHeuristicImportance())
                * Math.pow(pheromoneTrailValue,
                configurationProvider.getPheromoneImportance());
    }
}
