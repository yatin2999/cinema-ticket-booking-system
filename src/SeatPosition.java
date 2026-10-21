record SeatPosition(int rowIndex, int number) implements Comparable<SeatPosition> {

    char rowLetter() {
        return (char) ('A' + rowIndex);
    }

    @Override
    public int compareTo(SeatPosition other) {
        return rowIndex != other.rowIndex ? Integer.compare(rowIndex, other.rowIndex) : Integer.compare(number, other.number);
    }

    @Override
    public String toString() {
        return rowLetter() + Integer.toString(number);
    }
}
