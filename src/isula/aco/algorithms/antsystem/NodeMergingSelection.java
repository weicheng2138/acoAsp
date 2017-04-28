package isula.aco.algorithms.antsystem;

import isula.aco.AntPolicy;
import isula.aco.AntPolicyType;
import isula.aco.ConfigurationProvider;
import isula.aco.Environment;
import isula.aco.exception.ConfigurationException;
import isula.aco.exception.SolutionConstructionException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

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

            // in terms of accumulation of probability to achieve randomly choose one city
            if (total >= value) {
                nextNode = componentWithProbability.getKey();
                getAnt().visitNode(nextNode);

                return true;
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

        double denominator = Double.MIN_VALUE;
        // getAnt().getNeighbourhood(environment) directly gain the unvisited index of city
        for (C possibleMove : getAnt().getNeighbourhood(environment)) {

            if (!getAnt().isNodeVisited(possibleMove)
                    && getAnt().isNodeValid(possibleMove)) {

                // heuristicTimesPheromone -> { η*τ }
                Double heuristicTimesPheromone = getHeuristicTimesPheromone(
                        environment, configurationProvider, possibleMove);
                denominator += heuristicTimesPheromone;

                // giving hashmap with valid exact city index that may head to
                componentsWithProbabilities.put(possibleMove, 0.0);
            }
        }


        Double totalProbability = 0.0;
        Iterator<Map.Entry<C, Double>> componentWithProbabilitiesIterator = componentsWithProbabilities
                .entrySet().iterator();
        while (componentWithProbabilitiesIterator.hasNext()) {
            Map.Entry<C, Double> componentWithProbability = componentWithProbabilitiesIterator
                    .next();
            C component = componentWithProbability.getKey();

            Double numerator = getHeuristicTimesPheromone(
                    environment, configurationProvider, component);
            Double probability = numerator / denominator;
            totalProbability += probability;

            componentWithProbability.setValue(probability);
        }

        if (componentsWithProbabilities.size() < 1) {
            return doIfNoComponentsFound(environment, configurationProvider);
        }
        double delta = 0.001;
        if (Math.abs(totalProbability - 1.0) > delta) {
            throw new ConfigurationException("The sum of probabilities for the possible components is " +
                    totalProbability + ". We expect this value to be closer to 1.");
        }

        return componentsWithProbabilities;
    }


    protected HashMap<C, Double> doIfNoComponentsFound(E environment,
                                                       ConfigurationProvider configurationProvider) {
        throw new SolutionConstructionException(
                "We have no suitable components to add to the solution from current position."
                        + "\n Previous Component: "
                        + getAnt().getSolution()[getAnt().getCurrentIndex() - 1]
                        + " at position " + (getAnt().getCurrentIndex() - 1)
                        + "\n Environment: " + environment.toString()
                        + "\nPartial solution : " + getAnt().getSolutionAsString());
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
