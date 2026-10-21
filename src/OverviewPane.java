import java.util.List;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

final class OverviewPane extends BorderPane implements BookingListener {

    private static final double ROW_HEIGHT = 32;

    private final BookingService service;
    private final TableView<ShowSummary> table = new TableView<>();
    private final Label totals = new Label();

    OverviewPane(BookingService service) {
        this.service = service;

        table.getColumns().addAll(List.of(
                column("Show", 64, summary -> summary.show().id()),
                column("Movie", 150, summary -> summary.show().movie().title()),
                column("Starts", 130, summary -> CinemaApp.SHOW_TIME.format(summary.show().start())),
                column("Seats sold", 145, ShowSummary::occupancy),
                column("Sales", 100, summary -> summary.sales().toString())));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setMinWidth(table.getColumns().stream().mapToDouble(TableColumn::getMinWidth).sum() + 4);
        table.setFixedCellSize(ROW_HEIGHT);
        table.setPrefHeight(Math.max(120, ROW_HEIGHT * (service.shows().size() + 1) + 4));
        table.setPlaceholder(new Label("This catalogue has no shows."));
        table.setAccessibleText("Seats sold and sales per show");
        totals.getStyleClass().add("totals");
        totals.setWrapText(true);
        VBox figures = new VBox(12, CinemaApp.heading("Occupancy and sales"), table, totals);
        figures.getStyleClass().add("card");
        ScrollPane figuresScroll = new ScrollPane(figures);
        figuresScroll.setFitToWidth(true);
        figuresScroll.getStyleClass().add("plain-scroll");

        setCenter(figuresScroll);
        getStyleClass().add("page");
        refresh();
    }

    @Override
    public void bookingChanged(Booking booking) {
        refresh();
    }

    private void refresh() {
        List<ShowSummary> summaries = service.shows().stream().map(service::summary).toList();
        table.getItems().setAll(summaries);
        int seatsSold = summaries.stream().mapToInt(ShowSummary::seatsSold).sum();
        int capacity = summaries.stream().mapToInt(ShowSummary::capacity).sum();
        Money sales = summaries.stream().map(ShowSummary::sales).reduce(Money.ZERO, Money::plus);
        totals.setText("All shows · seats sold: " + ShowSummary.occupancy(seatsSold, capacity) + " · sales: " + sales);
    }

    private static TableColumn<ShowSummary, String> column(String title, double width,
            Function<ShowSummary, String> value) {
        TableColumn<ShowSummary, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        column.setMinWidth(width);
        column.setPrefWidth(width);
        column.setSortable(false);
        column.setReorderable(false);
        return column;
    }
}
