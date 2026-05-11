package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpamSymbol {
    @JsonProperty("name")
    public String name;

    @JsonProperty("score")
    public Double score;

    @JsonProperty("options")
    public List<String> options;

    @JsonProperty("description")
    public String description;
}
