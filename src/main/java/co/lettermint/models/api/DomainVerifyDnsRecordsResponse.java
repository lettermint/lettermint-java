package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DomainVerifyDnsRecordsResponse {
    @JsonProperty("message")
    public String message;

    @JsonProperty("recommended_failed_records")
    public List<Map<String, Object>> recommendedFailedRecords;
}
