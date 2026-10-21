package co.lettermint;

import co.lettermint.types.CreateTeamInviteData;
import co.lettermint.types.CursorPage;
import co.lettermint.types.ListTeamInvitesQuery;
import co.lettermint.types.MessageResponse;
import co.lettermint.types.Operations;
import co.lettermint.types.TeamInviteData;
import co.lettermint.types.UpdateTeamInviteData;

/** Pending team invitations. Needs the team token. Thread-safe. */
public final class TeamInvites {
    private final Transport transport;

    TeamInvites(Transport transport) {
        this.transport = transport;
    }

    /**
     * Lists pending team invitations, one page at a time.
     *
     * @return the response
     */
    public CursorPage<TeamInviteData> list() {
        return list(null, null);
    }

    /**
     * Lists pending team invitations, one page at a time.
     *
     * @param query the query parameters, or null
     * @return the response
     */
    public CursorPage<TeamInviteData> list(ListTeamInvitesQuery query) {
        return list(query, null);
    }

    /**
     * Lists pending team invitations, one page at a time.
     *
     * @param query the query parameters, or null
     * @param options per-call options, or null
     * @return the response
     */
    public CursorPage<TeamInviteData> list(ListTeamInvitesQuery query, RequestOptions options) {
        return transport.call(Operations.LIST_TEAM_INVITES, Transport.call("team.invites.list").query(query).options(options));
    }

    /**
     * Iterates over every pending team invitation, following {@code next_cursor}.
     *
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<TeamInviteData> iterate() {
        return iterate(null, null);
    }

    /**
     * Iterates over every pending team invitation, following {@code next_cursor}.
     *
     * @param query the query parameters, or null
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<TeamInviteData> iterate(ListTeamInvitesQuery query) {
        return iterate(query, null);
    }

    /**
     * Iterates over every pending team invitation, following {@code next_cursor}.
     *
     * @param query the query parameters, or null
     * @param options per-call options, or null
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<TeamInviteData> iterate(ListTeamInvitesQuery query, RequestOptions options) {
        return transport.paginate(Operations.LIST_TEAM_INVITES, Transport.call("team.invites.iterate").query(query).options(options));
    }

    /**
     * Retrieves a pending team invitation.
     *
     * @param inviteId the invite id
     * @return the response
     */
    public TeamInviteData retrieve(String inviteId) {
        return retrieve(inviteId, null);
    }

    /**
     * Retrieves a pending team invitation.
     *
     * @param inviteId the invite id
     * @param options per-call options, or null
     * @return the response
     */
    public TeamInviteData retrieve(String inviteId, RequestOptions options) {
        return transport.call(Operations.GET_TEAM_INVITE, Transport.call("team.invites.retrieve").path(inviteId).options(options));
    }

    /**
     * Invites a person to the team and sends the invitation email. The team needs a free seat, and the
     * Owner role can't be assigned.
     *
     * @param body the request body
     * @return the response
     */
    public TeamInviteData create(CreateTeamInviteData body) {
        return create(body, null);
    }

    /**
     * Invites a person to the team and sends the invitation email. The team needs a free seat, and the
     * Owner role can't be assigned.
     *
     * @param body the request body
     * @param options per-call options, or null
     * @return the response
     */
    public TeamInviteData create(CreateTeamInviteData body, RequestOptions options) {
        return transport.call(Operations.CREATE_TEAM_INVITE, Transport.call("team.invites.create").body(body).options(options));
    }

    /**
     * Replaces a pending invitation's email, role and project access. A new invitation email is sent
     * only when the address changes.
     *
     * @param inviteId the invite id
     * @param body the request body
     * @return the response
     */
    public TeamInviteData update(String inviteId, UpdateTeamInviteData body) {
        return update(inviteId, body, null);
    }

    /**
     * Replaces a pending invitation's email, role and project access. A new invitation email is sent
     * only when the address changes.
     *
     * @param inviteId the invite id
     * @param body the request body
     * @param options per-call options, or null
     * @return the response
     */
    public TeamInviteData update(String inviteId, UpdateTeamInviteData body, RequestOptions options) {
        return transport.call(Operations.UPDATE_TEAM_INVITE, Transport.call("team.invites.update").path(inviteId).body(body).options(options));
    }

    /**
     * Revokes a pending team invitation.
     *
     * @param inviteId the invite id
     * @return the response
     */
    public MessageResponse revoke(String inviteId) {
        return revoke(inviteId, null);
    }

    /**
     * Revokes a pending team invitation.
     *
     * @param inviteId the invite id
     * @param options per-call options, or null
     * @return the response
     */
    public MessageResponse revoke(String inviteId, RequestOptions options) {
        return transport.call(Operations.DELETE_TEAM_INVITE, Transport.call("team.invites.revoke").path(inviteId).options(options));
    }

    @Override
    public String toString() {
        return "TeamInvites{}";
    }
}
