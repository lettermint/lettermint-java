package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CancelScheduledMessageResponse extends RescheduleMessageResponse {
}
