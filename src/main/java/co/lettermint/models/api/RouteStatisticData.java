package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RouteStatisticData {
    @JsonProperty("date")
    public String date;

    @JsonProperty("sent_count")
    public Integer sentCount;

    @JsonProperty("delivered_count")
    public Integer deliveredCount;

    @JsonProperty("opened_count")
    public Integer openedCount;

    @JsonProperty("clicked_count")
    public Integer clickedCount;

    @JsonProperty("hard_bounce_count")
    public Integer hardBounceCount;

    @JsonProperty("spam_complaint_count")
    public Integer spamComplaintCount;

    @JsonProperty("inbound_received_count")
    public Integer inboundReceivedCount;

    @JsonProperty("observed_opened_count")
    public Integer observedOpenedCount;

    @JsonProperty("human_opened_count")
    public Integer humanOpenedCount;

    @JsonProperty("privacy_opened_count")
    public Integer privacyOpenedCount;

    @JsonProperty("effective_opened_count")
    public Integer effectiveOpenedCount;

    @JsonProperty("machine_opened_count")
    public Integer machineOpenedCount;

    @JsonProperty("machine_clicked_count")
    public Integer machineClickedCount;
}
