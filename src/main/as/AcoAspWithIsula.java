package main.as;

import isula.aco.*;
import isula.aco.algorithms.antsystem.*;
import isula.aco.exception.InvalidInputException;
import isula.asp.AntForAsp;
import isula.asp.AspEnvironment;

import javax.naming.ConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by kevinhung on 2017/4/18.
 *
 *
 * buildSolutions has been override for antColony
 *
 */
public class AcoAspWithIsula {
    private static Logger logger = Logger.getLogger(AcoTspWithIsula.class.getName());
    private static final String LOG_PROP = "/Users/kevinhung/IdeaProjects/isula/resources/log.properties";

    public static void main(String[] args) throws IOException, InvalidInputException, ConfigurationException {
        // implement customized logging properties for different logging situation
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(LOG_PROP));
            LogManager.getLogManager().readConfiguration(is);
            is.close();
        } catch (IOException e) {
            logger.log(Level.FINE, "讀取 " + LOG_PROP + " 檔案失敗.", e);
            System.exit(0);
        }

        // Algorithm start from here
        String fileName = "/Users/kevinhung/IdeaProjects/isula/resources/test4.asp";
        logger.info("fileName : " + fileName);
        double[][] problemRepresentation = getRepresentationFromFile(fileName);
        System.out.println(Arrays.deepToString(problemRepresentation));

        AspProblemConfiguration configurationProvider = new AspProblemConfiguration(problemRepresentation);
        AntColony<Integer, AspEnvironment> colony = getAntColony(configurationProvider);
        AspEnvironment environment = new AspEnvironment(problemRepresentation, configurationProvider);

        AcoProblemSolver<Integer, AspEnvironment> solver = new AcoProblemSolver<>();
        solver.initialize(environment, colony, configurationProvider);

        solver.addDaemonActions(new StartPheromoneMatrix<>(), new PerformEvaporation<>(), getPheromoneUpdatePolicy());
        solver.getAntColony().addAntPolicies(new RandomNodeSelection<>(), new NodeMergingSelection<>());

        solver.solveProblem();
        System.out.println(solver.getBestLayerThicknessMap());
        System.out.println(Arrays.toString(solver.getEnvironment().getPheromoneMatrix()[0]));



    }

    /**
     * Produces an Ant Colony instance for the ASP problem.
     *
     * @param configurationProvider Algorithm configuration.
     * @return Ant Colony instance.
     */
    public static AntColony<Integer, AspEnvironment> getAntColony(final ConfigurationProvider configurationProvider) {
        return new AntColony<Integer, AspEnvironment>(configurationProvider.getNumberOfAnts()) {
            @Override
            protected Ant<Integer, AspEnvironment> createAnt(AspEnvironment environment) {
                int initialReference = new Random().nextInt(environment.getNumberOfCities());
                return new AntForAsp(environment.getNumberOfCities(), environment.getProblemGraph());
            }

            @Override
            public void buildSolutions(AspEnvironment environment, ConfigurationProvider configurationProvider) {
                logger.log(Level.FINE, "BUILDING ANT SOLUTIONS");
                int antCounter = 0;

                if (getHive().size() == 0) {
                    throw new isula.aco.exception.ConfigurationException(
                            "Your colony is empty: You have no ants to solve the problem. "
                                    + "Have you called the buildColony() method?");
                }

                for (Ant<Integer, AspEnvironment> ant : getHive()) {
                    logger.fine("Current ant: " + antCounter);

                    // TODO ant.isSolutionReady
                    while (!ant.isSolutionReady(environment)) {
                        ant.selectNextNode(environment, configurationProvider);
                        ant.selectMergingNode(environment, configurationProvider);
                    }

//                    ant.doAfterSolutionIsReady(environment, configurationProvider);

                    logger.log(Level.FINE,
                            "Solution is ready > Cost: " + ant.getSolutionCost(environment)
                                    + ", Solution: " + ant.getSolutionAsString());

                    antCounter++;
                }
            }
        };
    }

    /**
     * On ASP, the pheromone value update procedure depends on the visual quality of the minimum thickness of layers.
     *
     * @return A daemon action that implements this procedure.
     */
    private static DaemonAction<Integer, AspEnvironment> getPheromoneUpdatePolicy() {
        return new OfflinePheromoneUpdate<Integer, AspEnvironment>() {
            @Override
            protected double getNewPheromoneValue(Ant<Integer, AspEnvironment> ant,
                                                  Integer positionInSolution,
                                                  Integer solutionComponent,
                                                  AspEnvironment environment,
                                                  ConfigurationProvider configurationProvider) {
                Double contribution = 1 / (1 - ant.getTotalVisualQuality());
                return ant.getPheromoneTrailValue(solutionComponent, positionInSolution, environment) + contribution;
            }
        };
    }


    public static double[][] getRepresentationFromFile(String fileName) throws IOException {
        List<Double> xCoordinates = new ArrayList<>();
        List<Double> yCoordinates = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(" ");

                if (tokens.length == 3) {
                    xCoordinates.add(Double.parseDouble(tokens[1]));
                    yCoordinates.add(Double.parseDouble(tokens[2]));
                }
            }
        }

        double[][] representation = new double[xCoordinates.size()][2];
        for (int index = 0; index < xCoordinates.size(); index += 1) {
            representation[index][0] = xCoordinates.get(index);
            representation[index][1] = yCoordinates.get(index);

        }

        return representation;
    }

}
