import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class CatalogueLoader {

    private static final DateTimeFormatter START = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    static List<Show> load(Path file) throws IOException, CatalogueException {
        Map<String, Auditorium> auditoriums = new HashMap<>();
        Map<String, Movie> movies = new HashMap<>();
        Map<String, Show> shows = new LinkedHashMap<>();
        int lineNumber = 0;
        try {
            for (String line : Files.readAllLines(file)) {
                lineNumber++;
                line = line.strip();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] fields = line.split("\\s*\\|\\s*", -1);
                switch (fields[0]) {
                    case "AUDITORIUM" -> {
                        expectFields(fields, 6, lineNumber);
                        int rows = Integer.parseInt(fields[3]);
                        int seatsPerRow = Integer.parseInt(fields[4]);
                        int premiumRows = Integer.parseInt(fields[5]);
                        if (rows < 1 || rows > 26 || seatsPerRow < 1 || seatsPerRow > 30) {
                            throw new CatalogueException(lineNumber,
                                    "An auditorium needs 1 to 26 rows of 1 to 30 seats");
                        }
                        auditoriums.put(fields[1], new Auditorium(fields[1], fields[2], rows, seatsPerRow, premiumRows));
                    }
                    case "MOVIE" -> {
                        if (fields.length != 4) {
                            expectFields(fields, 5, lineNumber);
                        }
                        String description = fields.length == 5 ? fields[4] : "";
                        movies.put(fields[1], new Movie(fields[1], fields[2], Integer.parseInt(fields[3]), description));
                    }
                    case "SHOW" -> {
                        expectFields(fields, 5, lineNumber);
                        Movie movie = movies.get(fields[2]);
                        if (movie == null) {
                            throw new CatalogueException(lineNumber,
                                    "Movie '" + fields[2] + "' is not defined above this line");
                        }
                        Auditorium auditorium = auditoriums.get(fields[3]);
                        if (auditorium == null) {
                            throw new CatalogueException(lineNumber,
                                    "Auditorium '" + fields[3] + "' is not defined above this line");
                        }
                        LocalDateTime start = LocalDateTime.parse(fields[4], START);
                        shows.put(fields[1], new Show(fields[1], movie, auditorium, start));
                    }
                    default -> throw new CatalogueException(lineNumber, "Unknown record type '" + fields[0] + "'");
                }
            }
        } catch (NumberFormatException e) {
            throw new CatalogueException(lineNumber, "Rows, seats and runtimes must be whole numbers");
        } catch (DateTimeParseException e) {
            throw new CatalogueException(lineNumber,
                    "The start must look like 2026-11-06 19:30, not '" + e.getParsedString() + "'");
        }
        return new ArrayList<>(shows.values());
    }

    private static void expectFields(String[] fields, int count, int lineNumber) throws CatalogueException {
        if (fields.length != count) {
            throw new CatalogueException(lineNumber, fields[0] + " needs " + count + " fields, not " + fields.length);
        }
    }
}
