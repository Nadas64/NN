import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;

public class GUI extends JFrame {

    JTextField hiddenCountField = new JTextField("6");
    JTextField populationSizeField = new JTextField("100");
    JTextField generationsField = new JTextField("1000");
    JTextField eliteCountField = new JTextField("10");
    JTextField mutationRateField = new JTextField("0.08");
    JTextField mutationStrengthField = new JTextField("0.3");

    JButton loadCsvButton = new JButton("Įkelti CSV");
    JButton trainButton = new JButton("Paleisti training");
    JButton testRandomButton = new JButton("Atsitiktinė gėlė");

    JLabel messageLabel = new JLabel("", SwingConstants.CENTER);
    JLabel csvStatusLabel = new JLabel("CSV nerastas");

    List<Row> data;
    List<Row> testData;

    NeuralNetwork nn;
    double[] bestWeights;

    Random random = new Random();

    public GUI() {
        setTitle("Iris Neural Network");
        setSize(550, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel networkPanel = new JPanel(new GridLayout(1, 2, 8, 6));
        networkPanel.setBorder(BorderFactory.createTitledBorder("Tinklas"));
        networkPanel.add(new JLabel("Paslėptų neuronų kiekis:"));
        networkPanel.add(hiddenCountField);

        JPanel trainingPanel = new JPanel(new GridLayout(3, 2, 8, 6));
        trainingPanel.setBorder(BorderFactory.createTitledBorder("Mokymas"));
        trainingPanel.add(new JLabel("Bandymų kiekis:"));
        trainingPanel.add(populationSizeField);
        trainingPanel.add(new JLabel("Mokymo kartos:"));
        trainingPanel.add(generationsField);
        trainingPanel.add(new JLabel("Geriausių palikti:"));
        trainingPanel.add(eliteCountField);

        JPanel mutationPanel = new JPanel(new GridLayout(2, 2, 8, 6));
        mutationPanel.setBorder(BorderFactory.createTitledBorder("Mutacija"));
        mutationPanel.add(new JLabel("Mutacijos tikimybė:"));
        mutationPanel.add(mutationRateField);
        mutationPanel.add(new JLabel("Mutacijos stiprumas:"));
        mutationPanel.add(mutationStrengthField);

        settingsPanel.add(networkPanel);
        settingsPanel.add(trainingPanel);
        settingsPanel.add(mutationPanel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loadCsvButton);
        buttonPanel.add(trainButton);
        buttonPanel.add(testRandomButton);

        messageLabel.setFont(new Font("Arial", Font.BOLD, 20));
        messageLabel.setPreferredSize(new Dimension(500, 120));

        csvStatusLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        resultPanel.add(messageLabel, BorderLayout.CENTER);

        JPanel csvPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        csvPanel.add(csvStatusLabel);
        resultPanel.add(csvPanel, BorderLayout.SOUTH);

        add(settingsPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(resultPanel, BorderLayout.SOUTH);

        trainButton.setEnabled(false);
        testRandomButton.setEnabled(false);

        loadCsvButton.addActionListener(e -> loadCsv());
        trainButton.addActionListener(e -> trainModel());
        testRandomButton.addActionListener(e -> testRandomFlower());
    }

    void loadCsv() {
        try {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();

                data = CsvLoader.loadCsv(file.getAbsolutePath());

                csvStatusLabel.setText(file.getName() + " loaded");
                messageLabel.setText("");

                bestWeights = null;
                trainButton.setEnabled(true);
                testRandomButton.setEnabled(false);
            }

        } catch (Exception ex) {
            csvStatusLabel.setText("CSV nerastas");
            messageLabel.setText("");
        }
    }

    void trainModel() {

        showMessage("Training...");
        trainButton.setEnabled(false);
        testRandomButton.setEnabled(false);

        new Thread(() -> {
            try {
                int hiddenCount = Integer.parseInt(hiddenCountField.getText());
                int populationSize = Integer.parseInt(populationSizeField.getText());
                int generations = Integer.parseInt(generationsField.getText());
                int eliteCount = Integer.parseInt(eliteCountField.getText());
                double mutationRate = Double.parseDouble(mutationRateField.getText());
                double mutationStrength = Double.parseDouble(mutationStrengthField.getText());

                List<Row> shuffledData = new ArrayList<>(data);
                Collections.shuffle(shuffledData);

                int splitIndex = (int) (shuffledData.size() * 0.8);

                List<Row> trainData = shuffledData.subList(0, splitIndex);
                List<Row> testData = shuffledData.subList(splitIndex, shuffledData.size());

                nn = new NeuralNetwork(hiddenCount);

                bestWeights = EvolutionTrainer.trainEvolution(
                        nn,
                        trainData,
                        populationSize,
                        generations,
                        eliteCount,
                        mutationRate,
                        mutationStrength
                );

                double accuracy = EvolutionTrainer.accuracy(nn, testData, bestWeights);
                double mse = EvolutionTrainer.mse(nn, testData, bestWeights);

                SwingUtilities.invokeLater(() -> {
                    showMessage("Training atliktas<br>" +
                            "Tikslumas: " + accuracy + "%<br>" +
                            "MSE: " + String.format("%.4f", mse));

                    trainButton.setEnabled(true);
                    testRandomButton.setEnabled(true);
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    showMessage("Training klaida");
                    trainButton.setEnabled(true);
                });
            }
        }).start();
    }

    void testRandomFlower() {
        if (data == null || bestWeights == null) {
            return;
        }

        Row row = data.get(random.nextInt(data.size()));

        double output = nn.predict(row.x, bestWeights);
        int predictedClass = EvolutionTrainer.classFromOutput(output);
        int realClass = (int) row.target;

        String predictedSpecies = speciesFromClass(predictedClass);
        boolean guessed = predictedClass == realClass;

        showMessage("Spėjimas: " + predictedSpecies + "<br>Atspėjo: " + guessed);
    }

    void showMessage(String text) {
        messageLabel.setText(
                "<html><div style='text-align:center;'>" + text + "</div></html>"
        );
    }

    String speciesFromClass(int value) {
        return switch (value) {
            case 1 -> "Iris-setosa";
            case 2 -> "Iris-versicolor";
            default -> "Iris-virginica";
        };
    }
}
