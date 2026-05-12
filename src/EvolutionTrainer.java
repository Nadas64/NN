import java.util.*;

public class EvolutionTrainer {
    static Random random = new Random();

    public static double[] trainEvolution(
        NeuralNetwork nn,
        List<Row> data,
        int populationSize,
        int generations,
        int eliteCount,
        double mutationRate,
        double mutationStrength
    ) {
        int weightCount = nn.weightCount();

        double[][] population = new double[populationSize][weightCount];

        for (int i = 0; i < populationSize; i++) {
            for (int j = 0; j < weightCount; j++) {
                population[i][j] = random.nextDouble() * 2 - 1;
            }
        }

        for (int gen = 0; gen < generations; gen++) {
            Arrays.sort(population, Comparator.comparingDouble(w -> mse(nn, data, w)));

            double[][] next = new double[populationSize][weightCount];

            for (int i = 0; i < eliteCount; i++) {
                next[i] = population[i].clone();
            }

            for (int i = eliteCount; i < populationSize; i++) {
                double[] parent1 = population[random.nextInt(populationSize / 2)];
                double[] parent2 = population[random.nextInt(populationSize / 2)];

                double[] child = crossover(parent1, parent2);
                mutate(child, mutationRate, mutationStrength);

                next[i] = child;
            }

            population = next;
        }

        Arrays.sort(population, Comparator.comparingDouble(w -> mse(nn, data, w)));

        return population[0];
    }

    static double[] crossover(double[] a, double[] b) {
        double[] child = new double[a.length];

        for (int i = 0; i < a.length; i++) {
            if (random.nextBoolean()) {
                child[i] = a[i];
            } else {
                child[i] = b[i];
            }
        }

        return child;
    }

    static void mutate(double[] weights, double rate, double strength) {
        for (int i = 0; i < weights.length; i++) {
            if (random.nextDouble() < rate) {
                weights[i] += random.nextGaussian() * strength;
            }
        }
    }

    public static int classFromOutput(double output) {
        if (output <= 1.5) {
            return 1;
        }

        if (output <= 2.5) {
            return 2;
        }

        return 3;
    }

    public static double accuracy(NeuralNetwork nn, List<Row> data, double[] weights) {
        int correct = 0;

        for (Row row : data) {
            double output = nn.predict(row.x, weights);

            int predictedClass = classFromOutput(output);
            int realClass = (int) row.target;

            if (predictedClass == realClass) {
                correct++;
            }
        }

        return Math.round(correct * 100.0 / data.size());
    }

    public static double mse(NeuralNetwork nn, List<Row> data, double[] weights) {
        double sum = 0;

        for (Row row : data) {
            double prediction = nn.predict(row.x, weights);
            double error = prediction - row.target;
            sum += error * error;
        }

        return sum / data.size();
    }
}
