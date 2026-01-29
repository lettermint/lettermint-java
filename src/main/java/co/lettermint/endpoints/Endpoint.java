package co.lettermint.endpoints;

import co.lettermint.client.LettermintClient;

/**
 * Base class for API endpoints.
 */
public abstract class Endpoint {

    protected final LettermintClient client;

    protected Endpoint(LettermintClient client) {
        this.client = client;
    }
}
