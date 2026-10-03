package co.lettermint;

import co.lettermint.types.GetStatsQuery;
import co.lettermint.types.Operations;
import co.lettermint.types.StatsData;

/** Sending statistics. Needs the team token. Thread-safe. */
public final class Stats {
    private final Transport transport;

    Stats(Transport transport) {
        this.transport = transport;
    }

    /**
     * Daily statistics between {@code from} and {@code to} (Y-m-d, at most 90 days).
     *
     * @param query the query parameters
     * @return the response
     */
    public StatsData retrieve(GetStatsQuery query) {
        return retrieve(query, null);
    }

    /**
     * Daily statistics between {@code from} and {@code to} (Y-m-d, at most 90 days).
     *
     * @param query the query parameters
     * @param options per-call options, or null
     * @return the response
     */
    public StatsData retrieve(GetStatsQuery query, RequestOptions options) {
        return transport.call(Operations.GET_STATS, Transport.call("stats.retrieve").query(query).options(options));
    }

    @Override
    public String toString() {
        return "Stats{}";
    }
}
