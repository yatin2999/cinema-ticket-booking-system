import java.util.List;
import java.util.stream.Collectors;

record Booking(String reference, String customer, Show show, List<SeatPosition> seats, Money total, BookingStatus status) {

    boolean isConfirmed() {
        return status == BookingStatus.CONFIRMED;
    }

    String seatList() {
        return seats.stream().map(SeatPosition::toString).collect(Collectors.joining(", "));
    }

    Booking cancelled() {
        return new Booking(reference, customer, show, seats, total, BookingStatus.CANCELLED);
    }
}
