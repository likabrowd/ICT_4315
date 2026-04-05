package parking;

import java.util.Objects;

// Represents currency values in cents to avoid errors during any calculations. 

public class Money {

    private final long cents;

    public Money(long cents) {
        this.cents = cents;
    }

    public static Money ofDollars(double dollars) {
        return new Money(Math.round(dollars * 100));
    }

    public static Money ofCents(long cents) {
        return new Money(cents);
    }

    public long getCents() {
        return cents;
    }

    public double getDollars() {
        return cents / 100.0;
    }

    public Money times(long n) {
        return new Money(cents * n);
    }

    public Money plus(Money other) {
        return new Money(this.cents + other.cents);
    }

    @Override
    public String toString() {
        return String.format("$%.2f", getDollars());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money)) return false;
        Money m = (Money) o;
        return cents == m.cents;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(cents);
    }
}
