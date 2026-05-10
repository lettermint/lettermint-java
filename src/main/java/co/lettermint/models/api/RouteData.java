package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RouteData {
    @JsonProperty("id")
    public String id;

    @JsonProperty("project_id")
    public String projectId;

    @JsonProperty("slug")
    public String slug;

    @JsonProperty("name")
    public String name;

    @JsonProperty("route_type")
    public RouteType routeType;

    @JsonProperty("is_default")
    public Boolean isDefault;

    @JsonProperty("inbound_address")
    public String inboundAddress;

    @JsonProperty("inbound_domain")
    public String inboundDomain;

    @JsonProperty("inbound_domain_verified_at")
    public String inboundDomainVerifiedAt;

    @JsonProperty("inbound_spam_threshold")
    public Double inboundSpamThreshold;

    @JsonProperty("attachment_delivery")
    public AttachmentDelivery attachmentDelivery;

    @JsonProperty("project")
    public ProjectData project;

    @JsonProperty("webhooks_count")
    public Integer webhooksCount;

    @JsonProperty("suppressed_recipients_count")
    public Integer suppressedRecipientsCount;

    @JsonProperty("statistics")
    public Object statistics;

    @JsonProperty("created_at")
    public String createdAt;

    @JsonProperty("updated_at")
    public String updatedAt;
}
