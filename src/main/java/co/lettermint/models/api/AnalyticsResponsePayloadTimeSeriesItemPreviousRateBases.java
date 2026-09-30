package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyticsResponsePayloadTimeSeriesItemPreviousRateBases {
    @JsonProperty("delivery_rate")
    public AnalyticsResponsePayloadTimeSeriesItemPreviousRateBasesDeliveryRate deliveryRate;

    @JsonProperty("effective_delivery_rate")
    public AnalyticsResponsePayloadTimeSeriesItemPreviousRateBasesEffectiveDeliveryRate effectiveDeliveryRate;

    @JsonProperty("bounce_rate")
    public AnalyticsResponsePayloadTimeSeriesItemPreviousRateBasesBounceRate bounceRate;

    @JsonProperty("deferral_rate")
    public AnalyticsResponsePayloadTimeSeriesItemPreviousRateBasesDeferralRate deferralRate;

    @JsonProperty("complaint_rate")
    public AnalyticsResponsePayloadTimeSeriesItemPreviousRateBasesComplaintRate complaintRate;

    @JsonProperty("human_open_rate")
    public AnalyticsResponsePayloadTimeSeriesItemPreviousRateBasesHumanOpenRate humanOpenRate;

    @JsonProperty("human_click_rate")
    public AnalyticsResponsePayloadTimeSeriesItemPreviousRateBasesHumanClickRate humanClickRate;
}
