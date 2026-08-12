package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateRouteInboundSettingsData {
    @JsonProperty("inbound_domain")
    public String inboundDomain;

    @JsonProperty("inbound_spam_threshold")
    public Double inboundSpamThreshold;

    @JsonProperty("attachment_delivery")
    public String attachmentDelivery;
}
