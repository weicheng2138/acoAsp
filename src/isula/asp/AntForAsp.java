package isula.asp;

import isula.aco.Ant;
import isula.aco.AntPolicy;
import isula.aco.AntPolicyType;
import isula.aco.ConfigurationProvider;
import isula.aco.exception.ConfigurationException;
import isula.tsp.AntForTsp;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by kevinhung on 2017/4/14.
 */
public class AntForAsp extends Ant<Integer, AspEnvironment> {
    private static Logger logger = Logger.getLogger(AntForTsp.class.getName());

    private static final double DELTA = Float.MIN_VALUE;
    private final int numberOfCities;
    private final double[][] problemGraphReference;
    private int initialReference;
    private Map<Integer, Integer> layerThicknessMap = new HashMap<>();
    private Integer[][] choosingReference;
    private double[] visualQualityArray;


    public AntForAsp(int numberOfCities, double[][] problemGraph) {
        super();
        this.numberOfCities = numberOfCities;
        this.setSolution(new Integer[numberOfCities]);
        this.choosingReference = new Integer[this.numberOfCities][2];
        this.problemGraphReference = problemGraph;
        this.visualQualityArray = new double[numberOfCities];


//        for (int i = 0; i < numberOfCities; i++) {
//            getLayerThicknessMap().put(i, 1);
//        }
    }


    @Override
    public void clear() {
        super.clear();
        getLayerThicknessMap().clear();
        for (int i = 0; i < numberOfCities; i++) {
            getLayerThicknessMap().put(i, 1);
        }

        for (int i = 0; i < numberOfCities; i++) {
            visualQualityArray[i] = this.problemGraphReference[i][1];
        }
        logger.finer("visualQualityArray: " + Arrays.toString(visualQualityArray));

        for (int i = 0; i < numberOfCities; i++) {
            getChoosingReference()[i] = null;
        }
        this.initialReference = new Random().nextInt(this.numberOfCities);
    }


    /**
     * On ASP, a solution is ready when the total visual quality reduce to a certain threshold.
     * The certain threshold is defined in configurationProvider
     *
     * @param environment Environment instance with problem information.
     * @return True if the solution is ready.
     */
    @Override
    public boolean isSolutionReady(AspEnvironment environment) {
        return getSolutionCost(environment) >= environment.getThreshold();
    }


    /**
     * On TSP, the cost of a solution is the total distance traversed by the salesman.
     *
     * @param environment Environment instance with problem information.
     * @return Total distance.
     */
    @Override
    public double getSolutionCost(AspEnvironment environment) {
        return 1 - getTotalVisualQuality();
    }


    /**
     * The heuristic contribution in ASP is related to the added visual quality given by selecting an specific component.
     * According to the algorithm, when the solution is empty we take a random city as a reference.
     *
     * @param solutionComponent  Solution component. (possibleMove)
     * @param positionInSolution Position of this component in the solution. (getAnt().getCurrentIndex())
     * @param environment        Environment instance with problem information.
     * @return Heuristic contribution.
     */
    @Override
    public Double getHeuristicValue(Integer solutionComponent, Integer positionInSolution, AspEnvironment environment) {
//        Integer lastComponent = this.initialReference;
//
//        // if this.getSolution.length > 0, change last component to the last component of this.getSolution
//        if (getCurrentIndex() > 0) {
//            lastComponent = this.getSolution()[getCurrentIndex() - 1];
//        }

//        System.out.println(solutionComponent + ": AntForAsp");
//        System.out.println(getVisualQuality(solutionComponent, environment.getProblemGraph()) + ": **AntForAsp");
        // TODO check for visual quality to be larger than 1 or not (not 1/distance right now -> return only distance)
        return getVisualQualityArray()[solutionComponent]+ DELTA;
    }

    /**
     * Just retrieves a value from the pheromone matrix.
     *
     * @param solutionComponent  Solution component. (possibleMove)
     * @param positionInSolution Position of this component in the solution. (getAnt().getCurrentIndex())
     * @param environment        Environment instance with problem information.
     * @return pheromoneMatrix
     */
    @Override
    public Double getPheromoneTrailValue(Integer solutionComponent,
                                         Integer positionInSolution, AspEnvironment environment) {

        Integer previousComponent = this.initialReference;
        if (positionInSolution > 0) {
            previousComponent = getSolution()[positionInSolution - 1];
        }

        double[][] pheromoneMatrix = environment.getPheromoneMatrix();


        return pheromoneMatrix[solutionComponent][previousComponent];
    }

    /**
     * On TSP, the neighbourhood is given by the non-visited cities.
     *
     * @param environment Environment instance with problem information.
     * @return List of neighbourhood
     */
    @Override
    public List<Integer> getNeighbourhood(AspEnvironment environment) {
        List<Integer> neighbourhood = new ArrayList<>();

        for (int cityIndex = 0; cityIndex < environment.getNumberOfCities(); cityIndex += 1) {
            if (!this.isNodeVisited(cityIndex)) {
                neighbourhood.add(cityIndex);
            }
        }

        return neighbourhood;
    }


    /**
     * Just updates the pheromone matrix.
     *
     * @param solutionComponent  Solution component.
     * @param positionInSolution Position of this component in the solution.
     * @param environment        Environment instance with problem information.
     * @param value              New pheromone value.
     */
    @Override
    public void setPheromoneTrailValue(Integer solutionComponent, Integer positionInSolution,
                                       AspEnvironment environment, Double value) {
        Integer previousComponent = this.initialReference;
        if (positionInSolution > 0) {
            previousComponent = getSolution()[positionInSolution - 1];
        }

        double[][] pheromoneMatrix = environment.getPheromoneMatrix();
        pheromoneMatrix[solutionComponent][previousComponent] = value;
        pheromoneMatrix[previousComponent][solutionComponent] = value;

    }

    /**
     * Selects a node and marks it as visited.
     *
     * @param environment           Environment where the ant is building a solution.
     * @param configurationProvider Configuration provider.
     */
    @Override
    public void selectMergingNode(AspEnvironment environment,
                               ConfigurationProvider configurationProvider) {

        AntPolicy<Integer, AspEnvironment> selectNodePolicy = getAntPolicy(
                AntPolicyType.MERGING_SELECTION, 1);

        // TODO(cgavidia): With this approach, the policy is a shared resource
        // between ants. This doesn't allow parallelism.
        selectNodePolicy.setAnt(this);
        boolean policyResult = selectNodePolicy.applyPolicy(environment, configurationProvider);
        if (!policyResult) {
            throw new ConfigurationException("The node selection policy " + selectNodePolicy.getClass().getName() +
                    " wasn't able to select a node to merge.");
        }
    }

    /**
     * merge the layer we just choose
     *
     * @param visitedNode Node we choose from Node Selection Policy
     * @param visitedLayer Layer we choose for the next merging
     */
    public void mergeLayer(Integer visitedNode, Integer visitedLayer) {

    }

//    /**
//     * Mark a node that has been chosen for the next merging.
//     *
//     * @param choosingNode chosen node.
//     */
//    public void visitChoosingNode(Integer choosingNode, int currentIndex) {
//
//
//        if (currentIndex < getChoosingRef().length) {
//
//            getChoosingRef()[currentIndex] = choosingNode;
//        } else {
//            throw new SolutionConstructionException("Couldn't add component "
//                    + choosingNode.toString() + " at index " + currentIndex
//                    + ": choosingRef length is: " + getChoosingRef().length
//                    + ". \nPartial choosingRef is " + getChoosingRefAsString());
//        }
//
//    }

    /**
     * Calculates the total visual quality.

     * @return Total visual quality.
     */
    public double getTotalVisualQuality() {
        double totalVisualQuality = 0.0;

        Iterator<Map.Entry<Integer, Integer>> layerThicknessIterator = this.getLayerThicknessMap()
                .entrySet().iterator();
        while (layerThicknessIterator.hasNext()) {
            Map.Entry<Integer, Integer> componentWithlayerThickness = layerThicknessIterator
                    .next();
            Integer index = componentWithlayerThickness.getKey();
            Integer thickness = componentWithlayerThickness.getValue();

            if (thickness > 0) {
                totalVisualQuality += this.getVisualQualityArray()[index];
            }

        }
        return totalVisualQuality;
    }



    public Map<Integer, Integer> getLayerThicknessMap() {
        return layerThicknessMap;
    }

    public Integer[][] getChoosingReference() {
        return choosingReference;
    }

    public double[] getVisualQualityArray() {
        return visualQualityArray;
    }
}
