package co.lettermint;

import co.lettermint.types.GetReportForwardingResponse;
import co.lettermint.types.Operations;
import co.lettermint.types.ReportForwardingRequest;
import co.lettermint.types.ResendReportForwardingCodeResponse;
import co.lettermint.types.UpdateReportForwardingResponse;
import co.lettermint.types.VerifyReportForwardingRequest;
import co.lettermint.types.VerifyReportForwardingResponse;

/** DMARC and complaint report forwarding of a project. Needs the team token. Thread-safe. */
public final class ReportForwarding {
    private final Transport transport;

    ReportForwarding(Transport transport) {
        this.transport = transport;
    }

    /**
     * Retrieves the report forwarding settings.
     *
     * @param projectId the project id
     * @return the response
     */
    public GetReportForwardingResponse retrieve(String projectId) {
        return retrieve(projectId, null);
    }

    /**
     * Retrieves the report forwarding settings.
     *
     * @param projectId the project id
     * @param options per-call options, or null
     * @return the response
     */
    public GetReportForwardingResponse retrieve(String projectId, RequestOptions options) {
        return transport.call(Operations.GET_REPORT_FORWARDING, Transport.call("projects.reportForwarding.retrieve").path(projectId).options(options));
    }

    /**
     * Sets the forwarding destination.
     *
     * @param projectId the project id
     * @param body the request body
     * @return the response
     */
    public UpdateReportForwardingResponse update(String projectId, ReportForwardingRequest body) {
        return update(projectId, body, null);
    }

    /**
     * Sets the forwarding destination.
     *
     * @param projectId the project id
     * @param body the request body
     * @param options per-call options, or null
     * @return the response
     */
    public UpdateReportForwardingResponse update(String projectId, ReportForwardingRequest body, RequestOptions options) {
        return transport.call(Operations.UPDATE_REPORT_FORWARDING, Transport.call("projects.reportForwarding.update").path(projectId).body(body).options(options));
    }

    /**
     * Disables report forwarding (HTTP 204).
     *
     * @param projectId the project id
     */
    public void delete(String projectId) {
        delete(projectId, null);
    }

    /**
     * Disables report forwarding (HTTP 204).
     *
     * @param projectId the project id
     * @param options per-call options, or null
     */
    public void delete(String projectId, RequestOptions options) {
        transport.call(Operations.DELETE_REPORT_FORWARDING, Transport.call("projects.reportForwarding.delete").path(projectId).options(options));
    }

    /**
     * Verifies the destination with the emailed code.
     *
     * @param projectId the project id
     * @param body the request body
     * @return the response
     */
    public VerifyReportForwardingResponse verify(String projectId, VerifyReportForwardingRequest body) {
        return verify(projectId, body, null);
    }

    /**
     * Verifies the destination with the emailed code.
     *
     * @param projectId the project id
     * @param body the request body
     * @param options per-call options, or null
     * @return the response
     */
    public VerifyReportForwardingResponse verify(String projectId, VerifyReportForwardingRequest body, RequestOptions options) {
        return transport.call(Operations.VERIFY_REPORT_FORWARDING, Transport.call("projects.reportForwarding.verify").path(projectId).body(body).options(options));
    }

    /**
     * Sends the verification code again.
     *
     * @param projectId the project id
     * @return the response
     */
    public ResendReportForwardingCodeResponse resendCode(String projectId) {
        return resendCode(projectId, null);
    }

    /**
     * Sends the verification code again.
     *
     * @param projectId the project id
     * @param options per-call options, or null
     * @return the response
     */
    public ResendReportForwardingCodeResponse resendCode(String projectId, RequestOptions options) {
        return transport.call(Operations.RESEND_REPORT_FORWARDING_CODE, Transport.call("projects.reportForwarding.resendCode").path(projectId).options(options));
    }

    @Override
    public String toString() {
        return "ReportForwarding{}";
    }
}
