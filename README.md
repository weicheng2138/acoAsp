# Isula: A Framework for ACO and Adaptive Slcing

Isula allows an easy implementation of Ant-Colony Optimization algorithms using the Java Programming Language. It contains the common elements present in the meta-heuristic to allow algorithm designers the reutilization of common behaviors. With Isula, solving optimization problems with Ant Colony can be done in few lines of code.

Isula in Action
---------------
If you are not familiar with the framework, a good place to start is the classic Travelling Salesman Problem:
* The Travelling Salesman Problem, using Ant System: https://github.com/cptanalatriste/main.java.aco-tsp
* The Travelling Salesman Problem, using Ant Colony System: https://github.com/cptanalatriste/main.java.aco-acs-tsp


Here are some advanced examples of optimization problems solved with Isula-based algorithms:
* The Flow-Shop Scheduling  Problem, using Max-Min Ant System: https://github.com/cptanalatriste/main.java.aco-flowshop
* Binary Image Segmentation using Ant System: https://github.com/cptanalatriste/main.java.aco-image-thresholding
* Image Clustering using Max-Min Ant System: https://github.com/cptanalatriste/main.java.aco-image-segmentation

An Isula Primer
---------------
To solve a problem with an Ant-Colony Optimization algorithm, you need a Colony of Agents (a.k.a Ants), a graph representing the problem, and a pheromone data-structure to allow communication between this agents. Isula tries to emulate that pattern:

```java
    TspProblemConfiguration configurationProvider = new TspProblemConfiguration(problemRepresentation);
    AntColony<Integer, TspEnvironment> colony = getAntColony(configurationProvider);
    TspEnvironment environment = new TspEnvironment(problemRepresentation);

    AcoProblemSolver<Integer, TspEnvironment> solver = new AcoProblemSolver<>();
    solver.initialize(environment, colony, configurationProvider);
    solver.addDaemonActions(new StartPheromoneMatrix<Integer, TspEnvironment>(),
            new PerformEvaporation<Integer, TspEnvironment>());

    solver.addDaemonActions(getPheromoneUpdatePolicy());

    solver.getAntColony().addAntPolicies(new RandomNodeSelection<Integer, TspEnvironment>());
    solver.solveProblem();
```
That's a snippet from the Travelling Salesman Problem solution. Some things to notice there:
* Problem and algorithm configuration is provided by `ConfigurationProvider` instances. Make your own with the values you need.
* The class that does everything is `AcoProblemSolver`. In this case, we're using the same one provided by the framework but you can extend it to suit your needs.
* The Problem Solver needs an Environment that manages the problem graph and the pheromone matrix. You need to extend the `Environment` class provided with the framework to adjust it to support your problem.
* And we need an Ant Colony, of course. The Ant Colony main responsibility is to create Ants, and make them built solutions in iterations. The robust base `AntColony` class makes implementing this very easy.
* The hearth of the algorithm is the `Ant` class. You will need to define an Ant that suits your needs.
* Isula supports daemon actions -global behaviors- and ant-level policies, such as the ones present in multiple ACO Algorithms. You can add daemon actions to a solver via the `addDaemonActions` method and ant policies to a colony via the `addAntPolicies` method.
* Finaly, you call the `solveProblem()` method and wait for the best solution to be shown.

