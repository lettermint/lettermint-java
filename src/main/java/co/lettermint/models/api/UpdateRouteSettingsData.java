package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateRouteSettingsData {
    @JsonProperty("track_opens")
    public Boolean trackOpens;

    @JsonProperty("track_clicks")
    public Boolean trackClicks;

    @JsonProperty("disable_plaintext_generation")
    public Boolean disablePlaintextGeneration;

    @JsonProperty("disable_hosted_unsubscribe")
    public Boolean disableHostedUnsubscribe;

    @JsonProperty("redact_email_content")
    public Boolean redactEmailContent;
}
