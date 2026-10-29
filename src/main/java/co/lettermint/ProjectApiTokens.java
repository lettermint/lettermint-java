package co.lettermint;

import co.lettermint.types.CursorPage;
import co.lettermint.types.ListProjectApiTokensQuery;
import co.lettermint.types.MessageResponse;
import co.lettermint.types.Operations;
import co.lettermint.types.ProjectApiToken;
import co.lettermint.types.ProjectApiTokenCreatedResponse;
import co.lettermint.types.StoreProjectApiTokenData;

/** API tokens of a project. Needs the team token. Thread-safe. */
public final class ProjectApiTokens {
    private final Transport transport;

    ProjectApiTokens(Transport transport) {
        this.transport = transport;
    }

    /**
     * Lists the API tokens of a project, one page at a time. The token values are not included.
     *
     * @param projectId the project id
     * @return the response
     */
    public CursorPage<ProjectApiToken> list(String projectId) {
        return list(projectId, null, null);
    }

    /**
     * Lists the API tokens of a project, one page at a time. The token values are not included.
     *
     * @param projectId the project id
     * @param query the query parameters, or null
     * @return the response
     */
    public CursorPage<ProjectApiToken> list(String projectId, ListProjectApiTokensQuery query) {
        return list(projectId, query, null);
    }

    /**
     * Lists the API tokens of a project, one page at a time. The token values are not included.
     *
     * @param projectId the project id
     * @param query the query parameters, or null
     * @param options per-call options, or null
     * @return the response
     */
    public CursorPage<ProjectApiToken> list(String projectId, ListProjectApiTokensQuery query, RequestOptions options) {
        return transport.call(Operations.LIST_PROJECT_API_TOKENS, Transport.call("projects.apiTokens.list").path(projectId).query(query).options(options));
    }

    /**
     * Iterates over every API token of a project, following {@code next_cursor}.
     *
     * @param projectId the project id
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<ProjectApiToken> iterate(String projectId) {
        return iterate(projectId, null, null);
    }

    /**
     * Iterates over every API token of a project, following {@code next_cursor}.
     *
     * @param projectId the project id
     * @param query the query parameters, or null
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<ProjectApiToken> iterate(String projectId, ListProjectApiTokensQuery query) {
        return iterate(projectId, query, null);
    }

    /**
     * Iterates over every API token of a project, following {@code next_cursor}.
     *
     * @param projectId the project id
     * @param query the query parameters, or null
     * @param options per-call options, or null
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<ProjectApiToken> iterate(String projectId, ListProjectApiTokensQuery query, RequestOptions options) {
        return transport.paginate(Operations.LIST_PROJECT_API_TOKENS, Transport.call("projects.apiTokens.iterate").path(projectId).query(query).options(options));
    }

    /**
     * Creates a project API token. The token ({@code data().token()}) is only returned once. Use it to
     * send through the API, or as the SMTP password when SMTP is enabled for the project; {@code smtp()}
     * then holds the SMTP settings.
     *
     * @param projectId the project id
     * @param body the request body
     * @return the response
     */
    public ProjectApiTokenCreatedResponse create(String projectId, StoreProjectApiTokenData body) {
        return create(projectId, body, null);
    }

    /**
     * Creates a project API token. The token ({@code data().token()}) is only returned once. Use it to
     * send through the API, or as the SMTP password when SMTP is enabled for the project; {@code smtp()}
     * then holds the SMTP settings.
     *
     * @param projectId the project id
     * @param body the request body
     * @param options per-call options, or null
     * @return the response
     */
    public ProjectApiTokenCreatedResponse create(String projectId, StoreProjectApiTokenData body, RequestOptions options) {
        return transport.call(Operations.CREATE_PROJECT_API_TOKEN, Transport.call("projects.apiTokens.create").path(projectId).body(body).options(options));
    }

    /**
     * Deletes a project API token. It stops working immediately.
     *
     * @param projectId the project id
     * @param tokenId the token id
     * @return the response
     */
    public MessageResponse delete(String projectId, String tokenId) {
        return delete(projectId, tokenId, null);
    }

    /**
     * Deletes a project API token. It stops working immediately.
     *
     * @param projectId the project id
     * @param tokenId the token id
     * @param options per-call options, or null
     * @return the response
     */
    public MessageResponse delete(String projectId, String tokenId, RequestOptions options) {
        return transport.call(Operations.DELETE_PROJECT_API_TOKEN, Transport.call("projects.apiTokens.delete").path(projectId, tokenId).options(options));
    }

    @Override
    public String toString() {
        return "ProjectApiTokens{}";
    }
}
