import java.nio.file.*;
import java.util.*;

public class CsvLoader {

    public static List<Row> loadCsv(String file) throws Exception {
        List<Row> data = new ArrayList<>();
        List<String> lines = Files.readAllLines(Path.of(file));

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;

            String[] p = line.split(",");

            double[] x = new double[] {
                    Double.parseDouble(p[1]),
                    Double.parseDouble(p[2]),
                    Double.parseDouble(p[3]),
                    Double.parseDouble(p[4])
            };

            String species = p[5];

            Row row = new Row(x, species);
            row.target = targetOf(species);

            data.add(row);
        }
        return data;
    }

    static double targetOf(String species) {
        return switch (species) {
            case "Iris-setosa" -> 1.0;
            case "Iris-versicolor" -> 2.0;
            case "Iris-virginica" -> 3.0;
            default -> throw new RuntimeException("Nežinoma klasė: " + species);
        };
    }

}
