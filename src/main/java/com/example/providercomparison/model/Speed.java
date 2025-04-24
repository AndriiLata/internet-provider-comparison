package com.example.providercomparison.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Immutable value object representing line speed in megabits per second (Mbit/s).
 * Down‑ and upstream are kept separate because some providers expose only down.
 */
@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Speed implements Comparable<Speed> {

    private final int downstreamMbps;
    private final int upstreamMbps;   // 0 if unknown

    /** Factory for downstream‑only speeds (common in the specs). */
    public static Speed down(int downstreamMbps) {
        return new Speed(downstreamMbps, 0);
    }

    public static Speed of(int down, int up) {
        return new Speed(down, up);
    }

    @Override
    public int compareTo(Speed other) {
        return Integer.compare(this.downstreamMbps, other.downstreamMbps);
    }
}
