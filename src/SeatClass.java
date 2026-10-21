enum SeatClass {
    STANDARD(250),
    PREMIUM(350);

    private final Money price;

    SeatClass(long rupees) {
        price = Money.ofRupees(rupees);
    }

    Money price() {
        return price;
    }
}
