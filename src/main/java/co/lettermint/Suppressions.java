package co.lettermint;

import co.lettermint.types.CursorPage;
import co.lettermint.types.DeleteSuppressionResponse;
import co.lettermint.types.ListSuppressionsQuery;
import co.lettermint.types.Operations;
import co.lettermint.types.StoreSuppressionData;
import co.lettermint.types.SuppressedRecipientData;
import co.lettermint.types.SuppressionStoreResponse;

/** The suppression list. Needs the team token. Thread-safe. */
public final class Suppressions {
    private final Transport transport;

    Suppressions(Transport transport) {
        this.transport = transport;
    }

    /**
     * Lists suppressions, one page at a time.
     *
     * @return the response
     */
    public CursorPage<SuppressedRecipientData> list() {
        return list(null, null);
    }

    /**
     * Lists suppressions, one page at a time.
     *
     * @param query the query parameters, or null
     * @return the response
     */
    public CursorPage<SuppressedRecipientData> list(ListSuppressionsQuery query) {
        return list(query, null);
    }

    /**
     * Lists suppressions, one page at a time.
     *
     * @param query the query parameters, or null
     * @param options per-call options, or null
     * @return the response
     */
    public CursorPage<SuppressedRecipientData> list(ListSuppressionsQuery query, RequestOptions options) {
        return transport.call(Operations.LIST_SUPPRESSIONS, Transport.call("suppressions.list").query(query).options(options));
    }

    /**
     * Iterates over every suppression, following {@code next_cursor}.
     *
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<SuppressedRecipientData> iterate() {
        return iterate(null, null);
    }

    /**
     * Iterates over every suppression, following {@code next_cursor}.
     *
     * @param query the query parameters, or null
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<SuppressedRecipientData> iterate(ListSuppressionsQuery query) {
        return iterate(query, null);
    }

    /**
     * Iterates over every suppression, following {@code next_cursor}.
     *
     * @param query the query parameters, or null
     * @param options per-call options, or null
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<SuppressedRecipientData> iterate(ListSuppressionsQuery query, RequestOptions options) {
        return transport.paginate(Operations.LIST_SUPPRESSIONS, Transport.call("suppressions.iterate").query(query).options(options));
    }

    /**
     * Suppresses one or more recipients.
     *
     * @param body the request body
     * @return the response
     */
    public SuppressionStoreResponse create(StoreSuppressionData body) {
        return create(body, null);
    }

    /**
     * Suppresses one or more recipients.
     *
     * @param body the request body
     * @param options per-call options, or null
     * @return the response
     */
    public SuppressionStoreResponse create(StoreSuppressionData body, RequestOptions options) {
        return transport.call(Operations.CREATE_SUPPRESSIONS, Transport.call("suppressions.create").body(body).options(options));
    }

    /**
     * Removes a suppression, or opens a review ticket for it.
     *
     * @param suppressionId the suppression id
     * @return the response
     */
    public DeleteSuppressionResponse delete(String suppressionId) {
        return delete(suppressionId, null);
    }

    /**
     * Removes a suppression, or opens a review ticket for it.
     *
     * @param suppressionId the suppression id
     * @param options per-call options, or null
     * @return the response
     */
    public DeleteSuppressionResponse delete(String suppressionId, RequestOptions options) {
        return transport.call(Operations.DELETE_SUPPRESSION, Transport.call("suppressions.delete").path(suppressionId).options(options));
    }

    @Override
    public String toString() {
        return "Suppressions{}";
    }
}
