package co.lettermint;

import co.lettermint.client.LettermintClient;
import co.lettermint.endpoints.EmailEndpoint;

/**
 * Main entry point for the Lettermint Java SDK.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Lettermint lettermint = new Lettermint("your-api-token");
 *
 * SendEmailResponse response = lettermint.email()
 *     .from("sender@example.com")
 *     .to("recipient@example.com")
 *     .subject("Hello")
 *     .html("<p>Hello World</p>")
 *     .send();
 * }</pre>
 */
public class Lettermint {

    private final LettermintClient client;

    /**
     * Create a new Lettermint instance with the default API base URL.
     *
     * @param apiToken Your Lettermint API token
     */
    public Lettermint(String apiToken) {
        this(apiToken, null);
    }

    /**
     * Create a new Lettermint instance with a custom API base URL.
     *
     * @param apiToken Your Lettermint API token
     * @param baseUrl  Custom API base URL (e.g., "https://custom-url.com/v1")
     */
    public Lettermint(String apiToken, String baseUrl) {
        this.client = new LettermintClient(apiToken, baseUrl);
    }

    /**
     * Get a new email endpoint for sending emails.
     * Each call returns a fresh instance for thread-safety.
     *
     * @return A new email endpoint with fluent builder methods
     */
    public EmailEndpoint email() {
        return new EmailEndpoint(client);
    }

    /**
     * Get the underlying HTTP client.
     *
     * @return The Lettermint HTTP client
     */
    public LettermintClient getClient() {
        return client;
    }
}
