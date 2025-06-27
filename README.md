# JNeat: A Modern Java NEAT Implementation

JNeat is a flexible and well-documented implementation of the **NeuroEvolution of Augmenting Topologies (NEAT)** algorithm, written in modern Java. It provides a robust framework for evolving neural networks to solve a variety of problems, from simple benchmarks to complex control tasks and games.

This library is designed to be both a powerful tool for machine learning practitioners and an educational resource for those looking to understand the inner workings of NEAT. It features a clean separation of concerns, extensive documentation, and a rich suite of examples demonstrating its capabilities.

[![Java CI with Maven](https://github.com/your-username/JNeat/actions/workflows/maven.yml/badge.svg)](https://github.com/your-username/JNeat/actions/workflows/maven.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https/opensource.org/licenses/MIT)


## What is NEAT?

NeuroEvolution of Augmenting Topologies (NEAT) is an algorithm that evolves artificial neural networks using genetic algorithms. Unlike other neuro-evolution methods that only evolve connection weights, NEAT evolves both the weights and the structure (topology) of the networks.

It begins with a population of simple, minimal networks and gradually increases their complexity over generations by adding new nodes and connections. This process of "complexification" allows NEAT to find solutions in a much more efficient search space.

The core innovations of NEAT are:
1.  **Complexifying Genomes:** Starting simple and adding complexity only when necessary.
2.  **Speciation:** Protecting novel topologies by grouping similar genomes into "species," allowing them time to optimize before competing with the entire population.
3.  **Historical Markings (Innovation Numbers):** Tracking the historical origin of every gene to efficiently perform crossover between different topologies without expensive analysis.

This implementation is based on the principles outlined in the original paper:
> [Evolving Neural Networks through Augmenting Topologies](http://nn.cs.utexas.edu/downloads/papers/stanley.ec02.pdf) by Kenneth O. Stanley and Risto Miikkulainen.

## Features

- **Classic NEAT Algorithm:** Faithfully implements speciation, crossover, and mutation of both weights and topology.
- **Clean Architecture:** A clear separation between the genotype (`core`), phenotype (`network`), and the evolutionary process (`evolution`).
- **Highly Configurable:** A single `NEATConfig` class with a builder pattern allows for easy tuning of dozens of parameters.
- **Recurrent & Feed-Forward Networks:** Supports both standard feed-forward networks and recurrent neural networks (RNNs) for tasks requiring memory, controlled by a simple boolean flag.
- **Live Visualization:** Includes a Swing-based visualizer to watch the topology of the champion genome evolve in real-time.
- **Genome Serialization:** Easily save and load winning genomes as human-readable JSON files.
- **Parallel Fitness Evaluation:** Examples demonstrate how to leverage Java's parallel streams to significantly speed up fitness evaluation on multi-core systems.
- **Experiment Framework:** The `AbstractNeatExample` class provides a simple yet powerful template for setting up your own experiments with minimal boilerplate.
- **Rich Set of Examples:** Comes with six diverse examples that showcase how to apply NEAT to different problem domains.
- ## Project Structure

The library is organized into logical packages that map directly to the core concepts of NEAT.

-   `de.mkoehler.neat.config`
    -   Contains `NEATConfig.java`, a central class for all algorithm parameters. This is the main control panel for tuning an experiment.

-   `de.mkoehler.neat.core`
    -   Represents the **genotype**.
    -   `Genome.java`: The genetic blueprint of a neural network, containing a collection of node and connection genes.
    -   `NodeGene.java` & `ConnectionGene.java`: The "DNA" of the network, defining nodes and their connections.
    -   `InnovationTracker.java`: A crucial NEAT component that assigns unique innovation numbers to new structural mutations.

-   `de.mkoehler.neat.network`
    -   Represents the **phenotype** (the functional neural network).
    -   `NeuralNetwork.java`: The functional network created from a `Genome`. It handles the activation process.
    -   `Neuron.java` & `Synapse.java`: The building blocks of the `NeuralNetwork`.
    -   `ActivationFunction.java`: A utility class with common activation functions (Sigmoid, Tanh, ReLU, etc.).

-   `de.mkoehler.neat.evolution`
    -   Manages the evolutionary process.
    -   `Population.java`: Manages the entire population of genomes across generations. Contains the main `evolve()` loop.
    -   `Species.java`: Manages a sub-group of similar genomes, calculating shared fitness and handling reproduction.
    -   `FitnessEvaluator.java`: A functional interface you implement to define how a genome's performance is scored.
    -   `GoalEvaluator.java`: An optional interface to define a hard "success" condition for your problem.

-   `de.mkoehler.neat.examples`
    -   Contains several sub-packages, each demonstrating how to use the library to solve a specific problem.
## Included Examples

The examples are designed to showcase different features and use cases of the JNeat library. Each example has a `main` method and can be run directly from your IDE.

---

### 1. XOR
-   **What is it?** The classic "Hello, World!" of neural networks. The network must learn the exclusive OR (XOR) function, which is not linearly separable.
-   **What's Special?** This is the simplest example and serves as a sanity check to ensure the core NEAT algorithm is working correctly. It demonstrates a minimal setup.
-   **How to Run:**
    -   Execute the `main` method in `de.mkoehler.neat.examples.xor.Main`.
    -   The program will print the progress for each generation and stop once a solution is found, printing the final test results.

---

### 2. Cart-Pole (Simple)
-   **What is it?** The classic OpenAI Gym control problem. A pole is attached to a cart, and the network must learn to move the cart left or right to keep the pole balanced.
-   **What's Special?** This is a basic control task. Fitness is simply the number of time steps the pole remains balanced.
-   **How to Run:**
    -   Execute the `main` method in `de.mkoehler.neat.examples.cartpole.MainCartPole`.
    -   Console output shows the generation progress. The final, best-performing genome is demonstrated with a simple text-based animation in the console.

---

### 3. Cart-Pole (Advanced Interactive)
-   **What is it?** A more difficult version of the Cart-Pole problem. The network is rewarded not just for balancing but for keeping the cart near the center of the track. It also trains for robustness by surviving random "kicks" to the pole.
-   **What's Special?**
    -   Demonstrates a more complex fitness function.
    -   Uses the `CartPoleRobustnessEvaluator` to evolve controllers that can recover from disturbances.
    -   Features a live, interactive Swing visualizer. During the final demonstration, you can **click on either side of the cart** to apply a force and test the controller's stability.
-   **How to Run:**
    -   Execute the `main` method in `de.mkoehler.neat.examples.cartpole_centering.MainCartPoleCentering`.
    -   Two windows will appear: one showing the live topology of the generation's champion, and another for the final interactive demonstration.
---

### 4. Memory Challenge
-   **What is it?** A task specifically designed to require memory. An agent in a 2D world must first find and "eat" a piece of food. Only then does the final goal appear. The agent must remember the location of the goal relative to the food.
-   **What's Special?**
    -   This example requires a **recurrent neural network (RNN)**. The configuration `allowRecurrent` is set to `true`.
    -   It uses the `AbstractNeatExample` framework, demonstrating how to set up a `GoalEvaluator` to define a clear success condition.
    -   Network activation is done over multiple time steps (`net.activate(inputs, 3)`) to allow signals to propagate through recurrent connections.
-   **How to Run:**
    -   Execute the `main` method in `de.mkoehler.neat.examples.memory.MainMemoryChallenge`.

---

### 5. Car Racing
-   **What is it?** A more complex control problem where a car with proximity sensors must learn to navigate a race track.
-   **What's Special?**
    -   Demonstrates a larger network (initially 3 inputs, 2 outputs).
    -   The `CarRacingEvaluator` is **parallelized** using `population.parallelStream()`, dramatically speeding up evaluation time on multi-core CPUs.
    -   It uses the `AbstractNeatExample` framework, so it supports command-line arguments for loading and seeding genomes.
-   **How to Run:**
    -   **Standard Evolution:** Execute `main` in `de.mkoehler.neat.examples.car.MainCarRacing`.
    -   **Load & Demonstrate:** After a champion genome (e.g., `carRacing_fitness-1234_...json`) is saved, you can run a demonstration without re-evolving by passing the `--demo <filepath>` command-line arguments.
    -   **Seed Evolution:** Start a new run seeded with a previously saved genome by passing the `--genome <filepath>` command-line arguments.

---

### 6. 2D Soccer
-   **What is it?** A 1-vs-1 soccer game where genomes must learn to play against each other.
-   **What's Special?**
    -   **Co-evolution:** Genomes are evaluated by playing against other genomes in the same population, not against a static opponent.
    -   **Complex, Parallel Evaluation:** The `SoccerEvaluator` plays hundreds of matches per generation in parallel.
    -   **Egocentric Inputs:** The network receives sensory data relative to its own position and orientation, a common technique for robust agent AI.
    -   **Multi-Panel Visualizer:** The UI shows the game field plus real-time visualizations of each player's sensory inputs.
-   **How to Run:**
    -   Execute `main` in `de.mkoehler.neat.examples.soccer.MainSoccer`. This also supports the `--demo` and `--genome` flags.
## Creating Your Own Experiment

The `AbstractNeatExample` class makes it easy to create a new experiment.

1.  Create a new class that extends `AbstractNeatExample`.
2.  Implement the required abstract methods:
    -   `createNeatConfig()`: Return a `NEATConfig` instance tailored to your problem (input/output nodes, etc.).
    -   `createFitnessEvaluator()`: Return an instance of your custom `FitnessEvaluator`. This is where you define the "game" and scoring for your problem.
    -   `demonstrate()`: Write the code to visualize or test a single, winning genome.
    -   `getGenomeFilePrefix()`: Provide a name for saved genome files (e.g., `"myProblem"`).
3.  Create a `main` method to instantiate your class and call `run()`.

Here is a basic skeleton:
```java
public class MyExperiment extends AbstractNeatExample {

    public static void main(String[] args) {
        MyExperiment experiment = new MyExperiment();
        experiment.run(args, 500, 20); // Evolve for 500 gens, check goal every 20
    }

    @Override
    protected NEATConfig createNeatConfig() {
        return NEATConfig.builder()
                .inputNodes(3)
                .outputNodes(1)
                .populationSize(200)
                // ... other custom parameters
                .build();
    }

    @Override
    protected FitnessEvaluator createFitnessEvaluator(NEATConfig config) {
        // You would create your own MyFitnessEvaluator class
        return new MyFitnessEvaluator(config);
    }

    @Override
    protected void demonstrate(Genome bestGenome, NEATConfig config) {
        // Code to run and show off your winning genome
        System.out.println("Demonstrating the winner!");
        // ...
    }

    @Override
    protected String getGenomeFilePrefix() {
        return "myExperiment";
    }

    // Optionally override other hooks like setupProblemSpecificVisualizers()
}