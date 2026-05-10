package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DomainListData {
    @JsonProperty("id")
    public String id;

    @JsonProperty("domain")
    public String domain;

    @JsonProperty("status")
    public DomainStatus status;

    @JsonProperty("status_changed_at")
    public String statusChangedAt;

    @JsonProperty("created_at")
    public String createdAt;
}
