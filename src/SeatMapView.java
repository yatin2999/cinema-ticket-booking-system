import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

final class SeatMapView extends VBox {

    static final int MAX_SEATS = 10;

    private static final String SELECTED_MARK = "✓";
    private static final String BOOKED_MARK = "×";

    private final GridPane grid = new GridPane();
    private final Map<SeatPosition, ToggleButton> buttons = new HashMap<>();
    private final SortedSet<SeatPosition> selection = new TreeSet<>();
    private final Runnable onSelectionChanged;
    private final Runnable onLimitReached;
    private Show show;

    SeatMapView(Runnable onSelectionChanged, Runnable onLimitReached) {
        this.onSelectionChanged = onSelectionChanged;
        this.onLimitReached = onLimitReached;
        Label screen = new Label("SCREEN");
        screen.getStyleClass().add("screen");
        screen.setMaxWidth(Double.MAX_VALUE);
        grid.setHgap(4);
        grid.setVgap(5);
        grid.setAlignment(Pos.CENTER);
        getChildren().addAll(screen, grid, legend());
        setSpacing(16);
        setAlignment(Pos.TOP_CENTER);
        setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        getStyleClass().add("seat-map");
    }

    void show(Show show, Set<SeatPosition> booked) {
        if (show != this.show) {
            selection.clear();
        }
        this.show = show;
        grid.getChildren().clear();
        buttons.clear();
        Auditorium auditorium = show.auditorium();
        for (int row = 0; row < auditorium.rows(); row++) {
            boolean premiumRow = auditorium.seatClassOf(new SeatPosition(row, 1)) == SeatClass.PREMIUM;
            grid.add(rowLabel(row, premiumRow), 0, row);
            for (int number = 1; number <= auditorium.seatsPerRow(); number++) {
                SeatPosition seat = new SeatPosition(row, number);
                grid.add(seatButton(seat, booked.contains(seat)), number, row);
            }
            grid.add(rowLabel(row, premiumRow), auditorium.seatsPerRow() + 1, row);
        }
    }

    List<SeatPosition> selection() {
        return List.copyOf(selection);
    }

    void clearSelection() {
        for (SeatPosition seat : List.copyOf(selection)) {
            ToggleButton button = buttons.get(seat);
            button.setSelected(false);
            describe(button, seat);
        }
        selection.clear();
        onSelectionChanged.run();
    }

    private ToggleButton seatButton(SeatPosition seat, boolean booked) {
        boolean premium = show.auditorium().seatClassOf(seat) == SeatClass.PREMIUM;
        ToggleButton button = new ToggleButton();
        button.getStyleClass().addAll("seat", premium ? "seat-premium" : "seat-standard");
        if (booked) {
            button.getStyleClass().add("seat-booked");
            button.setText(BOOKED_MARK);
            button.setDisable(true);
            button.setAccessibleText("Seat " + seat + ", " + (premium ? "premium" : "standard") + ", booked");
        } else {
            button.setSelected(selection.contains(seat));
            button.setOnAction(event -> toggle(button, seat));
            Tooltip tooltip = new Tooltip("Seat " + seat + " · " + (premium ? "Premium" : "Standard") + " · " + show.priceOf(seat));
            tooltip.setShowDelay(Duration.millis(300));
            button.setTooltip(tooltip);
            describe(button, seat);
        }
        buttons.put(seat, button);
        return button;
    }

    private void toggle(ToggleButton button, SeatPosition seat) {
        if (button.isSelected() && selection.size() == MAX_SEATS) {
            button.setSelected(false);
            onLimitReached.run();
            return;
        }
        if (button.isSelected()) {
            selection.add(seat);
        } else {
            selection.remove(seat);
        }
        describe(button, seat);
        onSelectionChanged.run();
    }

    private void describe(ToggleButton button, SeatPosition seat) {
        boolean premium = show.auditorium().seatClassOf(seat) == SeatClass.PREMIUM;
        button.setText(button.isSelected() ? SELECTED_MARK : String.valueOf(seat.number()));
        button.setAccessibleText("Seat " + seat + ", " + (premium ? "premium" : "standard") + ", "
                + show.priceOf(seat) + (button.isSelected() ? ", selected" : ", available"));
    }

    private static Label rowLabel(int row, boolean premiumRow) {
        String letter = String.valueOf(new SeatPosition(row, 1).rowLetter());
        Label label = new Label(premiumRow ? letter + "★" : letter);
        label.getStyleClass().add("row-label");
        if (premiumRow) {
            label.getStyleClass().add("premium");
        }
        return label;
    }

    private static HBox legend() {
        HBox legend = new HBox(18,
                legendItem("7", "seat-standard", "Standard"),
                legendItem("7", "seat-premium", "Premium (rows marked ★)"),
                legendItem(SELECTED_MARK, "seat-selected", "Selected"),
                legendItem(BOOKED_MARK, "seat-booked", "Booked"));
        legend.setAlignment(Pos.CENTER);
        legend.getStyleClass().add("legend");
        return legend;
    }

    private static HBox legendItem(String mark, String style, String text) {
        Label sample = new Label(mark);
        sample.getStyleClass().addAll("seat", "legend-seat", style);
        HBox item = new HBox(6, sample, new Label(text));
        item.setAlignment(Pos.CENTER_LEFT);
        return item;
    }
}
