package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyticsResponsePayloadBreakdownItemPreviousRateBases {
    @JsonProperty("delivery_rate")
    public AnalyticsResponsePayloadBreakdownItemPreviousRateBasesDeliveryRate deliveryRate;

    @JsonProperty("effective_delivery_rate")
    public AnalyticsResponsePayloadBreakdownItemPreviousRateBasesEffectiveDeliveryRate effectiveDeliveryRate;

    @JsonProperty("bounce_rate")
    public AnalyticsResponsePayloadBreakdownItemPreviousRateBasesBounceRate bounceRate;

    @JsonProperty("deferral_rate")
    public AnalyticsResponsePayloadBreakdownItemPreviousRateBasesDeferralRate deferralRate;

    @JsonProperty("complaint_rate")
    public AnalyticsResponsePayloadBreakdownItemPreviousRateBasesComplaintRate complaintRate;

    @JsonProperty("human_open_rate")
    public AnalyticsResponsePayloadBreakdownItemPreviousRateBasesHumanOpenRate humanOpenRate;

    @JsonProperty("human_click_rate")
    public AnalyticsResponsePayloadBreakdownItemPreviousRateBasesHumanClickRate humanClickRate;
}
