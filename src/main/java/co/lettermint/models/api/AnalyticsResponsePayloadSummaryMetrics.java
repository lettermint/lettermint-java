package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyticsResponsePayloadSummaryMetrics {
    @JsonProperty("accepted")
    public Integer accepted;

    @JsonProperty("processed")
    public Integer processed;

    @JsonProperty("suppressed")
    public Integer suppressed;

    @JsonProperty("policy_rejected")
    public Integer policyRejected;

    @JsonProperty("application_failed")
    public Integer applicationFailed;

    @JsonProperty("mta_accepted")
    public Integer mtaAccepted;

    @JsonProperty("canceled")
    public Integer canceled;

    @JsonProperty("messages")
    public Integer messages;

    @JsonProperty("delivered")
    public Integer delivered;

    @JsonProperty("bounced")
    public Integer bounced;

    @JsonProperty("soft_bounced")
    public Integer softBounced;

    @JsonProperty("administratively_bounced")
    public Integer administrativelyBounced;

    @JsonProperty("deferred_recipients")
    public Integer deferredRecipients;

    @JsonProperty("deferred_events")
    public Integer deferredEvents;

    @JsonProperty("delivery_attempts")
    public Integer deliveryAttempts;

    @JsonProperty("attempted_recipients")
    public Integer attemptedRecipients;

    @JsonProperty("transport_outcome_recipients")
    public Integer transportOutcomeRecipients;

    @JsonProperty("effective_delivered")
    public Integer effectiveDelivered;

    @JsonProperty("open_tracked_delivered")
    public Integer openTrackedDelivered;

    @JsonProperty("click_tracked_delivered")
    public Integer clickTrackedDelivered;

    @JsonProperty("out_of_band_bounced_recipients")
    public Integer outOfBandBouncedRecipients;

    @JsonProperty("out_of_band_bounce_events")
    public Integer outOfBandBounceEvents;

    @JsonProperty("complained")
    public Integer complained;

    @JsonProperty("unsubscribed")
    public Integer unsubscribed;

    @JsonProperty("human_opens")
    public Integer humanOpens;

    @JsonProperty("human_opens_events")
    public Integer humanOpensEvents;

    @JsonProperty("human_clicks")
    public Integer humanClicks;

    @JsonProperty("human_clicks_events")
    public Integer humanClicksEvents;

    @JsonProperty("machine_opens")
    public Integer machineOpens;

    @JsonProperty("machine_opens_events")
    public Integer machineOpensEvents;

    @JsonProperty("machine_clicks")
    public Integer machineClicks;

    @JsonProperty("machine_clicks_events")
    public Integer machineClicksEvents;

    @JsonProperty("privacy_opens")
    public Integer privacyOpens;

    @JsonProperty("privacy_opens_events")
    public Integer privacyOpensEvents;

    @JsonProperty("privacy_clicks")
    public Integer privacyClicks;

    @JsonProperty("privacy_clicks_events")
    public Integer privacyClicksEvents;

    @JsonProperty("bot_opens")
    public Integer botOpens;

    @JsonProperty("bot_opens_events")
    public Integer botOpensEvents;

    @JsonProperty("bot_clicks")
    public Integer botClicks;

    @JsonProperty("bot_clicks_events")
    public Integer botClicksEvents;

    @JsonProperty("scanner_opens")
    public Integer scannerOpens;

    @JsonProperty("scanner_opens_events")
    public Integer scannerOpensEvents;

    @JsonProperty("scanner_clicks")
    public Integer scannerClicks;

    @JsonProperty("scanner_clicks_events")
    public Integer scannerClicksEvents;

    @JsonProperty("observed_opens")
    public Integer observedOpens;

    @JsonProperty("observed_opens_events")
    public Integer observedOpensEvents;

    @JsonProperty("observed_clicks")
    public Integer observedClicks;

    @JsonProperty("observed_clicks_events")
    public Integer observedClicksEvents;

    @JsonProperty("delivery_rate")
    public Double deliveryRate;

    @JsonProperty("effective_delivery_rate")
    public Double effectiveDeliveryRate;

    @JsonProperty("bounce_rate")
    public Double bounceRate;

    @JsonProperty("deferral_rate")
    public Double deferralRate;

    @JsonProperty("complaint_rate")
    public Double complaintRate;

    @JsonProperty("human_open_rate")
    public Double humanOpenRate;

    @JsonProperty("human_click_rate")
    public Double humanClickRate;

    @JsonProperty("processing_latency_p50_ms")
    public Double processingLatencyP50Ms;

    @JsonProperty("processing_latency_p95_ms")
    public Double processingLatencyP95Ms;

    @JsonProperty("processing_latency_p99_ms")
    public Double processingLatencyP99Ms;

    @JsonProperty("processing_latency_samples")
    public Integer processingLatencySamples;

    @JsonProperty("delivery_latency_p50_ms")
    public Double deliveryLatencyP50Ms;

    @JsonProperty("delivery_latency_p95_ms")
    public Double deliveryLatencyP95Ms;

    @JsonProperty("delivery_latency_p99_ms")
    public Double deliveryLatencyP99Ms;

    @JsonProperty("delivery_latency_samples")
    public Integer deliveryLatencySamples;

    @JsonProperty("total_latency_p50_ms")
    public Double totalLatencyP50Ms;

    @JsonProperty("total_latency_p95_ms")
    public Double totalLatencyP95Ms;

    @JsonProperty("total_latency_p99_ms")
    public Double totalLatencyP99Ms;

    @JsonProperty("total_latency_samples")
    public Integer totalLatencySamples;
}
