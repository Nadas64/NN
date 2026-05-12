public class NeuralNetwork {
    int inputCount = 4;
    int hiddenCount;

    public NeuralNetwork(int hiddenCount) {
        this.hiddenCount = hiddenCount;
    }

    public int weightCount() {
        return hiddenCount * inputCount + hiddenCount;
    }

    private double sigmoid(double z) {
        return 1.0 / (1.0 + Math.exp(-z));
    }

    public double predict(double[] x, double[] weights) {
        int k = 0;

        double[] hidden = new double[hiddenCount];

        for (int h = 0; h < hiddenCount; h++) {
            double sum = 0;

            for (int i = 0; i < inputCount; i++) {
                sum += x[i] * weights[k];
                k++;
            }
            hidden[h] = sigmoid(sum);
        }

        double output = 0;

        for (int h = 0; h < hiddenCount; h++) {
            output += hidden[h] * weights[k];
            k++;
        }

        return output;
    }
}
