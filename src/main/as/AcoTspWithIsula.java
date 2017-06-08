package main.as;

import isula.aco.*;
import isula.aco.algorithms.antsystem.OfflinePheromoneUpdate;
import isula.aco.algorithms.antsystem.PerformEvaporation;
import isula.aco.algorithms.antsystem.RandomNodeSelection;
import isula.aco.algorithms.antsystem.StartPheromoneMatrix;
import isula.aco.exception.InvalidInputException;
import isula.tsp.AntForTsp;
import isula.tsp.TspEnvironment;

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
 * Created by kevinhung on 2017/4/10.
 */
public class AcoTspWithIsula {
    private static Logger logger = Logger.getLogger(AcoTspWithIsula.class.getName());
    private static final String LOG_PROP = "/Users/kevinhung/IdeaProjects/isula/resources/log.properties";

    public static void main(String[] args) throws IOException, InvalidInputException, ConfigurationException {

        // implement customized logging properties for different logging situation
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(
                    LOG_PROP));
            LogManager.getLogManager().readConfiguration(is);
            is.close();
        } catch (IOException e) {
            logger.log(Level.FINE, "讀取 " + LOG_PROP + " 檔案失敗.", e);
            System.exit(0);
        }

        // Algorithm start from here
        String fileName = "/Users/kevinhung/IdeaProjects/isula/resources/berlin52.tsp";
        logger.info("fileName : " + fileName);

        double[][] problemRepresentation = getRepresentationFromFile(fileName);

        // produce an random initial pheromone for the current problem and provide every parameters
        TspProblemConfiguration configurationProvider = new TspProblemConfiguration(problemRepresentation);
        // give some config for AntColony and override function of createAnt to change the normal Ant to AntForTsp!!!
        AntColony<Integer, TspEnvironment> colony = getAntColony(configurationProvider);
        // TspEnv needs number of cities, problem graph(problemRepresentation) and pheromone matrix
        TspEnvironment environment = new TspEnvironment(problemRepresentation);

        AcoProblemSolver<Integer, TspEnvironment> solver = new AcoProblemSolver<>();
        solver.initialize(environment, colony, configurationProvider);

        solver.addDaemonActions(new StartPheromoneMatrix<>(), new PerformEvaporation<>(), getPheromoneUpdatePolicy());


//        solver.addDaemonActions(new StartPheromoneMatrix<>());
//        solver.addDaemonActions(new PerformEvaporation<>());
//        solver.addDaemonActions(getPheromoneUpdatePolicy());

        solver.getAntColony().addAntPolicies(new RandomNodeSelection<>());
        solver.solveProblem();

        System.out.println(Arrays.deepToString(solver.getEnvironment().getPheromoneMatrix()));




    }

    /**
     * Produces an Ant Colony instance for the TSP problem.
     *
     * @param configurationProvider Algorithm configuration.
     * @return Ant Colony instance.
     */
    public static AntColony<Integer, TspEnvironment> getAntColony(final ConfigurationProvider configurationProvider) {
        return new AntColony<Integer, TspEnvironment>(configurationProvider.getNumberOfAnts()) {
            @Override
            protected Ant<Integer, TspEnvironment> createAnt(TspEnvironment environment) {
                int initialReference = new Random().nextInt(environment.getNumberOfCities());
                return new AntForTsp(environment.getNumberOfCities());
            }
        };
    }

    /**
     * On TSP, the pheromone value update procedure depends on the distance of the generated routes.
     *
     * @return A daemon action that implements this procedure.
     */
    private static DaemonAction<Integer, TspEnvironment> getPheromoneUpdatePolicy() {
        return new OfflinePheromoneUpdate<Integer, TspEnvironment>() {
            @Override
            protected double getNewPheromoneValue(Ant<Integer, TspEnvironment> ant,
                                                  Integer positionInSolution,
                                                  Integer solutionComponent,
                                                  TspEnvironment environment,
                                                  ConfigurationProvider configurationProvider) {
                Double contribution = 1 / ant.getSolutionCost(environment);
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
