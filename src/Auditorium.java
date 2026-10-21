record Auditorium(String id, String name, int rows, int seatsPerRow, int premiumRows) {

    int capacity() {
        return rows * seatsPerRow;
    }

    SeatClass seatClassOf(SeatPosition seat) {
        return seat.rowIndex() >= rows - premiumRows ? SeatClass.PREMIUM : SeatClass.STANDARD;
    }
}
