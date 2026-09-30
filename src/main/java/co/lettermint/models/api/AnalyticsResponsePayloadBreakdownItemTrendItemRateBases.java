package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyticsResponsePayloadBreakdownItemTrendItemRateBases {
    @JsonProperty("delivery_rate")
    public AnalyticsResponsePayloadBreakdownItemTrendItemRateBasesDeliveryRate deliveryRate;

    @JsonProperty("effective_delivery_rate")
    public AnalyticsResponsePayloadBreakdownItemTrendItemRateBasesEffectiveDeliveryRate effectiveDeliveryRate;

    @JsonProperty("bounce_rate")
    public AnalyticsResponsePayloadBreakdownItemTrendItemRateBasesBounceRate bounceRate;

    @JsonProperty("deferral_rate")
    public AnalyticsResponsePayloadBreakdownItemTrendItemRateBasesDeferralRate deferralRate;

    @JsonProperty("complaint_rate")
    public AnalyticsResponsePayloadBreakdownItemTrendItemRateBasesComplaintRate complaintRate;

    @JsonProperty("human_open_rate")
    public AnalyticsResponsePayloadBreakdownItemTrendItemRateBasesHumanOpenRate humanOpenRate;

    @JsonProperty("human_click_rate")
    public AnalyticsResponsePayloadBreakdownItemTrendItemRateBasesHumanClickRate humanClickRate;
}
