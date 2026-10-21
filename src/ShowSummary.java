import java.math.BigDecimal;
import java.math.RoundingMode;

record ShowSummary(Show show, int seatsSold, Money sales) {

    int capacity() {
        return show.auditorium().capacity();
    }

    String occupancy() {
        return occupancy(seatsSold, capacity());
    }

    static String occupancy(int sold, int capacity) {
        if (capacity == 0) {
            return sold + " of 0 (n/a)";
        }
        BigDecimal percent = BigDecimal.valueOf(100L * sold).divide(BigDecimal.valueOf(capacity), 1, RoundingMode.HALF_UP);
        return sold + " of " + capacity + " (" + percent.toPlainString() + "%)";
    }
}
