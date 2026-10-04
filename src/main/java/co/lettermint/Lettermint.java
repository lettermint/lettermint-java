package co.lettermint;

import co.lettermint.exceptions.LettermintConfigException;
import co.lettermint.types.AnalyticsQuery;
import co.lettermint.types.AnalyticsResponse;
import co.lettermint.types.BlockedFileTypes;
import co.lettermint.types.Operations;
import java.net.http.HttpClient;
import java.time.Duration;

/**
 * The Lettermint client.
 *
 * <pre>{@code
 * Lettermint lettermint = Lettermint.builder()
 *     .sendingToken(System.getenv("LETTERMINT_PROJECT_TOKEN"))
 *     .teamToken(System.getenv("LETTERMINT_TEAM_TOKEN"))
 *     .build();
 *
 * Lettermint lettermint = Lettermint.of("lm_...");  // team or sending token, detected by prefix
 * }</pre>
 *
 * <p>{@link #emails()} uses the sending token; every other part uses the team token. {@link #ping()},
 * {@code messages().reschedule()} and {@code messages().cancel()} accept either. The client holds no
 * message state, is immutable and thread-safe: create it once and share it.
 */
public final class Lettermint {
    private final Transport transport;
    private final Emails emails;
    private final Domains domains;
    private final Messages messages;
    private final Projects projects;
    private final Routes routes;
    private final Stats stats;
    private final Suppressions suppressions;
    private final Team team;
    private final Webhooks webhooks;

    private Lettermint(Transport transport) {
        this.transport = transport;
        this.emails = new Emails(transport);
        this.domains = new Domains(transport);
        this.messages = new Messages(transport);
        this.projects = new Projects(transport);
        this.routes = new Routes(transport);
        this.stats = new Stats(transport);
        this.suppressions = new Suppressions(transport);
        this.team = new Team(transport);
        this.webhooks = new Webhooks(transport);
    }

    /**
     * @return a builder for a client
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a client from one token with the default options. {@code lm_team_} followed by letters
     * and digits is a team token; {@code lm_} followed by letters and digits is a sending token.
     *
     * @param token a team or sending token
     * @return the client
     * @throws LettermintConfigException for any other format, such as an SSO token ({@code lm_sso_...});
     *     the message never contains the token
     */
    public static Lettermint of(String token) {
        return builder().token(token).build();
    }

    /**
     * @return sending email, with the sending token
     */
    public Emails emails() {
        return emails;
    }

    /**
     * @return sending domains, with the team token
     */
    public Domains domains() {
        return domains;
    }

    /**
     * @return sent and received messages, with the team token
     */
    public Messages messages() {
        return messages;
    }

    /**
     * @return projects and their report forwarding, with the team token
     */
    public Projects projects() {
        return projects;
    }

    /**
     * @return routes of a project, with the team token
     */
    public Routes routes() {
        return routes;
    }

    /**
     * @return sending statistics, with the team token
     */
    public Stats stats() {
        return stats;
    }

    /**
     * @return the suppression list, with the team token
     */
    public Suppressions suppressions() {
        return suppressions;
    }

    /**
     * @return the team and its members, with the team token
     */
    public Team team() {
        return team;
    }

    /**
     * @return webhook endpoints and their deliveries, with the team token
     */
    public Webhooks webhooks() {
        return webhooks;
    }

    /**
     * Checks the configured token: {@code GET /ping} returns {@code pong}. Uses the team token when
     * configured, otherwise the sending token.
     *
     * @return {@code pong}
     */
    public String ping() {
        return ping(null);
    }

    /**
     * @param options per-call options, or null
     * @return {@code pong}
     * @see #ping()
     */
    public String ping(RequestOptions options) {
        return transport.call(Operations.PING, Transport.call("ping").options(options)).trim();
    }

    /**
     * Queries email analytics. Needs the team token.
     *
     * @param query the metrics, dimensions and filters
     * @return the analytics
     */
    public AnalyticsResponse analytics(AnalyticsQuery query) {
        return analytics(query, null);
    }

    /**
     * @param query the metrics, dimensions and filters
     * @param options per-call options, or null
     * @return the analytics
     * @see #analytics(AnalyticsQuery)
     */
    public AnalyticsResponse analytics(AnalyticsQuery query, RequestOptions options) {
        return transport.call(Operations.QUERY_ANALYTICS, Transport.call("analytics").body(query).options(options));
    }

    /**
     * The file extensions and MIME types that cannot be attached. Needs the team token.
     *
     * @return the blocked file types
     */
    public BlockedFileTypes blockedFileTypes() {
        return blockedFileTypes(null);
    }

    /**
     * @param options per-call options, or null
     * @return the blocked file types
     * @see #blockedFileTypes()
     */
    public BlockedFileTypes blockedFileTypes(RequestOptions options) {
        return transport.call(Operations.LIST_BLOCKED_FILE_TYPES, Transport.call("blockedFileTypes").options(options));
    }

    /** The configuration without credentials: tokens are shown as {@code [redacted]}. */
    @Override
    public String toString() {
        return "Lettermint{baseUrl=" + transport.baseUrl()
                + ", timeout=" + transport.timeout()
                + ", sendingToken=" + (transport.hasSendingToken() ? Secret.REDACTED : "null")
                + ", teamToken=" + (transport.hasTeamToken() ? Secret.REDACTED : "null") + "}";
    }

    /** Builds a {@link Lettermint} client. Pass at least one token. Not thread-safe. */
    public static final class Builder {
        private Secret sendingToken;
        private Secret teamToken;
        private String baseUrl = Transport.DEFAULT_BASE_URL;
        private Duration timeout = Transport.DEFAULT_TIMEOUT;
        private HttpClient httpClient;

        private Builder() {
        }

        /**
         * @param sendingToken a project sending token ({@code lm_...}), sent as {@code x-lettermint-token}
         *     by {@code emails()}
         * @return this builder
         */
        public Builder sendingToken(String sendingToken) {
            this.sendingToken = Tokens.check("sendingToken", sendingToken);
            return this;
        }

        /**
         * @param teamToken a team API token ({@code lm_team_...}), sent as {@code Authorization: Bearer}
         *     by the Team API
         * @return this builder
         */
        public Builder teamToken(String teamToken) {
            this.teamToken = Tokens.check("teamToken", teamToken);
            return this;
        }

        /**
         * Sets {@link #teamToken(String)} or {@link #sendingToken(String)} by the token's format; see
         * {@link Lettermint#of(String)}.
         *
         * @param token a team or sending token
         * @return this builder
         * @throws LettermintConfigException for an unrecognised format
         */
        public Builder token(String token) {
            return Tokens.detect(token) == Tokens.Kind.TEAM ? teamToken(token) : sendingToken(token);
        }

        /**
         * @param baseUrl the API base URL; default {@code https://api.lettermint.co/v1}
         * @return this builder
         */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = Transport.checkBaseUrl(baseUrl);
            return this;
        }

        /**
         * @param timeout the request timeout, covering the whole request including reading the body;
         *     default 30 seconds
         * @return this builder
         */
        public Builder timeout(Duration timeout) {
            this.timeout = Transport.checkTimeout(timeout, "timeout");
            return this;
        }

        /**
         * Uses your {@link HttpClient}, for example for a proxy, TLS settings or an executor. It must not
         * follow redirects ({@code followRedirects(HttpClient.Redirect.NEVER)}, the JDK default), so
         * tokens never reach another host. The default client also uses the timeout to connect.
         *
         * @param httpClient the HTTP client
         * @return this builder
         */
        public Builder httpClient(HttpClient httpClient) {
            if (httpClient == null) {
                throw new LettermintConfigException("httpClient must not be null.");
            }
            if (httpClient.followRedirects() != HttpClient.Redirect.NEVER) {
                throw new LettermintConfigException(
                        "httpClient must not follow redirects; build it with followRedirects(HttpClient.Redirect.NEVER).");
            }
            this.httpClient = httpClient;
            return this;
        }

        /**
         * @return the client
         * @throws LettermintConfigException when neither token is set
         */
        public Lettermint build() {
            if (sendingToken == null && teamToken == null) {
                throw new LettermintConfigException("Pass sendingToken, teamToken or both.");
            }
            HttpClient client = httpClient != null
                    ? httpClient
                    : HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).connectTimeout(timeout).build();
            return new Lettermint(new Transport(sendingToken, teamToken, client, baseUrl, timeout));
        }

        /** The configuration without credentials. */
        @Override
        public String toString() {
            return "Lettermint.Builder{baseUrl=" + baseUrl + ", timeout=" + timeout
                    + ", sendingToken=" + (sendingToken != null ? Secret.REDACTED : "null")
                    + ", teamToken=" + (teamToken != null ? Secret.REDACTED : "null") + "}";
        }
    }
}
