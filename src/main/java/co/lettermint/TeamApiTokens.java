package co.lettermint;

import co.lettermint.types.CursorPage;
import co.lettermint.types.ListTeamApiTokensQuery;
import co.lettermint.types.MessageResponse;
import co.lettermint.types.Operations;
import co.lettermint.types.StoreTeamApiTokenData;
import co.lettermint.types.TeamApiToken;
import co.lettermint.types.TeamApiTokenCreatedResponse;

/** Team API tokens. Needs the team token. Thread-safe. */
public final class TeamApiTokens {
    private final Transport transport;

    TeamApiTokens(Transport transport) {
        this.transport = transport;
    }

    /**
     * Lists the team's API tokens, one page at a time. The token values are not included.
     *
     * @return the response
     */
    public CursorPage<TeamApiToken> list() {
        return list(null, null);
    }

    /**
     * Lists the team's API tokens, one page at a time. The token values are not included.
     *
     * @param query the query parameters, or null
     * @return the response
     */
    public CursorPage<TeamApiToken> list(ListTeamApiTokensQuery query) {
        return list(query, null);
    }

    /**
     * Lists the team's API tokens, one page at a time. The token values are not included.
     *
     * @param query the query parameters, or null
     * @param options per-call options, or null
     * @return the response
     */
    public CursorPage<TeamApiToken> list(ListTeamApiTokensQuery query, RequestOptions options) {
        return transport.call(Operations.LIST_TEAM_API_TOKENS, Transport.call("team.apiTokens.list").query(query).options(options));
    }

    /**
     * Iterates over every team API token, following {@code next_cursor}.
     *
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<TeamApiToken> iterate() {
        return iterate(null, null);
    }

    /**
     * Iterates over every team API token, following {@code next_cursor}.
     *
     * @param query the query parameters, or null
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<TeamApiToken> iterate(ListTeamApiTokensQuery query) {
        return iterate(query, null);
    }

    /**
     * Iterates over every team API token, following {@code next_cursor}.
     *
     * @param query the query parameters, or null
     * @param options per-call options, or null
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<TeamApiToken> iterate(ListTeamApiTokensQuery query, RequestOptions options) {
        return transport.paginate(Operations.LIST_TEAM_API_TOKENS, Transport.call("team.apiTokens.iterate").query(query).options(options));
    }

    /**
     * Creates a team API token. The new token can only have abilities that the requesting token has.
     * It copies the requesting token's IP allowlist, and it can't outlive the requesting token. The
     * token ({@code data().token()}) is only returned once.
     *
     * @param body the request body
     * @return the response
     */
    public TeamApiTokenCreatedResponse create(StoreTeamApiTokenData body) {
        return create(body, null);
    }

    /**
     * Creates a team API token. The new token can only have abilities that the requesting token has.
     * It copies the requesting token's IP allowlist, and it can't outlive the requesting token. The
     * token ({@code data().token()}) is only returned once.
     *
     * @param body the request body
     * @param options per-call options, or null
     * @return the response
     */
    public TeamApiTokenCreatedResponse create(StoreTeamApiTokenData body, RequestOptions options) {
        return transport.call(Operations.CREATE_TEAM_API_TOKEN, Transport.call("team.apiTokens.create").body(body).options(options));
    }

    /**
     * Deletes a team API token. It stops working immediately. A token can only delete tokens whose
     * abilities it also has.
     *
     * @param tokenId the token id
     * @return the response
     */
    public MessageResponse delete(String tokenId) {
        return delete(tokenId, null);
    }

    /**
     * Deletes a team API token. It stops working immediately. A token can only delete tokens whose
     * abilities it also has.
     *
     * @param tokenId the token id
     * @param options per-call options, or null
     * @return the response
     */
    public MessageResponse delete(String tokenId, RequestOptions options) {
        return transport.call(Operations.DELETE_TEAM_API_TOKEN, Transport.call("team.apiTokens.delete").path(tokenId).options(options));
    }

    @Override
    public String toString() {
        return "TeamApiTokens{}";
    }
}
