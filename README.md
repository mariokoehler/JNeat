# JNeat: A Java Implementation of NEAT

JNeat is a pure Java implementation of the **NeuroEvolution of Augmenting Topologies (NEAT)** algorithm, originally developed by Kenneth O. Stanley. This project provides a lightweight, dependency-free library for evolving artificial neural networks to solve complex tasks.

The goal of this library is to provide a clean, understandable, and easy-to-use implementation of NEAT, suitable for both educational purposes and practical applications in AI and machine learning.

## Core Concepts of NEAT

NEAT is unique among neuroevolution algorithms because it evolves the network's structure (topology) alongside its connection weights. It achieves this through three key principles:

1.  **Genetic Encoding with Historical Markings:** A special genetic encoding with "innovation numbers" allows NEAT to meaningfully cross over genomes of different sizes and structures.
2.  **Speciation:** The population is divided into species based on topological similarity. This protects innovation by allowing new, potentially weaker structures time to optimize their weights before competing with the entire population.
3.  **Minimal Starting Structure:** Networks start as simple as possible and grow in complexity over generations, finding the minimal solution required for a task.

## Features

*   **Pure Java:** Written in modern Java with no external library dependencies (besides JUnit for testing).
*   **Full NEAT Implementation:** Includes speciation, crossover, and various mutation types (weights, add node, add connection).
*   **Configurable:** Key evolutionary parameters can be easily tuned through a configuration object.
*   **Clear Separation of Concerns:** The project is organized into distinct packages for the neural network, the evolutionary process, and configuration.
*   **Example Included:** Comes with a classic `XOR` example to demonstrate usage.

## Getting Started

### Prerequisites

*   Java Development Kit (JDK) 11 or newer
*   Apache Maven

### Installation

Since this project is not yet on a public repository like Maven Central, you will need to clone it and install it into your local Maven repository to use it in other projects.

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/mariokoehler/JNeat.git
    cd JNeat
    ```

2.  **Build and install with Maven:**
    ```bash
    mvn clean install
    ```
    This will compile the code and make the `JNeat` artifact available to your other local Maven projects.

### Usage Example: Solving the XOR Problem

Here is a step-by-step guide to evolving a network that can solve the XOR (exclusive OR) problem.

#### 1. Define a Fitness Function

You need to provide a function that evaluates how well a given genome (network) performs on the task. For XOR, we test the network against the four possible inputs and score it based on accuracy.

```java
// Implement the FitnessFunction interface
public class XorFitnessFunction implements FitnessFunction {
    private static final double[][] XOR_INPUTS = {{0, 0}, {0, 1}, {1, 0}, {1, 1}};
    private static final double[] XOR_IDEAL_OUTPUTS = {0, 1, 1, 0};

    @Override
    public double calculateFitness(Genome genome) {
        double errorSum = 0;
        NeuralNetwork network = new NeuralNetwork(genome);

        for (int i = 0; i < XOR_INPUTS.length; i++) {
            List<Double> output = network.calculate(XOR_INPUTS[i]);
            errorSum += Math.abs(XOR_IDEAL_OUTPUTS[i] - output.get(0));
        }
        
        // Fitness is the inverse of the error. A perfect network has fitness 4.0.
        // The formula (4 - errorSum)^2 rewards genomes that get very close to zero error.
        return Math.pow(4 - errorSum, 2);
    }
}