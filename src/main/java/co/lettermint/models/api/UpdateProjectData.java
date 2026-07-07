package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateProjectData {
    @JsonProperty("name")
    public String name;

    @JsonProperty("smtp_enabled")
    public Boolean smtpEnabled;

    @JsonProperty("redact_email_content")
    public Boolean redactEmailContent;

    @JsonProperty("default_route_id")
    public String defaultRouteId;
}
