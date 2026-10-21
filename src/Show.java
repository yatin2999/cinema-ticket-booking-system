import java.time.LocalDateTime;
import java.util.Collection;

record Show(String id, Movie movie, Auditorium auditorium, LocalDateTime start) {

    Money priceOf(SeatPosition seat) {
        return auditorium.seatClassOf(seat).price();
    }

    Money priceOf(Collection<SeatPosition> seats) {
        Money total = Money.ZERO;
        for (SeatPosition seat : seats) {
            total = total.plus(priceOf(seat));
        }
        return total;
    }
}
