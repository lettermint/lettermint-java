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
     * Replaces the projects that may send from the domain.
     *
     * @param domainId the domain id
     * @param body the request body
     * @return the response
     */
    public DomainMutationResponse updateProjects(String domainId, UpdateDomainProjectsData body) {
        return updateProjects(domainId, body, null);
    }

    /**
     * Replaces the projects that may send from the domain.
     *
     * @param domainId the domain id
     * @param body the request body
     * @param options per-call options, or null
     * @return the response
     */
    public DomainMutationResponse updateProjects(String domainId, UpdateDomainProjectsData body, RequestOptions options) {
        return transport.call(Operations.UPDATE_DOMAIN_PROJECTS, Transport.call("domains.updateProjects").path(domainId).body(body).options(options));
    }

    @Override
    public String toString() {
        return "Domains{}";
    }
}
