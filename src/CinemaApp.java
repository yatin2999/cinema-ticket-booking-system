import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class CinemaApp extends Application {

    static final String TITLE = "Cinema Ticket Booking System";
    static final DateTimeFormatter SHOW_TIME = DateTimeFormatter.ofPattern("EEE d MMM, HH:mm", Locale.ENGLISH);

    enum Tone { INFO, SUCCESS, WARNING, ERROR }

    private String catalogueName;
    private BookingService service;
    private String loadError;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        List<String> arguments = getParameters().getRaw();
        Path file = Path.of(arguments.isEmpty() ? "data/catalogue.txt" : arguments.get(0));
        catalogueName = file.getFileName().toString();
        try {
            service = new BookingService(CatalogueLoader.load(file));
        } catch (CatalogueException e) {
            loadError = e.getMessage();
        } catch (IOException e) {
            loadError = "The file could not be read: " + e.getMessage();
        }
    }

    @Override
    public void start(Stage stage) {
        Parent content = service == null ? loadErrorView() : tabs();
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(new BorderPane(content, header(), null, null, null),
                Math.min(1200, screen.getWidth()), Math.min(760, screen.getHeight() - 40));
        scene.getStylesheets().add(stylesheet().toExternalForm());
        stage.setTitle(TITLE);
        stage.setMinWidth(960);
        stage.setMinHeight(640);
        stage.setScene(scene);
        stage.show();
    }

    private Parent header() {
        Label title = new Label(TITLE);
        title.getStyleClass().add("app-title");
        Label subtitle = new Label(service == null
                ? "No catalogue loaded"
                : catalogueName + " · " + service.shows().size() + " shows");
        subtitle.getStyleClass().add("app-subtitle");
        VBox header = new VBox(4, title, subtitle);
        header.getStyleClass().add("app-header");
        return header;
    }

    private Parent tabs() {
        BookingPane booking = new BookingPane(service);
        TicketsPane tickets = new TicketsPane(service);
        OverviewPane overview = new OverviewPane(service);
        Tab ticketsTab = new Tab("Tickets", tickets);
        TabPane tabs = new TabPane(new Tab("Booking", booking), ticketsTab, new Tab("Overview", overview));
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        service.addListener(booking);
        service.addListener(tickets);
        service.addListener(overview);
        service.addListener(changed -> {
            if (changed.isConfirmed()) {
                tabs.getSelectionModel().select(ticketsTab);
            }
        });
        return tabs;
    }

    private Parent loadErrorView() {
        Label source = new Label("Catalogue: " + catalogueName);
        source.getStyleClass().add("muted");
        Label problem = messageLabel();
        say(problem, Tone.ERROR, loadError);
        Label advice = new Label("Nothing was loaded and no bookings can be made. Correct the file, then start the application again.");
        advice.setWrapText(true);
        Button close = new Button("Close");
        close.getStyleClass().add("primary");
        close.setOnAction(event -> Platform.exit());
        VBox card = new VBox(14, heading("The catalogue could not be loaded"), source, problem, advice, close);
        card.getStyleClass().add("card");
        card.setMaxSize(640, Region.USE_PREF_SIZE);
        StackPane page = new StackPane(card);
        StackPane.setAlignment(card, Pos.TOP_CENTER);
        page.getStyleClass().add("page");
        return page;
    }

    static Label heading(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-title");
        return label;
    }

    static Label messageLabel() {
        Label label = new Label();
        label.getStyleClass().add("message");
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        say(label, Tone.INFO, "");
        return label;
    }

    static void say(Label label, Tone tone, String text) {
        label.getStyleClass().removeAll("info", "success", "warning", "error");
        label.getStyleClass().add(tone.name().toLowerCase(Locale.ROOT));
        label.setText(text);
        label.setVisible(!text.isEmpty());
        label.setManaged(!text.isEmpty());
    }

    static Region spacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    static String describe(Show show) {
        return show.movie().title() + " · " + SHOW_TIME.format(show.start()) + " · " + show.auditorium().name();
    }

    static URL stylesheet() {
        return CinemaApp.class.getResource("/cinema.css");
    }
}
