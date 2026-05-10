package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageStatsData {
    @JsonProperty("messages_transactional")
    public Integer messagesTransactional;

    @JsonProperty("messages_broadcast")
    public Integer messagesBroadcast;

    @JsonProperty("messages_inbound")
    public Integer messagesInbound;

    @JsonProperty("deliverability")
    public Double deliverability;
}
