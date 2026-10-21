import java.util.Comparator;

enum ShowOrder {
    SHOWTIME("By showtime", Comparator.comparing(Show::start)),
    TITLE("By movie title", Comparator.comparing(show -> show.movie().title(), String.CASE_INSENSITIVE_ORDER));

    private final String label;
    private final Comparator<Show> comparator;

    ShowOrder(String label, Comparator<Show> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    Comparator<Show> comparator() {
        return comparator;
    }

    @Override
    public String toString() {
        return label;
    }
}
