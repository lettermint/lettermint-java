package co.lettermint;

import co.lettermint.types.CursorPage;
import co.lettermint.types.GetRouteQuery;
import co.lettermint.types.InboundDomainVerificationResponse;
import co.lettermint.types.ListRoutesQuery;
import co.lettermint.types.MessageResponse;
import co.lettermint.types.Operations;
import co.lettermint.types.RouteData;
import co.lettermint.types.RouteListData;
import co.lettermint.types.RouteMutationResponse;
import co.lettermint.types.StoreRouteData;
import co.lettermint.types.UpdateRouteData;

/** Routes of a project. Needs the team token. Thread-safe. */
public final class Routes {
    private final Transport transport;

    Routes(Transport transport) {
        this.transport = transport;
    }

    /**
     * Lists the routes of a project, one page at a time.
     *
     * @param projectId the project id
     * @return the response
     */
    public CursorPage<RouteListData> list(String projectId) {
        return list(projectId, null, null);
    }

    /**
     * Lists the routes of a project, one page at a time.
     *
     * @param projectId the project id
     * @param query the query parameters, or null
     * @return the response
     */
    public CursorPage<RouteListData> list(String projectId, ListRoutesQuery query) {
        return list(projectId, query, null);
    }

    /**
     * Lists the routes of a project, one page at a time.
     *
     * @param projectId the project id
     * @param query the query parameters, or null
     * @param options per-call options, or null
     * @return the response
     */
    public CursorPage<RouteListData> list(String projectId, ListRoutesQuery query, RequestOptions options) {
        return transport.call(Operations.LIST_ROUTES, Transport.call("routes.list").path(projectId).query(query).options(options));
    }

    /**
     * Iterates over every route of a project, following {@code next_cursor}.
     *
     * @param projectId the project id
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<RouteListData> iterate(String projectId) {
        return iterate(projectId, null, null);
    }

    /**
     * Iterates over every route of a project, following {@code next_cursor}.
     *
     * @param projectId the project id
     * @param query the query parameters, or null
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<RouteListData> iterate(String projectId, ListRoutesQuery query) {
        return iterate(projectId, query, null);
    }

    /**
     * Iterates over every route of a project, following {@code next_cursor}.
     *
     * @param projectId the project id
     * @param query the query parameters, or null
     * @param options per-call options, or null
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<RouteListData> iterate(String projectId, ListRoutesQuery query, RequestOptions options) {
        return transport.paginate(Operations.LIST_ROUTES, Transport.call("routes.iterate").path(projectId).query(query).options(options));
    }

    /**
     * Creates a route in a project.
     *
     * @param projectId the project id
     * @param body the request body
     * @return the response
     */
    public RouteMutationResponse create(String projectId, StoreRouteData body) {
        return create(projectId, body, null);
    }

    /**
     * Creates a route in a project.
     *
     * @param projectId the project id
     * @param body the request body
     * @param options per-call options, or null
     * @return the response
     */
    public RouteMutationResponse create(String projectId, StoreRouteData body, RequestOptions options) {
        return transport.call(Operations.CREATE_ROUTE, Transport.call("routes.create").path(projectId).body(body).options(options));
    }

    /**
     * Retrieves a route.
     *
     * @param routeId the route id
     * @return the response
     */
    public RouteData retrieve(String routeId) {
        return retrieve(routeId, null, null);
    }

    /**
     * Retrieves a route.
     *
     * @param routeId the route id
     * @param query the query parameters, or null
     * @return the response
     */
    public RouteData retrieve(String routeId, GetRouteQuery query) {
        return retrieve(routeId, query, null);
    }

    /**
     * Retrieves a route.
     *
     * @param routeId the route id
     * @param query the query parameters, or null
     * @param options per-call options, or null
     * @return the response
     */
    public RouteData retrieve(String routeId, GetRouteQuery query, RequestOptions options) {
        return transport.call(Operations.GET_ROUTE, Transport.call("routes.retrieve").path(routeId).query(query).options(options));
    }

    /**
     * Updates a route.
     *
     * @param routeId the route id
     * @param body the request body
     * @return the response
     */
    public RouteMutationResponse update(String routeId, UpdateRouteData body) {
        return update(routeId, body, null);
    }

    /**
     * Updates a route.
     *
     * @param routeId the route id
     * @param body the request body
     * @param options per-call options, or null
     * @return the response
     */
    public RouteMutationResponse update(String routeId, UpdateRouteData body, RequestOptions options) {
        return transport.call(Operations.UPDATE_ROUTE, Transport.call("routes.update").path(routeId).body(body).options(options));
    }

    /**
     * Deletes a route.
     *
     * @param routeId the route id
     * @return the response
     */
    public MessageResponse delete(String routeId) {
        return delete(routeId, null);
    }

    /**
     * Deletes a route.
     *
     * @param routeId the route id
     * @param options per-call options, or null
     * @return the response
     */
    public MessageResponse delete(String routeId, RequestOptions options) {
        return transport.call(Operations.DELETE_ROUTE, Transport.call("routes.delete").path(routeId).options(options));
    }

    /**
     * Checks the DNS of the route's inbound domain.
     *
     * @param routeId the route id
     * @return the response
     */
    public InboundDomainVerificationResponse verifyInboundDomain(String routeId) {
        return verifyInboundDomain(routeId, null);
    }

    /**
     * Checks the DNS of the route's inbound domain.
     *
     * @param routeId the route id
     * @param options per-call options, or null
     * @return the response
     */
    public InboundDomainVerificationResponse verifyInboundDomain(String routeId, RequestOptions options) {
        return transport.call(Operations.VERIFY_ROUTE_INBOUND_DOMAIN, Transport.call("routes.verifyInboundDomain").path(routeId).options(options));
    }

    @Override
    public String toString() {
        return "Routes{}";
    }
}
