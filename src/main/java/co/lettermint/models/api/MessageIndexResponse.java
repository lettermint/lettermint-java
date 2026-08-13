package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageIndexResponse {
    @JsonProperty("data")
    public List<MessageListData> data;

    @JsonProperty("links")
    public List<String> links;

    @JsonProperty("meta")
    public Map<String, Object> meta;
}
