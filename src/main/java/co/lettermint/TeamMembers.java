package co.lettermint;

import co.lettermint.types.CursorPage;
import co.lettermint.types.ListTeamMembersQuery;
import co.lettermint.types.Operations;
import co.lettermint.types.TeamMemberData;
import co.lettermint.types.UpdateTeamMemberAssignmentData;

/** Team members. Needs the team token. Thread-safe. */
public final class TeamMembers {
    private final Transport transport;

    TeamMembers(Transport transport) {
        this.transport = transport;
    }

    /**
     * Lists team members, one page at a time.
     *
     * @return the response
     */
    public CursorPage<TeamMemberData> list() {
        return list(null, null);
    }

    /**
     * Lists team members, one page at a time.
     *
     * @param query the query parameters, or null
     * @return the response
     */
    public CursorPage<TeamMemberData> list(ListTeamMembersQuery query) {
        return list(query, null);
    }

    /**
     * Lists team members, one page at a time.
     *
     * @param query the query parameters, or null
     * @param options per-call options, or null
     * @return the response
     */
    public CursorPage<TeamMemberData> list(ListTeamMembersQuery query, RequestOptions options) {
        return transport.call(Operations.LIST_TEAM_MEMBERS, Transport.call("team.members.list").query(query).options(options));
    }

    /**
     * Iterates over every team member, following {@code next_cursor}.
     *
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<TeamMemberData> iterate() {
        return iterate(null, null);
    }

    /**
     * Iterates over every team member, following {@code next_cursor}.
     *
     * @param query the query parameters, or null
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<TeamMemberData> iterate(ListTeamMembersQuery query) {
        return iterate(query, null);
    }

    /**
     * Iterates over every team member, following {@code next_cursor}.
     *
     * @param query the query parameters, or null
     * @param options per-call options, or null
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<TeamMemberData> iterate(ListTeamMembersQuery query, RequestOptions options) {
        return transport.paginate(Operations.LIST_TEAM_MEMBERS, Transport.call("team.members.iterate").query(query).options(options));
    }

    /**
     * Retrieves a team member.
     *
     * @param userId the user id
     * @return the response
     */
    public TeamMemberData retrieve(String userId) {
        return retrieve(userId, null);
    }

    /**
     * Retrieves a team member.
     *
     * @param userId the user id
     * @param options per-call options, or null
     * @return the response
     */
    public TeamMemberData retrieve(String userId, RequestOptions options) {
        return transport.call(Operations.GET_TEAM_MEMBER, Transport.call("team.members.retrieve").path(userId).options(options));
    }

    /**
     * Changes a member's role and project access.
     *
     * @param userId the user id
     * @param body the request body
     * @return the response
     */
    public TeamMemberData updateAssignment(String userId, UpdateTeamMemberAssignmentData body) {
        return updateAssignment(userId, body, null);
    }

    /**
     * Changes a member's role and project access.
     *
     * @param userId the user id
     * @param body the request body
     * @param options per-call options, or null
     * @return the response
     */
    public TeamMemberData updateAssignment(String userId, UpdateTeamMemberAssignmentData body, RequestOptions options) {
        return transport.call(Operations.UPDATE_TEAM_MEMBER_ASSIGNMENT, Transport.call("team.members.updateAssignment").path(userId).body(body).options(options));
    }

    @Override
    public String toString() {
        return "TeamMembers{}";
    }
}
