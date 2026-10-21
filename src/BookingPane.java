import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

final class BookingPane extends BorderPane implements BookingListener {

    private final BookingService service;
    private final ListView<Show> shows = new ListView<>();
    private final ChoiceBox<ShowOrder> order = new ChoiceBox<>();
    private final SeatMapView seatMap;
    private final Label showTitle = CinemaApp.heading("");
    private final Label description = new Label();
    private final Label showDetails = new Label();
    private final Label occupancy = new Label();
    private final VBox selectedSeats = new VBox(6);
    private final Label ticketCount = new Label();
    private final Label total = new Label();
    private final TextField customer = new TextField();
    private final Button confirm = new Button("Confirm booking");
    private final Button clear = new Button("Clear selection");
    private final Label message = CinemaApp.messageLabel();

    BookingPane(BookingService service) {
        this.service = service;
        seatMap = new SeatMapView(this::selectionEdited, this::selectionLimitReached);

        order.getItems().setAll(ShowOrder.values());
        order.setValue(ShowOrder.SHOWTIME);
        order.setAccessibleText("Sort shows");
        order.valueProperty().addListener((observable, previous, chosen) -> shows.getItems().sort(chosen.comparator()));
        shows.getItems().setAll(service.shows());
        shows.getItems().sort(order.getValue().comparator());
        shows.setCellFactory(list -> new ShowCell());
        shows.setPlaceholder(new Label("This catalogue has no shows."));
        shows.setAccessibleText("Shows");
        shows.getStyleClass().add("show-list");
        HBox showsHeader = new HBox(10, CinemaApp.heading("Shows"), CinemaApp.spacer(), order);
        showsHeader.setAlignment(Pos.CENTER_LEFT);
        VBox left = new VBox(10, showsHeader, shows);
        VBox.setVgrow(shows, Priority.ALWAYS);
        left.setPrefWidth(250);
        left.setMinWidth(220);

        description.getStyleClass().add("muted");
        description.setWrapText(true);
        description.setMinHeight(Region.USE_PREF_SIZE);
        description.managedProperty().bind(description.textProperty().isNotEmpty());
        showDetails.getStyleClass().add("muted");
        showDetails.setWrapText(true);
        showDetails.setMinHeight(Region.USE_PREF_SIZE);
        occupancy.getStyleClass().add("muted");
        ScrollPane mapScroll = new ScrollPane(new StackPane(seatMap));
        mapScroll.setFitToWidth(true);
        mapScroll.getStyleClass().add("plain-scroll");
        VBox centre = new VBox(6, showTitle, description, showDetails, occupancy, mapScroll);
        VBox.setVgrow(mapScroll, Priority.ALWAYS);
        VBox.setMargin(mapScroll, new Insets(10, 0, 0, 0));
        centre.getStyleClass().add("card");
        BorderPane.setMargin(centre, new Insets(0, 16, 0, 16));

        ticketCount.getStyleClass().add("muted");
        total.getStyleClass().add("total");
        HBox totalRow = new HBox(new Label("Total"), CinemaApp.spacer(), total);
        totalRow.setAlignment(Pos.CENTER_LEFT);
        confirm.getStyleClass().add("primary");
        confirm.setMaxWidth(Double.MAX_VALUE);
        confirm.setOnAction(event -> confirm());
        clear.setMaxWidth(Double.MAX_VALUE);
        clear.setOnAction(event -> seatMap.clearSelection());
        Label customerLabel = new Label("Customer name");
        customerLabel.setLabelFor(customer);
        customer.setPromptText("Name on the ticket");
        VBox summary = new VBox(12, CinemaApp.heading("Your selection"), selectedSeats, new Separator(),
                ticketCount, totalRow, new VBox(6, customerLabel, customer), confirm, clear, message);
        summary.getStyleClass().add("card");
        ScrollPane summaryScroll = new ScrollPane(summary);
        summaryScroll.setFitToWidth(true);
        summaryScroll.getStyleClass().add("plain-scroll");
        summaryScroll.setPrefWidth(290);
        summaryScroll.setMinWidth(260);

        setLeft(left);
        setCenter(centre);
        setRight(summaryScroll);
        getStyleClass().add("page");

        shows.getSelectionModel().selectedItemProperty().addListener((observable, previous, show) -> refresh());
        if (service.shows().isEmpty()) {
            refresh();
        } else {
            shows.getSelectionModel().selectFirst();
        }
    }

    @Override
    public void bookingChanged(Booking booking) {
        refresh();
    }

    private Show selectedShow() {
        return shows.getSelectionModel().getSelectedItem();
    }

    private void refresh() {
        Show show = selectedShow();
        seatMap.setVisible(show != null);
        if (show != null) {
            seatMap.show(show, service.bookedSeats(show));
        }
        describeShow(show);
        CinemaApp.say(message, CinemaApp.Tone.INFO, "");
        updateSummary();
    }

    private void selectionEdited() {
        CinemaApp.say(message, CinemaApp.Tone.INFO, "");
        updateSummary();
    }

    private void describeShow(Show show) {
        if (show == null) {
            showTitle.setText("No show to book");
            description.setText("");
            showDetails.setText("This catalogue has no shows.");
            occupancy.setText("");
            return;
        }
        showTitle.setText(show.movie().title() + " · " + show.movie().runtimeMinutes() + " min");
        description.setText(show.movie().description());
        showDetails.setText(CinemaApp.SHOW_TIME.format(show.start()) + " · " + show.auditorium().name() + " · "
                + SeatClass.STANDARD.price() + " standard, " + SeatClass.PREMIUM.price() + " premium");
        occupancy.setText("Seats sold: " + service.summary(show).occupancy());
    }

    private void updateSummary() {
        Show show = selectedShow();
        List<SeatPosition> seats = show == null ? List.of() : seatMap.selection();
        selectedSeats.getChildren().clear();
        if (seats.isEmpty()) {
            Label hint = new Label("Click free seats on the map to select up to " + SeatMapView.MAX_SEATS + " of them.");
            hint.getStyleClass().add("muted");
            hint.setWrapText(true);
            selectedSeats.getChildren().add(hint);
        }
        for (SeatPosition seat : seats) {
            boolean premium = show.auditorium().seatClassOf(seat) == SeatClass.PREMIUM;
            HBox line = new HBox(new Label(seat + (premium ? "   Premium" : "   Standard")), CinemaApp.spacer(),
                    new Label(show.priceOf(seat).toString()));
            line.getStyleClass().add("summary-line");
            selectedSeats.getChildren().add(line);
        }
        ticketCount.setText(seats.size() == 1 ? "1 ticket" : seats.size() + " tickets");
        total.setText(show == null ? Money.ZERO.toString() : show.priceOf(seats).toString());
        confirm.setDisable(seats.isEmpty());
        clear.setDisable(seats.isEmpty());
    }

    private void selectionLimitReached() {
        CinemaApp.say(message, CinemaApp.Tone.WARNING,
                "You can book at most " + SeatMapView.MAX_SEATS + " seats at a time.");
    }

    private void confirm() {
        String name = customer.getText().strip();
        if (name.isEmpty()) {
            CinemaApp.say(message, CinemaApp.Tone.WARNING, "Enter the customer's name to confirm the booking.");
            customer.requestFocus();
            return;
        }
        List<SeatPosition> seats = seatMap.selection();
        seatMap.clearSelection();
        customer.clear();
        service.book(name, selectedShow(), seats);
    }

    private static final class ShowCell extends ListCell<Show> {

        @Override
        protected void updateItem(Show show, boolean empty) {
            super.updateItem(show, empty);
            setText(null);
            if (empty || show == null) {
                setGraphic(null);
                setAccessibleText(null);
                return;
            }
            Label title = new Label(show.movie().title());
            title.getStyleClass().add("show-title");
            Label when = new Label(CinemaApp.SHOW_TIME.format(show.start()) + " · " + show.auditorium().name());
            when.getStyleClass().add("show-meta");
            setGraphic(new VBox(3, title, when));
            setAccessibleText(CinemaApp.describe(show));
        }
    }
}
