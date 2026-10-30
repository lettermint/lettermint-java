package co.lettermint;

import co.lettermint.types.CreateTeamRoleData;
import co.lettermint.types.DeleteTeamRoleQuery;
import co.lettermint.types.DuplicateTeamRoleData;
import co.lettermint.types.GetTeamQuery;
import co.lettermint.types.MessageResponse;
import co.lettermint.types.Operations;
import co.lettermint.types.TeamData;
import co.lettermint.types.TeamMutationResponse;
import co.lettermint.types.TeamRoleData;
import co.lettermint.types.TeamRoleListResponse;
import co.lettermint.types.TeamUsageDetailData;
import co.lettermint.types.UpdateTeamData;
import co.lettermint.types.UpdateTeamRoleData;

/** The team of the token. Needs the team token. Thread-safe. */
public final class Team {
    private final Transport transport;
    private final TeamMembers members;
    private final TeamInvites invites;
    private final TeamApiTokens apiTokens;

    Team(Transport transport) {
        this.transport = transport;
        this.members = new TeamMembers(transport);
        this.invites = new TeamInvites(transport);
        this.apiTokens = new TeamApiTokens(transport);
    }

    /**
     * @return team members
     */
    public TeamMembers members() {
        return members;
    }

    /**
     * @return pending team invitations
     */
    public TeamInvites invites() {
        return invites;
    }

    /**
     * @return team API tokens
     */
    public TeamApiTokens apiTokens() {
        return apiTokens;
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

    /**
     * Retrieves a team role.
     *
     * @param roleId the role id
     * @return the response
     */
    public TeamRoleData retrieveRole(String roleId) {
        return retrieveRole(roleId, null);
    }

    /**
     * Retrieves a team role.
     *
     * @param roleId the role id
     * @param options per-call options, or null
     * @return the response
     */
    public TeamRoleData retrieveRole(String roleId, RequestOptions options) {
        return transport.call(Operations.GET_TEAM_ROLE, Transport.call("team.retrieveRole").path(roleId).options(options));
    }

    /**
     * Creates a custom role. Needs a plan with custom roles; the permissions must include the
     * permissions they depend on, and role names are unique within the team.
     *
     * @param body the request body
     * @return the response
     */
    public TeamRoleData createRole(CreateTeamRoleData body) {
        return createRole(body, null);
    }

    /**
     * Creates a custom role. Needs a plan with custom roles; the permissions must include the
     * permissions they depend on, and role names are unique within the team.
     *
     * @param body the request body
     * @param options per-call options, or null
     * @return the response
     */
    public TeamRoleData createRole(CreateTeamRoleData body, RequestOptions options) {
        return transport.call(Operations.CREATE_TEAM_ROLE, Transport.call("team.createRole").body(body).options(options));
    }

    /**
     * Replaces a custom role's name, description and permissions. Built-in roles can't be changed.
     *
     * @param roleId the role id
     * @param body the request body
     * @return the response
     */
    public TeamRoleData updateRole(String roleId, UpdateTeamRoleData body) {
        return updateRole(roleId, body, null);
    }

    /**
     * Replaces a custom role's name, description and permissions. Built-in roles can't be changed.
     *
     * @param roleId the role id
     * @param body the request body
     * @param options per-call options, or null
     * @return the response
     */
    public TeamRoleData updateRole(String roleId, UpdateTeamRoleData body, RequestOptions options) {
        return transport.call(Operations.UPDATE_TEAM_ROLE, Transport.call("team.updateRole").path(roleId).body(body).options(options));
    }

    /**
     * Deletes a custom role. Built-in roles can't be deleted. While the role is still assigned to
     * members, invites or SCIM groups, pass {@code replacementRoleId} to move those assignments.
     *
     * @param roleId the role id
     * @return the response
     */
    public MessageResponse deleteRole(String roleId) {
        return deleteRole(roleId, null, null);
    }

    /**
     * Deletes a custom role. Built-in roles can't be deleted. While the role is still assigned to
     * members, invites or SCIM groups, pass {@code replacementRoleId} to move those assignments.
     *
     * @param roleId the role id
     * @param query the query parameters, or null
     * @return the response
     */
    public MessageResponse deleteRole(String roleId, DeleteTeamRoleQuery query) {
        return deleteRole(roleId, query, null);
    }

    /**
     * Deletes a custom role. Built-in roles can't be deleted. While the role is still assigned to
     * members, invites or SCIM groups, pass {@code replacementRoleId} to move those assignments.
     *
     * @param roleId the role id
     * @param query the query parameters, or null
     * @param options per-call options, or null
     * @return the response
     */
    public MessageResponse deleteRole(String roleId, DeleteTeamRoleQuery query, RequestOptions options) {
        return transport.call(Operations.DELETE_TEAM_ROLE, Transport.call("team.deleteRole").path(roleId).query(query).options(options));
    }

    /**
     * Copies a role under a new name. Without a description, the copy keeps the source role's
     * description. The Owner role can't be duplicated.
     *
     * @param roleId the role id
     * @param body the request body
     * @return the response
     */
    public TeamRoleData duplicateRole(String roleId, DuplicateTeamRoleData body) {
        return duplicateRole(roleId, body, null);
    }

    /**
     * Copies a role under a new name. Without a description, the copy keeps the source role's
     * description. The Owner role can't be duplicated.
     *
     * @param roleId the role id
     * @param body the request body
     * @param options per-call options, or null
     * @return the response
     */
    public TeamRoleData duplicateRole(String roleId, DuplicateTeamRoleData body, RequestOptions options) {
        return transport.call(Operations.DUPLICATE_TEAM_ROLE, Transport.call("team.duplicateRole").path(roleId).body(body).options(options));
    }

    @Override
    public String toString() {
        return "Team{}";
    }
}
