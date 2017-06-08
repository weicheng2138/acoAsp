package isula.asp;

import isula.aco.Ant;
import isula.aco.AntPolicy;
import isula.aco.AntPolicyType;
import isula.aco.ConfigurationProvider;
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
//        for (Map.Entry<Integer, Integer> layerThickness : getLayerThicknessMap()
//                .entrySet()) {
//        }
//        System.out.println("isSolutionReady: ant next node*********************");
        int counterRef = 0;
        int counterRecord = 0;

        for (int i = 0; i < numberOfCities; i++) {
            for (int j = 0; j < numberOfCities; j++) {
                if (i != j) {
                    Integer layerThicknessA = getLayerThicknessMap().get(i);
                    Integer layerThicknessB = getLayerThicknessMap().get(j);
                    if (layerThicknessA > 0 && layerThicknessB > 0) {
                        counterRef++;
                        if (layerThicknessA + layerThicknessB > environment.getMaxThickness()) {
                            counterRecord++;
                        }
                    }
                }
            }
        }
//        System.out.println("counterRef -> " + counterRef);
//        System.out.println("counterRecord -> " + counterRecord);
//        System.out.println(counterRef == counterRecord);
//
//        System.out.println("isSolutionReady: Threshold -> " + ((1 - getTotalVisualQuality()) >= environment.getThreshold()));
//        System.out.println("isSolutionReady: counter -> " + (counterRef == counterRecord));

//        System.out.println(((environment.getInitialVisualQuality()-getTotalVisualQuality())/environment.getInitialVisualQuality()));
        return ((environment.getInitialVisualQuality()-getTotalVisualQuality())/environment.getInitialVisualQuality())
                >= environment.getThreshold()
                || counterRef == counterRecord;
    }


    /**
     * On TSP, the cost of a solution is the total distance traversed by the salesman.
     *
     * @param environment Environment instance with problem information.
     * @return Total distance.
     */
    @Override
    public double getSolutionCost(AspEnvironment environment) {
        double totalLayers = 0;

        Iterator<Map.Entry<Integer, Integer>> layerThicknessIterator = this.getLayerThicknessMap()
                .entrySet().iterator();
        while (layerThicknessIterator.hasNext()) {
            Map.Entry<Integer, Integer> componentWithlayerThickness = layerThicknessIterator
                    .next();
            Integer thickness = componentWithlayerThickness.getValue();

            if (thickness > 0) {
                totalLayers += 1;
            }

        }
        return totalLayers;
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
        return 1 / getVisualQualityArray()[solutionComponent]+ DELTA;
    }

    /**
     * Just retrieves a value from the pheromone matrix.
     *
     * @param solutionComponent  Solution component for solution array. (possibleMove)
     * @param positionInSolution Position of this component in the solution. (getAnt().getCurrentIndex())
     * @param environment        Environment instance with problem information.
     * @return pheromoneMatrix
     */
    @Override
    public Double getPheromoneTrailValue(Integer solutionComponent,
                                         Integer positionInSolution, AspEnvironment environment) {

//        Integer previousComponent = this.initialReference;
//        if (positionInSolution > 0) {
//            previousComponent = getSolution()[positionInSolution - 1];
//        }

        double[][] pheromoneMatrix = environment.getPheromoneMatrix();
        return pheromoneMatrix[0][solutionComponent];
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
     * On TSP, the neighbourhood is given by the non-visited cities.
     *
     * @param environment Environment instance with problem information.
     * @return List of neighbourhood
     */
    @Override
    public List<Integer> getMergingNeighbourhood(AspEnvironment environment) {
        List<Integer> neighbourhood = new ArrayList<>();

        // RandomNodeSelection has already increased the currentIndex
        Integer currentIndex = getCurrentIndex()-1;
        Integer currentLayerIndex = getSolution()[currentIndex];

        Integer upperTraceResult = upperTrace(currentLayerIndex, environment);
        Integer lowerTraceResult = lowerTrace(currentLayerIndex, environment);

//        System.out.println("getMergingNeighbourhood: currentIndex -> " + currentIndex);
//        System.out.println("getMergingNeighbourhood: currentLayerIndex -> " + currentLayerIndex);
//        System.out.println("getMergingNeighbourhood: upperTraceResult -> " + upperTraceResult);
//        System.out.println("getMergingNeighbourhood: lowerTraceResult -> " + lowerTraceResult);


        if (upperTraceResult != -1) {
            neighbourhood.add(upperTraceResult);
        }
        if (lowerTraceResult != -1) {
            neighbourhood.add(lowerTraceResult);
        }

//        System.out.println("getMergingNeighbourhood: neighbourhood size -> " + neighbourhood.size());
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
//        Integer previousComponent = this.initialReference;
//        if (positionInSolution > 0) {
//            previousComponent = getSolution()[positionInSolution - 1];
//        }

        double[][] pheromoneMatrix = environment.getPheromoneMatrix();
        pheromoneMatrix[0][solutionComponent] = value;
//        pheromoneMatrix[previousComponent][solutionComponent] = value;

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

        // Return false -> merge nothing; true -> merge one layer
        boolean policyResult = selectNodePolicy.applyPolicy(environment, configurationProvider);
//        if (!policyResult) {
//            throw new ConfigurationException("The node selection policy " + selectNodePolicy.getClass().getName() +
//                    " wasn't able to select a node to merge.");
//        }
    }

    /**
     * merge the layer we just choose
     *
     * @param mergingLayerIndex Node we choose to merge
     */
    public void mergeLayer(Integer mergingLayerIndex) {

        Integer currentIndex = getCurrentIndex()-1;
        Integer currentLayerIndex = getSolution()[currentIndex];

//        System.out.println("mergeLayer: mergingLayerIndex -> " + mergingLayerIndex);
//        System.out.println("mergeLayer: currentLayerIndex -> " + currentLayerIndex);

        // Modify LayerThicknessMap
        Integer preThickness = getLayerThicknessMap().put(currentLayerIndex, 0);
        Integer newThickness = getLayerThicknessMap().get(mergingLayerIndex) + preThickness;
        getLayerThicknessMap().put(mergingLayerIndex, newThickness);

        // Modify visualQualityArray: mergingNode Visual Quality & currentLayer Visual Quality
        double mergingLayerVQ = getVisualQualityArray()[mergingLayerIndex];
        double currentLayerVQ = getVisualQualityArray()[currentLayerIndex];
        if (mergingLayerVQ > currentLayerVQ) {
            getVisualQualityArray()[currentLayerIndex] = mergingLayerVQ;
        } else if (currentLayerVQ > mergingLayerVQ) {
            getVisualQualityArray()[mergingLayerIndex] = currentLayerVQ;
        }

//        System.out.println("mergeLayer: mergeResult -> " + getLayerThicknessMap().toString());

    }

    /**
     * Calculates the total visual quality.
     *
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

    /**
     * Trace upper through LayerThicknessMap util we get the layer is valid

     * @return Layer index which is valid.
     */
    private Integer upperTrace(Integer startIndex, AspEnvironment environment) {
        Integer traceResult = -1;
        Integer startIndexThickness = getLayerThicknessMap().get(startIndex);

        if (startIndex != 0) {
            for (int i = startIndex-1; i >= 0; i--) {
                if (getLayerThicknessMap().get(i) > 0 &&
                        startIndexThickness + getLayerThicknessMap().get(i) <= environment.getMaxThickness()){
                    traceResult = i;
                    break;
                } else if (getLayerThicknessMap().get(i) > 0 &&
                        startIndexThickness + getLayerThicknessMap().get(i) > environment.getMaxThickness()){
                    break;
                }
            }
        }
        return traceResult;
    }

    /**
     * Trace lower through LayerThicknessMap util we get the layer is valid

     * @return Layer index which is valid.
     */
    private Integer lowerTrace(Integer startIndex, AspEnvironment environment) {
        Integer traceResult = -1;
        Integer startIndexThickness = getLayerThicknessMap().get(startIndex);

        if (startIndex != numberOfCities-1){
            for (int i = startIndex+1; i < numberOfCities; i++) {
                if (getLayerThicknessMap().get(i) > 0 &&
                        startIndexThickness + getLayerThicknessMap().get(i) <= environment.getMaxThickness()){
                    traceResult = i;
                    break;
                } else if (getLayerThicknessMap().get(i) > 0 &&
                        startIndexThickness + getLayerThicknessMap().get(i) > environment.getMaxThickness()){
                    break;
                }
            }
        }
        return traceResult;
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
