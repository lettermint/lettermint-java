package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DomainData {
    @JsonProperty("id")
    public String id;

    @JsonProperty("domain")
    public String domain;

    @JsonProperty("status_changed_at")
    public String statusChangedAt;

    @JsonProperty("dns_records")
    public List<DomainDnsRecordData> dnsRecords;

    @JsonProperty("projects")
    public List<Map<String, Object>> projects;

    @JsonProperty("created_at")
    public String createdAt;
}
