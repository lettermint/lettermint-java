package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyticsResponsePayloadBreakdownItemTrendItemPreviousRateBases {
    @JsonProperty("delivery_rate")
    public AnalyticsResponsePayloadBreakdownItemTrendItemPreviousRateBasesDeliveryRate deliveryRate;

    @JsonProperty("effective_delivery_rate")
    public AnalyticsResponsePayloadBreakdownItemTrendItemPreviousRateBasesEffectiveDeliveryRate effectiveDeliveryRate;

    @JsonProperty("bounce_rate")
    public AnalyticsResponsePayloadBreakdownItemTrendItemPreviousRateBasesBounceRate bounceRate;

    @JsonProperty("deferral_rate")
    public AnalyticsResponsePayloadBreakdownItemTrendItemPreviousRateBasesDeferralRate deferralRate;

    @JsonProperty("complaint_rate")
    public AnalyticsResponsePayloadBreakdownItemTrendItemPreviousRateBasesComplaintRate complaintRate;

    @JsonProperty("human_open_rate")
    public AnalyticsResponsePayloadBreakdownItemTrendItemPreviousRateBasesHumanOpenRate humanOpenRate;

    @JsonProperty("human_click_rate")
    public AnalyticsResponsePayloadBreakdownItemTrendItemPreviousRateBasesHumanClickRate humanClickRate;
}
