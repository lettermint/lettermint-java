package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RouteVerifyInboundDomainResponse {
    @JsonProperty("data")
    public Map<String, Object> data;
}
