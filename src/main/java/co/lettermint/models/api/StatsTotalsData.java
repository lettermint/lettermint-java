package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StatsTotalsData {
    @JsonProperty("sent")
    public Integer sent;

    @JsonProperty("delivered")
    public Integer delivered;

    @JsonProperty("hard_bounced")
    public Integer hardBounced;

    @JsonProperty("spam_complaints")
    public Integer spamComplaints;

    @JsonProperty("opened")
    public Integer opened;

    @JsonProperty("clicked")
    public Integer clicked;

    @JsonProperty("inbound")
    public StatsInboundData inbound;

    @JsonProperty("transactional")
    public StatsTypeData transactional;

    @JsonProperty("broadcast")
    public StatsTypeData broadcast;

    @JsonProperty("effective_opened")
    public Integer effectiveOpened;

    @JsonProperty("machine_opened")
    public Integer machineOpened;

    @JsonProperty("machine_clicked")
    public Integer machineClicked;
}
