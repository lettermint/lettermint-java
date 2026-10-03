package co.lettermint;

import co.lettermint.types.GetTeamQuery;
import co.lettermint.types.Operations;
import co.lettermint.types.TeamData;
import co.lettermint.types.TeamMutationResponse;
import co.lettermint.types.TeamRoleListResponse;
import co.lettermint.types.TeamUsageDetailData;
import co.lettermint.types.UpdateTeamData;

/** The team of the token. Needs the team token. Thread-safe. */
public final class Team {
    private final Transport transport;
    private final TeamMembers members;

    Team(Transport transport) {
        this.transport = transport;
        this.members = new TeamMembers(transport);
    }

    /**
     * @return team members
     */
    public TeamMembers members() {
        return members;
    }

    /**
     * Retrieves the team; {@code include} can add its features.
     *
     * @return the response
     */
    public TeamData retrieve() {
        return retrieve(null, null);
    }

    /**
     * Retrieves the team; {@code include} can add its features.
     *
     * @param query the query parameters, or null
     * @return the response
     */
    public TeamData retrieve(GetTeamQuery query) {
        return retrieve(query, null);
    }

    /**
     * Retrieves the team; {@code include} can add its features.
     *
     * @param query the query parameters, or null
     * @param options per-call options, or null
     * @return the response
     */
    public TeamData retrieve(GetTeamQuery query, RequestOptions options) {
        return transport.call(Operations.GET_TEAM, Transport.call("team.retrieve").query(query).options(options));
    }

    /**
     * Updates the team.
     *
     * @param body the request body
     * @return the response
     */
    public TeamMutationResponse update(UpdateTeamData body) {
        return update(body, null);
    }

    /**
     * Updates the team.
     *
     * @param body the request body
     * @param options per-call options, or null
     * @return the response
     */
    public TeamMutationResponse update(UpdateTeamData body, RequestOptions options) {
        return transport.call(Operations.UPDATE_TEAM, Transport.call("team.update").body(body).options(options));
    }

    /**
     * Usage of the current and previous billing periods.
     *
     * @return the response
     */
    public TeamUsageDetailData usage() {
        return usage(null);
    }

    /**
     * Usage of the current and previous billing periods.
     *
     * @param options per-call options, or null
     * @return the response
     */
    public TeamUsageDetailData usage(RequestOptions options) {
        return transport.call(Operations.GET_TEAM_USAGE, Transport.call("team.usage").options(options));
    }

    /**
     * The roles that can be assigned to members.
     *
     * @return the response
     */
    public TeamRoleListResponse roles() {
        return roles(null);
    }

    /**
     * The roles that can be assigned to members.
     *
     * @param options per-call options, or null
     * @return the response
     */
    public TeamRoleListResponse roles(RequestOptions options) {
        return transport.call(Operations.LIST_TEAM_ROLES, Transport.call("team.roles").options(options));
    }

    @Override
    public String toString() {
        return "Team{}";
    }
}
