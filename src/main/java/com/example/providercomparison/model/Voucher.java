package com.example.providercomparison.model;

public sealed interface Voucher permits AbsoluteVoucher, PercentageVoucher {
}

record AbsoluteVoucher(int discountInCent, int minOrderValueInCent) implements Voucher {}

record PercentageVoucher(int percentage, int maxDiscountInCent) implements Voucher {}
