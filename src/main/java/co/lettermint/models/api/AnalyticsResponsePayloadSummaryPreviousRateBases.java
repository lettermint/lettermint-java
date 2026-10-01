package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyticsResponsePayloadSummaryPreviousRateBases {
    @JsonProperty("delivery_rate")
    public AnalyticsResponsePayloadSummaryPreviousRateBasesDeliveryRate deliveryRate;

    @JsonProperty("effective_delivery_rate")
    public AnalyticsResponsePayloadSummaryPreviousRateBasesEffectiveDeliveryRate effectiveDeliveryRate;

    @JsonProperty("bounce_rate")
    public AnalyticsResponsePayloadSummaryPreviousRateBasesBounceRate bounceRate;

    @JsonProperty("deferral_rate")
    public AnalyticsResponsePayloadSummaryPreviousRateBasesDeferralRate deferralRate;

    @JsonProperty("complaint_rate")
    public AnalyticsResponsePayloadSummaryPreviousRateBasesComplaintRate complaintRate;

    @JsonProperty("human_open_rate")
    public AnalyticsResponsePayloadSummaryPreviousRateBasesHumanOpenRate humanOpenRate;

    @JsonProperty("human_click_rate")
    public AnalyticsResponsePayloadSummaryPreviousRateBasesHumanClickRate humanClickRate;
}
