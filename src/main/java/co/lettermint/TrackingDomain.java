package co.lettermint;

import co.lettermint.types.Operations;
import co.lettermint.types.ProjectTrackingDomain;
import co.lettermint.types.ProjectTrackingDomainMutationResponse;
import co.lettermint.types.ProjectTrackingDomainVerificationResponse;
import co.lettermint.types.UpdateProjectTrackingDomainData;

/**
 * The custom tracking domain of a project: the domain that open and click tracking links use
 * instead of the Lettermint tracking domain. Needs the team token. Thread-safe.
 */
public final class TrackingDomain {
    private final Transport transport;

    TrackingDomain(Transport transport) {
        this.transport = transport;
    }

    /**
     * Retrieves the custom tracking domain and whether its CNAME record is verified. {@code domain}
     * is null when the project uses the Lettermint tracking domain.
     *
     * @param projectId the project id
     * @return the response
     */
    public ProjectTrackingDomain retrieve(String projectId) {
        return retrieve(projectId, null);
    }

    /**
     * Retrieves the custom tracking domain and whether its CNAME record is verified. {@code domain}
     * is null when the project uses the Lettermint tracking domain.
     *
     * @param projectId the project id
     * @param options per-call options, or null
     * @return the response
     */
    public ProjectTrackingDomain retrieve(String projectId, RequestOptions options) {
        return transport.call(Operations.GET_PROJECT_TRACKING_DOMAIN, Transport.call("projects.trackingDomain.retrieve").path(projectId).options(options));
    }

    /**
     * Sets or replaces the custom tracking domain. Add a CNAME record for the domain that points to
     * {@code cname_target}; tracking links keep using the Lettermint tracking domain until it is
     * verified. Needs a plan with custom tracking domains.
     *
     * @param projectId the project id
     * @param body the request body
     * @return the response
     */
    public ProjectTrackingDomainMutationResponse update(String projectId, UpdateProjectTrackingDomainData body) {
        return update(projectId, body, null);
    }

    /**
     * Sets or replaces the custom tracking domain. Add a CNAME record for the domain that points to
     * {@code cname_target}; tracking links keep using the Lettermint tracking domain until it is
     * verified. Needs a plan with custom tracking domains.
     *
     * @param projectId the project id
     * @param body the request body
     * @param options per-call options, or null
     * @return the response
     */
    public ProjectTrackingDomainMutationResponse update(String projectId, UpdateProjectTrackingDomainData body, RequestOptions options) {
        return transport.call(Operations.UPDATE_PROJECT_TRACKING_DOMAIN, Transport.call("projects.trackingDomain.update").path(projectId).body(body).options(options));
    }

    /**
     * Removes the custom tracking domain (HTTP 204). Tracking links use the Lettermint tracking
     * domain again.
     *
     * @param projectId the project id
     */
    public void delete(String projectId) {
        delete(projectId, null);
    }

    /**
     * Removes the custom tracking domain (HTTP 204). Tracking links use the Lettermint tracking
     * domain again.
     *
     * @param projectId the project id
     * @param options per-call options, or null
     */
    public void delete(String projectId, RequestOptions options) {
        transport.call(Operations.DELETE_PROJECT_TRACKING_DOMAIN, Transport.call("projects.trackingDomain.delete").path(projectId).options(options));
    }

    /**
     * Checks the CNAME record of the custom tracking domain now. When the record is missing, points
     * to another target, or the DNS lookup failed, this throws a
     * {@link co.lettermint.exceptions.ValidationException} whose {@code getBody()} holds the same
     * outcome ({@code data.found_cname}, {@code data.temporary_error}).
     *
     * @param projectId the project id
     * @return the response
     */
    public ProjectTrackingDomainVerificationResponse verify(String projectId) {
        return verify(projectId, null);
    }

    /**
     * Checks the CNAME record of the custom tracking domain now. When the record is missing, points
     * to another target, or the DNS lookup failed, this throws a
     * {@link co.lettermint.exceptions.ValidationException} whose {@code getBody()} holds the same
     * outcome ({@code data.found_cname}, {@code data.temporary_error}).
     *
     * @param projectId the project id
     * @param options per-call options, or null
     * @return the response
     */
    public ProjectTrackingDomainVerificationResponse verify(String projectId, RequestOptions options) {
        return transport.call(Operations.VERIFY_PROJECT_TRACKING_DOMAIN, Transport.call("projects.trackingDomain.verify").path(projectId).options(options));
    }

    @Override
    public String toString() {
        return "TrackingDomain{}";
    }
}
