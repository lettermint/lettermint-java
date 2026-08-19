package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateRouteSettingsData {
    @JsonProperty("track_opens")
    public Boolean trackOpens;

    @JsonProperty("track_clicks")
    public Boolean trackClicks;

    @JsonProperty("generate_plaintext_fallback")
    public Boolean generatePlaintextFallback;

    @JsonProperty("suppress_auto_responders")
    public Boolean suppressAutoResponders;

    @JsonProperty("suppress_disposable_recipients")
    public Boolean suppressDisposableRecipients;

    @JsonProperty("tls")
    public String tls;

    @JsonProperty("disable_hosted_unsubscribe")
    public Boolean disableHostedUnsubscribe;

    @JsonProperty("redact_email_content")
    public Boolean redactEmailContent;
}
