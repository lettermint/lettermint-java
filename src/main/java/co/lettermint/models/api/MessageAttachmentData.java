package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageAttachmentData {
    @JsonProperty("size")
    public Integer size;

    @JsonProperty("filename")
    public String filename;

    @JsonProperty("content_id")
    public String contentId;

    @JsonProperty("content_type")
    public String contentType;
}
