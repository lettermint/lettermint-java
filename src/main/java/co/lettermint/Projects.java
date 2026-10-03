package co.lettermint;

import co.lettermint.types.CursorPage;
import co.lettermint.types.GetProjectQuery;
import co.lettermint.types.ListProjectsQuery;
import co.lettermint.types.MessageResponse;
import co.lettermint.types.Operations;
import co.lettermint.types.ProjectCreatedData;
import co.lettermint.types.ProjectData;
import co.lettermint.types.ProjectListData;
import co.lettermint.types.ProjectMutationResponse;
import co.lettermint.types.RotateProjectTokenResponse;
import co.lettermint.types.StoreProjectData;
import co.lettermint.types.UpdateProjectData;

/** Projects. Needs the team token. Thread-safe. */
public final class Projects {
    private final Transport transport;
    private final ReportForwarding reportForwarding;

    Projects(Transport transport) {
        this.transport = transport;
        this.reportForwarding = new ReportForwarding(transport);
    }

    /**
     * @return report forwarding of a project
     */
    public ReportForwarding reportForwarding() {
        return reportForwarding;
    }

    /**
     * Lists projects, one page at a time.
     *
     * @return the response
     */
    public CursorPage<ProjectListData> list() {
        return list(null, null);
    }

    /**
     * Lists projects, one page at a time.
     *
     * @param query the query parameters, or null
     * @return the response
     */
    public CursorPage<ProjectListData> list(ListProjectsQuery query) {
        return list(query, null);
    }

    /**
     * Lists projects, one page at a time.
     *
     * @param query the query parameters, or null
     * @param options per-call options, or null
     * @return the response
     */
    public CursorPage<ProjectListData> list(ListProjectsQuery query, RequestOptions options) {
        return transport.call(Operations.LIST_PROJECTS, Transport.call("projects.list").query(query).options(options));
    }

    /**
     * Iterates over every project, following {@code next_cursor}.
     *
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<ProjectListData> iterate() {
        return iterate(null, null);
    }

    /**
     * Iterates over every project, following {@code next_cursor}.
     *
     * @param query the query parameters, or null
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<ProjectListData> iterate(ListProjectsQuery query) {
        return iterate(query, null);
    }

    /**
     * Iterates over every project, following {@code next_cursor}.
     *
     * @param query the query parameters, or null
     * @param options per-call options, or null
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<ProjectListData> iterate(ListProjectsQuery query, RequestOptions options) {
        return transport.paginate(Operations.LIST_PROJECTS, Transport.call("projects.iterate").query(query).options(options));
    }

    /**
     * Creates a project. The response holds its sending token once ({@code apiToken}).
     *
     * @param body the request body
     * @return the response
     */
    public ProjectCreatedData create(StoreProjectData body) {
        return create(body, null);
    }

    /**
     * Creates a project. The response holds its sending token once ({@code apiToken}).
     *
     * @param body the request body
     * @param options per-call options, or null
     * @return the response
     */
    public ProjectCreatedData create(StoreProjectData body, RequestOptions options) {
        return transport.call(Operations.CREATE_PROJECT, Transport.call("projects.create").body(body).options(options));
    }

    /**
     * Retrieves a project.
     *
     * @param projectId the project id
     * @return the response
     */
    public ProjectData retrieve(String projectId) {
        return retrieve(projectId, null, null);
    }

    /**
     * Retrieves a project.
     *
     * @param projectId the project id
     * @param query the query parameters, or null
     * @return the response
     */
    public ProjectData retrieve(String projectId, GetProjectQuery query) {
        return retrieve(projectId, query, null);
    }

    /**
     * Retrieves a project.
     *
     * @param projectId the project id
     * @param query the query parameters, or null
     * @param options per-call options, or null
     * @return the response
     */
    public ProjectData retrieve(String projectId, GetProjectQuery query, RequestOptions options) {
        return transport.call(Operations.GET_PROJECT, Transport.call("projects.retrieve").path(projectId).query(query).options(options));
    }

    /**
     * Updates a project.
     *
     * @param projectId the project id
     * @param body the request body
     * @return the response
     */
    public ProjectMutationResponse update(String projectId, UpdateProjectData body) {
        return update(projectId, body, null);
    }

    /**
     * Updates a project.
     *
     * @param projectId the project id
     * @param body the request body
     * @param options per-call options, or null
     * @return the response
     */
    public ProjectMutationResponse update(String projectId, UpdateProjectData body, RequestOptions options) {
        return transport.call(Operations.UPDATE_PROJECT, Transport.call("projects.update").path(projectId).body(body).options(options));
    }

    /**
     * Deletes a project.
     *
     * @param projectId the project id
     * @return the response
     */
    public MessageResponse delete(String projectId) {
        return delete(projectId, null);
    }

    /**
     * Deletes a project.
     *
     * @param projectId the project id
     * @param options per-call options, or null
     * @return the response
     */
    public MessageResponse delete(String projectId, RequestOptions options) {
        return transport.call(Operations.DELETE_PROJECT, Transport.call("projects.delete").path(projectId).options(options));
    }

    /**
     * Rotates the project's legacy sending token. The API marks this endpoint as legacy.
     *
     * @param projectId the project id
     * @return the response
     * @deprecated The API marks this endpoint as legacy.
     */
    @Deprecated
    public RotateProjectTokenResponse rotateToken(String projectId) {
        return rotateToken(projectId, null);
    }

    /**
     * Rotates the project's legacy sending token. The API marks this endpoint as legacy.
     *
     * @param projectId the project id
     * @param options per-call options, or null
     * @return the response
     * @deprecated The API marks this endpoint as legacy.
     */
    @Deprecated
    public RotateProjectTokenResponse rotateToken(String projectId, RequestOptions options) {
        return transport.call(Operations.ROTATE_PROJECT_TOKEN, Transport.call("projects.rotateToken").path(projectId).options(options));
    }

    @Override
    public String toString() {
        return "Projects{}";
    }
}
