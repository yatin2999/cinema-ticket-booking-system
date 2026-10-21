import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

final class TicketsPane extends BorderPane implements BookingListener {

    private static final ButtonType CANCEL_BOOKING = new ButtonType("Cancel booking", ButtonData.OK_DONE);
    private static final ButtonType KEEP_BOOKING = new ButtonType("Keep booking", ButtonData.CANCEL_CLOSE);

    private final BookingService service;
    private final TextField reference = new TextField();
    private final VBox ticket = new VBox(12);
    private final Label message = CinemaApp.messageLabel();
    private Booking shown;

    TicketsPane(BookingService service) {
        this.service = service;
        Label referenceLabel = new Label("Booking reference");
        referenceLabel.setLabelFor(reference);
        reference.setPromptText("For example BK-0001");
        reference.setPrefColumnCount(22);
        reference.setOnAction(event -> lookUp());
        Button find = new Button("Find booking");
        find.setOnAction(event -> lookUp());
        HBox search = new HBox(10, reference, find);
        search.setAlignment(Pos.CENTER_LEFT);
        ticket.getStyleClass().add("ticket");
        VBox content = new VBox(14, CinemaApp.heading("Find a booking"), new VBox(6, referenceLabel, search), message,
                CinemaApp.heading("Ticket"), ticket);
        content.setMaxWidth(600);
        setCenter(content);
        BorderPane.setAlignment(content, Pos.TOP_LEFT);
        getStyleClass().add("page");
        render();
    }

    @Override
    public void bookingChanged(Booking booking) {
        if (booking.isConfirmed()) {
            shown = booking;
            CinemaApp.say(message, CinemaApp.Tone.SUCCESS, "Booking " + booking.reference() + " is confirmed.");
        } else if (shown != null && shown.reference().equals(booking.reference())) {
            shown = booking;
        }
        render();
    }

    private void lookUp() {
        String text = reference.getText();
        if (text.isBlank()) {
            CinemaApp.say(message, CinemaApp.Tone.WARNING, "Enter a booking reference such as BK-0001.");
            return;
        }
        shown = service.findBooking(text);
        if (shown == null) {
            CinemaApp.say(message, CinemaApp.Tone.WARNING, "No booking has the reference " + text.strip() + ".");
        } else {
            CinemaApp.say(message, CinemaApp.Tone.INFO, "");
        }
        render();
    }

    private void cancel() {
        Booking booking = shown;
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "The seats in this booking will become available again.", CANCEL_BOOKING, KEEP_BOOKING);
        confirmation.setTitle("Cancel booking");
        confirmation.setHeaderText("Cancel booking " + booking.reference() + "?");
        confirmation.initOwner(getScene().getWindow());
        confirmation.getDialogPane().getStylesheets().add(CinemaApp.stylesheet().toExternalForm());
        if (confirmation.showAndWait().orElse(KEEP_BOOKING) != CANCEL_BOOKING) {
            return;
        }
        service.cancel(booking.reference());
        CinemaApp.say(message, CinemaApp.Tone.SUCCESS,
                "Booking " + booking.reference() + " is cancelled, and its seats are available again.");
    }

    private void render() {
        ticket.getChildren().clear();
        if (shown == null) {
            Label empty = new Label("No ticket to show. Confirm a booking on the Booking tab, or find one by its reference.");
            empty.getStyleClass().add("muted");
            empty.setWrapText(true);
            ticket.getChildren().add(empty);
            return;
        }
        Booking booking = shown;
        Show show = booking.show();
        Label referenceLabel = new Label(booking.reference());
        referenceLabel.getStyleClass().add("ticket-reference");
        Label status = new Label(booking.status().name());
        status.getStyleClass().addAll("status", booking.isConfirmed() ? "status-confirmed" : "status-cancelled");
        HBox top = new HBox(12, referenceLabel, CinemaApp.spacer(), status);
        top.setAlignment(Pos.CENTER_LEFT);
        Label movie = new Label(show.movie().title());
        movie.getStyleClass().add("ticket-movie");
        GridPane details = new GridPane();
        details.setHgap(18);
        details.setVgap(6);
        addRow(details, 0, "Customer", booking.customer());
        addRow(details, 1, "Auditorium", show.auditorium().name());
        addRow(details, 2, "Show time", CinemaApp.SHOW_TIME.format(show.start()));
        addRow(details, 3, "Seats", booking.seatList());
        addRow(details, 4, "Total", booking.total().toString());
        ticket.getChildren().addAll(top, movie, details);
        if (booking.isConfirmed()) {
            Button cancel = new Button("Cancel this booking");
            cancel.getStyleClass().add("danger");
            cancel.setOnAction(event -> cancel());
            ticket.getChildren().add(cancel);
        } else {
            Label note = new Label("This booking was cancelled and its seats were released.");
            note.getStyleClass().add("muted");
            ticket.getChildren().add(note);
        }
    }

    private static void addRow(GridPane grid, int row, String name, String value) {
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("field-name");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("field-value");
        valueLabel.setWrapText(true);
        grid.addRow(row, nameLabel, valueLabel);
    }
}
