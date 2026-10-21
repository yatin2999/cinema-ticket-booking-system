import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

final class BookingService {

    private final List<Show> shows;
    private final Map<String, Booking> bookings = new LinkedHashMap<>();
    private final List<BookingListener> listeners = new ArrayList<>();
    private int lastReferenceNumber;

    BookingService(Collection<Show> shows) {
        this.shows = shows.stream()
                .sorted(Comparator.comparing(Show::start).thenComparing(Show::id))
                .toList();
    }

    List<Show> shows() {
        return shows;
    }

    void addListener(BookingListener listener) {
        listeners.add(listener);
    }

    Booking book(String customer, Show show, List<SeatPosition> seats) {
        Booking booking;
        synchronized (this) {
            Set<SeatPosition> booked = bookedSeats(show);
            List<SeatPosition> requested = seats.stream().sorted().toList();
            for (SeatPosition seat : requested) {
                if (booked.contains(seat)) {
                    throw new IllegalStateException("Seat " + seat + " is already booked");
                }
            }
            String reference = String.format(Locale.ROOT, "BK-%04d", ++lastReferenceNumber);
            booking = new Booking(reference, customer, show, requested, show.priceOf(requested), BookingStatus.CONFIRMED);
            bookings.put(reference, booking);
        }
        notifyListeners(booking);
        return booking;
    }

    Booking cancel(String reference) {
        Booking cancelled;
        synchronized (this) {
            cancelled = bookings.get(reference).cancelled();
            bookings.put(reference, cancelled);
        }
        notifyListeners(cancelled);
        return cancelled;
    }

    synchronized Booking findBooking(String reference) {
        return bookings.get(reference.strip().toUpperCase(Locale.ROOT));
    }

    synchronized Set<SeatPosition> bookedSeats(Show show) {
        Set<SeatPosition> booked = new HashSet<>();
        for (Booking booking : bookings.values()) {
            if (booking.isConfirmed() && booking.show().equals(show)) {
                booked.addAll(booking.seats());
            }
        }
        return booked;
    }

    synchronized ShowSummary summary(Show show) {
        Money sales = Money.ZERO;
        for (Booking booking : bookings.values()) {
            if (booking.isConfirmed() && booking.show().equals(show)) {
                sales = sales.plus(booking.total());
            }
        }
        return new ShowSummary(show, bookedSeats(show).size(), sales);
    }

    private void notifyListeners(Booking booking) {
        for (BookingListener listener : listeners) {
            listener.bookingChanged(booking);
        }
    }
}
