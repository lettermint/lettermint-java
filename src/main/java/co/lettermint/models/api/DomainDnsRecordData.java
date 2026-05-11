package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DomainDnsRecordData {
    @JsonProperty("id")
    public String id;

    @JsonProperty("type")
    public RecordType type;

    @JsonProperty("hostname")
    public String hostname;

    @JsonProperty("fqdn")
    public String fqdn;

    @JsonProperty("content")
    public String content;

    @JsonProperty("status")
    public DnsRecordStatus status;

    @JsonProperty("verified_at")
    public String verifiedAt;

    @JsonProperty("last_checked_at")
    public String lastCheckedAt;
}
