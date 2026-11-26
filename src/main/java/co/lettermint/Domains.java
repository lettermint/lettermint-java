package co.lettermint;

import co.lettermint.types.CursorPage;
import co.lettermint.types.DnsVerificationSuccessResponse;
import co.lettermint.types.DomainData;
import co.lettermint.types.DomainListData;
import co.lettermint.types.DomainMutationResponse;
import co.lettermint.types.GetDomainQuery;
import co.lettermint.types.ListDomainsQuery;
import co.lettermint.types.MessageResponse;
import co.lettermint.types.Operations;
import co.lettermint.types.StoreDomainData;
import co.lettermint.types.UpdateDomainProjectsData;

/** Sending domains. Needs the team token. Thread-safe. */
public final class Domains {
    private final Transport transport;

    Domains(Transport transport) {
        this.transport = transport;
    }

    /**
     * Lists domains, one page at a time.
     *
     * @return the response
     */
    public CursorPage<DomainListData> list() {
        return list(null, null);
    }

    /**
     * Lists domains, one page at a time.
     *
     * @param query the query parameters, or null
     * @return the response
     */
    public CursorPage<DomainListData> list(ListDomainsQuery query) {
        return list(query, null);
    }

    /**
     * Lists domains, one page at a time.
     *
     * @param query the query parameters, or null
     * @param options per-call options, or null
     * @return the response
     */
    public CursorPage<DomainListData> list(ListDomainsQuery query, RequestOptions options) {
        return transport.call(Operations.LIST_DOMAINS, Transport.call("domains.list").query(query).options(options));
    }

    /**
     * Iterates over every domain, following {@code next_cursor}.
     *
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<DomainListData> iterate() {
        return iterate(null, null);
    }

    /**
     * Iterates over every domain, following {@code next_cursor}.
     *
     * @param query the query parameters, or null
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<DomainListData> iterate(ListDomainsQuery query) {
        return iterate(query, null);
    }

    /**
     * Iterates over every domain, following {@code next_cursor}.
     *
     * @param query the query parameters, or null
     * @param options per-call options, or null
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<DomainListData> iterate(ListDomainsQuery query, RequestOptions options) {
        return transport.paginate(Operations.LIST_DOMAINS, Transport.call("domains.iterate").query(query).options(options));
    }

    /**
     * Adds a sending domain.
     *
     * @param body the request body
     * @return the response
     */
    public DomainData create(StoreDomainData body) {
        return create(body, null);
    }

    /**
     * Adds a sending domain.
     *
     * @param body the request body
     * @param options per-call options, or null
     * @return the response
     */
    public DomainData create(StoreDomainData body, RequestOptions options) {
        return transport.call(Operations.CREATE_DOMAIN, Transport.call("domains.create").body(body).options(options));
    }

    /**
     * Retrieves a domain; {@code include} can add its DNS records.
     *
     * @param domainId the domain id
     * @return the response
     */
    public DomainData retrieve(String domainId) {
        return retrieve(domainId, null, null);
    }

    /**
     * Retrieves a domain; {@code include} can add its DNS records.
     *
     * @param domainId the domain id
     * @param query the query parameters, or null
     * @return the response
     */
    public DomainData retrieve(String domainId, GetDomainQuery query) {
        return retrieve(domainId, query, null);
    }

    /**
     * Retrieves a domain; {@code include} can add its DNS records.
     *
     * @param domainId the domain id
     * @param query the query parameters, or null
     * @param options per-call options, or null
     * @return the response
     */
    public DomainData retrieve(String domainId, GetDomainQuery query, RequestOptions options) {
        return transport.call(Operations.GET_DOMAIN, Transport.call("domains.retrieve").path(domainId).query(query).options(options));
    }

    /**
     * Deletes a domain.
     *
     * @param domainId the domain id
     * @return the response
     */
    public MessageResponse delete(String domainId) {
        return delete(domainId, null);
    }

    /**
     * Deletes a domain.
     *
     * @param domainId the domain id
     * @param options per-call options, or null
     * @return the response
     */
    public MessageResponse delete(String domainId, RequestOptions options) {
        return transport.call(Operations.DELETE_DOMAIN, Transport.call("domains.delete").path(domainId).options(options));
    }

    /**
     * Checks every DNS record of the domain.
     *
     * @param domainId the domain id
     * @return the response
     */
    public DnsVerificationSuccessResponse verifyDnsRecords(String domainId) {
        return verifyDnsRecords(domainId, null);
    }

    /**
     * Checks every DNS record of the domain.
     *
     * @param domainId the domain id
     * @param options per-call options, or null
     * @return the response
     */
    public DnsVerificationSuccessResponse verifyDnsRecords(String domainId, RequestOptions options) {
        return transport.call(Operations.VERIFY_DOMAIN_DNS_RECORDS, Transport.call("domains.verifyDnsRecords").path(domainId).options(options));
    }

    /**
     * Checks one DNS record of the domain.
     *
     * @param domainId the domain id
     * @param recordId the record id
     * @return the response
     */
    public MessageResponse verifyDnsRecord(String domainId, String recordId) {
        return verifyDnsRecord(domainId, recordId, null);
    }

    /**
     * Checks one DNS record of the domain.
     *
     * @param domainId the domain id
     * @param recordId the record id
     * @param options per-call options, or null
     * @return the response
     */
    public MessageResponse verifyDnsRecord(String domainId, String recordId, RequestOptions options) {
        return transport.call(Operations.VERIFY_DOMAIN_DNS_RECORD, Transport.call("domains.verifyDnsRecord").path(domainId, recordId).options(options));
    }

    /**
     * Replaces the projects that may send from the domain. An empty list makes the domain available
     * to every project in the team.
     *
     * @param domainId the domain id
     * @param body the request body
     * @return the response
     */
    public DomainMutationResponse updateProjects(String domainId, UpdateDomainProjectsData body) {
        return updateProjects(domainId, body, null);
    }

    /**
     * Replaces the projects that may send from the domain. An empty list makes the domain available
     * to every project in the team.
     *
     * @param domainId the domain id
     * @param body the request body
     * @param options per-call options, or null
     * @return the response
     */
    public DomainMutationResponse updateProjects(String domainId, UpdateDomainProjectsData body, RequestOptions options) {
        return transport.call(Operations.UPDATE_DOMAIN_PROJECTS, Transport.call("domains.updateProjects").path(domainId).body(body).options(options));
    }

    /**
     * Starts the upgrade of a {@code legacy_txt} domain to managed DKIM (HTTP 202). The response
     * lists two new DKIM CNAME records with verification scope {@code migration}; add them to your
     * DNS. The current DKIM TXT record keeps signing until the upgrade completes. Calling it again
     * returns the upgrade that is in progress, and a domain that already uses managed DKIM returns
     * HTTP 200 without changes. When {@code dkim_transition.state} is {@code ready_to_activate},
     * call {@link #activateDkimUpgrade(String)}.
     *
     * @param domainId the domain id
     * @return the response
     */
    public DomainMutationResponse startDkimUpgrade(String domainId) {
        return startDkimUpgrade(domainId, null);
    }

    /**
     * Starts the upgrade of a {@code legacy_txt} domain to managed DKIM (HTTP 202). The response
     * lists two new DKIM CNAME records with verification scope {@code migration}; add them to your
     * DNS. The current DKIM TXT record keeps signing until the upgrade completes. Calling it again
     * returns the upgrade that is in progress, and a domain that already uses managed DKIM returns
     * HTTP 200 without changes. When {@code dkim_transition.state} is {@code ready_to_activate},
     * call {@link #activateDkimUpgrade(String)}.
     *
     * @param domainId the domain id
     * @param options per-call options, or null
     * @return the response
     */
    public DomainMutationResponse startDkimUpgrade(String domainId, RequestOptions options) {
        return transport.call(Operations.START_DOMAIN_DKIM_UPGRADE, Transport.call("domains.startDkimUpgrade").path(domainId).options(options));
    }

    /**
     * Cancels the managed DKIM upgrade and removes its DKIM CNAME records. The domain keeps its DKIM
     * TXT record. An upgrade can't be cancelled after its grace period started. Without an upgrade
     * in progress nothing changes, and a key rotation is never cancelled.
     *
     * @param domainId the domain id
     * @return the response
     */
    public DomainMutationResponse cancelDkimUpgrade(String domainId) {
        return cancelDkimUpgrade(domainId, null);
    }

    /**
     * Cancels the managed DKIM upgrade and removes its DKIM CNAME records. The domain keeps its DKIM
     * TXT record. An upgrade can't be cancelled after its grace period started. Without an upgrade
     * in progress nothing changes, and a key rotation is never cancelled.
     *
     * @param domainId the domain id
     * @param options per-call options, or null
     * @return the response
     */
    public DomainMutationResponse cancelDkimUpgrade(String domainId, RequestOptions options) {
        return transport.call(Operations.CANCEL_DOMAIN_DKIM_UPGRADE, Transport.call("domains.cancelDkimUpgrade").path(domainId).options(options));
    }

    /**
     * Activates the managed DKIM upgrade. Lettermint checks both DKIM CNAME records again, then
     * starts a grace period in which both keys are published. The upgrade completes at
     * {@code dkim_transition.completes_after}; {@code dkim_mode} then changes to
     * {@code managed_cname} and you can remove the old DKIM TXT record. Calling it again during the
     * grace period, or after the upgrade completed, changes nothing.
     *
     * @param domainId the domain id
     * @return the response
     */
    public DomainMutationResponse activateDkimUpgrade(String domainId) {
        return activateDkimUpgrade(domainId, null);
    }

    /**
     * Activates the managed DKIM upgrade. Lettermint checks both DKIM CNAME records again, then
     * starts a grace period in which both keys are published. The upgrade completes at
     * {@code dkim_transition.completes_after}; {@code dkim_mode} then changes to
     * {@code managed_cname} and you can remove the old DKIM TXT record. Calling it again during the
     * grace period, or after the upgrade completed, changes nothing.
     *
     * @param domainId the domain id
     * @param options per-call options, or null
     * @return the response
     */
    public DomainMutationResponse activateDkimUpgrade(String domainId, RequestOptions options) {
        return transport.call(Operations.ACTIVATE_DOMAIN_DKIM_UPGRADE, Transport.call("domains.activateDkimUpgrade").path(domainId).options(options));
    }

    /**
     * Starts a DKIM key rotation for a domain with managed DKIM (HTTP 202). The DKIM CNAME records
     * stay the same, so no DNS change is needed: Lettermint publishes the new key and keeps both keys
     * published during a grace period. Lettermint also rotates keys on a schedule, so you only need
     * this to replace a key early. Calling it again during a rotation returns the rotation that is
     * in progress.
     *
     * @param domainId the domain id
     * @return the response
     */
    public DomainMutationResponse rotateDkimKey(String domainId) {
        return rotateDkimKey(domainId, null);
    }

    /**
     * Starts a DKIM key rotation for a domain with managed DKIM (HTTP 202). The DKIM CNAME records
     * stay the same, so no DNS change is needed: Lettermint publishes the new key and keeps both keys
     * published during a grace period. Lettermint also rotates keys on a schedule, so you only need
     * this to replace a key early. Calling it again during a rotation returns the rotation that is
     * in progress.
     *
     * @param domainId the domain id
     * @param options per-call options, or null
     * @return the response
     */
    public DomainMutationResponse rotateDkimKey(String domainId, RequestOptions options) {
        return transport.call(Operations.ROTATE_DOMAIN_DKIM_KEY, Transport.call("domains.rotateDkimKey").path(domainId).options(options));
    }

    @Override
    public String toString() {
        return "Domains{}";
    }
}
