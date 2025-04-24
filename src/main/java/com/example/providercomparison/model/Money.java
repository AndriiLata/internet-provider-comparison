package com.example.providercomparison.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Monetary amount stored in the smallest unit (cent) to avoid rounding errors.
 * Mutable business logic (add, subtract) returns new instances – keeps the VO pure.
 */
@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Money implements Comparable<Money> {

    private final long amountInCent;
    private final Currency currency;          // always EUR here, but stays flexible

    /** EUR factory helper. */
    public static Money euro(long amountInCent) {
        return new Money(amountInCent, Currency.getInstance("EUR"));
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(this.amountInCent + other.amountInCent, currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(this.amountInCent - other.amountInCent, currency);
    }

    public BigDecimal toBigDecimal() {
        return BigDecimal.valueOf(amountInCent, 2).setScale(2, RoundingMode.HALF_EVEN);
    }

    private void requireSameCurrency(Money other) {
        if (!Objects.equals(this.currency, other.currency)) {
            throw new IllegalArgumentException("Currency mismatch: " + currency + " vs " + other.currency);
        }
    }

    @Override
    public int compareTo(Money other) {
        requireSameCurrency(other);
        return Long.compare(this.amountInCent, other.amountInCent);
    }
}
