import java.util.Locale;

record Money(long paise) {

    static final Money ZERO = new Money(0);

    static Money ofRupees(long rupees) {
        return new Money(rupees * 100);
    }

    Money plus(Money other) {
        return new Money(paise + other.paise);
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "₹%d.%02d", paise / 100, paise % 100);
    }
}
