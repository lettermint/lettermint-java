package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BlockedFileTypesResponse {
    @JsonProperty("extensions")
    public List<String> extensions;

    @JsonProperty("mime_types")
    public List<String> mimeTypes;
}
