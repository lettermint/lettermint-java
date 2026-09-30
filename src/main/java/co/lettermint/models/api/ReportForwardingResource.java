package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportForwardingResource {
    @JsonProperty("destination")
    public String destination;

    @JsonProperty("verified")
    public Boolean verified;

    @JsonProperty("verified_at")
    public String verifiedAt;
}
