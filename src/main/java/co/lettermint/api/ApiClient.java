package co.lettermint.api;

import co.lettermint.client.LettermintClient;
import co.lettermint.models.api.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class ApiClient {
    private final LettermintClient client;
    private final DomainsEndpoint domains;
    private final MessagesEndpoint messages;
    private final ProjectsEndpoint projects;
    private final RoutesEndpoint routes;
    private final StatsEndpoint stats;
    private final SuppressionsEndpoint suppressions;
    private final TeamEndpoint team;
    private final WebhooksEndpoint webhooks;

    public ApiClient(String apiToken, String baseUrl) {
        this.client = new LettermintClient(apiToken, baseUrl, LettermintClient.AuthMode.BEARER);
        this.domains = new DomainsEndpoint(client);
        this.messages = new MessagesEndpoint(client);
        this.projects = new ProjectsEndpoint(client);
        this.routes = new RoutesEndpoint(client);
        this.stats = new StatsEndpoint(client);
        this.suppressions = new SuppressionsEndpoint(client);
        this.team = new TeamEndpoint(client);
        this.webhooks = new WebhooksEndpoint(client);
    }

    public String ping() {
        return client.getRaw("/ping").trim();
    }

    public BlockedFileTypesResponse blockedFileTypes() {
        return client.get("/blocked-file-types", BlockedFileTypesResponse.class);
    }

    public DomainsEndpoint domains() {
        return domains;
    }

    public MessagesEndpoint messages() {
        return messages;
    }

    public ProjectsEndpoint projects() {
        return projects;
    }

    public RoutesEndpoint routes() {
        return routes;
    }

    public StatsEndpoint stats() {
        return stats;
    }

    public SuppressionsEndpoint suppressions() {
        return suppressions;
    }

    public TeamEndpoint team() {
        return team;
    }

    public WebhooksEndpoint webhooks() {
        return webhooks;
    }

    private static String segment(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static class DomainsEndpoint {
        private final LettermintClient client;

        DomainsEndpoint(LettermintClient client) {
            this.client = client;
        }

        public DomainIndexResponse list() {
            return list(null);
        }

        public DomainIndexResponse list(Map<String, String> query) {
            return client.get("/domains", DomainIndexResponse.class, query);
        }

        public DomainData create(StoreDomainData payload) {
            return client.post("/domains", payload, DomainData.class);
        }

        public DomainData retrieve(String domainId) {
            return client.get("/domains/" + segment(domainId), DomainData.class);
        }

        public DomainDestroyResponse delete(String domainId) {
            return client.delete("/domains/" + segment(domainId), DomainDestroyResponse.class);
        }

        public DomainVerifyDnsRecordsResponse verifyDnsRecords(String domainId) {
            return client.post("/domains/" + segment(domainId) + "/dns-records/verify", null, DomainVerifyDnsRecordsResponse.class);
        }

        public DomainVerifySpecificDnsRecordResponse verifyDnsRecord(String domainId, String recordId) {
            return client.post("/domains/" + segment(domainId) + "/dns-records/" + segment(recordId) + "/verify", null, DomainVerifySpecificDnsRecordResponse.class);
        }

        public DomainUpdateProjectsResponse updateProjects(String domainId, UpdateDomainProjectsData payload) {
            return client.put("/domains/" + segment(domainId) + "/projects", payload, DomainUpdateProjectsResponse.class);
        }
    }

    public static class MessagesEndpoint {
        private final LettermintClient client;

        MessagesEndpoint(LettermintClient client) {
            this.client = client;
        }

        public MessageIndexResponse list() {
            return list(null);
        }

        public MessageIndexResponse list(Map<String, String> query) {
            return client.get("/messages", MessageIndexResponse.class, query);
        }

        public MessageData retrieve(String messageId) {
            return client.get("/messages/" + segment(messageId), MessageData.class);
        }

        public MessageEventsResponse events(String messageId) {
            return events(messageId, null);
        }

        public MessageEventsResponse events(String messageId, Map<String, String> query) {
            return client.get("/messages/" + segment(messageId) + "/events", MessageEventsResponse.class, query);
        }

        public String source(String messageId) {
            return client.getRaw("/messages/" + segment(messageId) + "/source");
        }

        public String html(String messageId) {
            return client.getRaw("/messages/" + segment(messageId) + "/html");
        }

        public String text(String messageId) {
            return client.getRaw("/messages/" + segment(messageId) + "/text");
        }
    }

    public static class ProjectsEndpoint {
        private final LettermintClient client;

        ProjectsEndpoint(LettermintClient client) {
            this.client = client;
        }

        public ProjectIndexResponse list() {
            return list(null);
        }

        public ProjectIndexResponse list(Map<String, String> query) {
            return client.get("/projects", ProjectIndexResponse.class, query);
        }

        public ProjectStoreResponse create(StoreProjectData payload) {
            return client.post("/projects", payload, ProjectStoreResponse.class);
        }

        public ProjectData retrieve(String projectId) {
            return client.get("/projects/" + segment(projectId), ProjectData.class);
        }

        public ProjectUpdateResponse update(String projectId, UpdateProjectData payload) {
            return client.put("/projects/" + segment(projectId), payload, ProjectUpdateResponse.class);
        }

        public ProjectDestroyResponse delete(String projectId) {
            return client.delete("/projects/" + segment(projectId), ProjectDestroyResponse.class);
        }

        public ProjectRotateTokenResponse rotateToken(String projectId) {
            return client.post("/projects/" + segment(projectId) + "/rotate-token", null, ProjectRotateTokenResponse.class);
        }

        public RouteIndexResponse routes(String projectId) {
            return routes(projectId, null);
        }

        public RouteIndexResponse routes(String projectId, Map<String, String> query) {
            return client.get("/projects/" + segment(projectId) + "/routes", RouteIndexResponse.class, query);
        }

        public RouteStoreResponse createRoute(String projectId, StoreRouteData payload) {
            return client.post("/projects/" + segment(projectId) + "/routes", payload, RouteStoreResponse.class);
        }
    }

    public static class RoutesEndpoint {
        private final LettermintClient client;

        RoutesEndpoint(LettermintClient client) {
            this.client = client;
        }

        public RouteData retrieve(String routeId) {
            return client.get("/routes/" + segment(routeId), RouteData.class);
        }

        public RouteUpdateResponse update(String routeId, UpdateRouteData payload) {
            return client.put("/routes/" + segment(routeId), payload, RouteUpdateResponse.class);
        }

        public RouteDestroyResponse delete(String routeId) {
            return client.delete("/routes/" + segment(routeId), RouteDestroyResponse.class);
        }

        public RouteVerifyInboundDomainResponse verifyInboundDomain(String routeId) {
            return client.post("/routes/" + segment(routeId) + "/verify-inbound-domain", null, RouteVerifyInboundDomainResponse.class);
        }
    }

    public static class StatsEndpoint {
        private final LettermintClient client;

        StatsEndpoint(LettermintClient client) {
            this.client = client;
        }

        public StatsData retrieve() {
            return retrieve(null);
        }

        public StatsData retrieve(Map<String, String> query) {
            return client.get("/stats", StatsData.class, query);
        }
    }

    public static class SuppressionsEndpoint {
        private final LettermintClient client;

        SuppressionsEndpoint(LettermintClient client) {
            this.client = client;
        }

        public SuppressionIndexResponse list() {
            return list(null);
        }

        public SuppressionIndexResponse list(Map<String, String> query) {
            return client.get("/suppressions", SuppressionIndexResponse.class, query);
        }

        public SuppressionStoreResponse create(StoreSuppressionData payload) {
            return client.post("/suppressions", payload, SuppressionStoreResponse.class);
        }

        public SuppressionDestroyResponse delete(String suppressionId) {
            return client.delete("/suppressions/" + segment(suppressionId), SuppressionDestroyResponse.class);
        }
    }

    public static class TeamEndpoint {
        private final LettermintClient client;

        TeamEndpoint(LettermintClient client) {
            this.client = client;
        }

        public TeamData retrieve() {
            return client.get("/team", TeamData.class);
        }

        public TeamUpdateResponse update(UpdateTeamData payload) {
            return client.put("/team", payload, TeamUpdateResponse.class);
        }

        public TeamUsageDetailData usage() {
            return usage(null);
        }

        public TeamUsageDetailData usage(Map<String, String> query) {
            return client.get("/team/usage", TeamUsageDetailData.class, query);
        }

        public TeamRolesResponse roles() {
            return client.get("/team/roles", TeamRolesResponse.class);
        }

        public TeamMembersResponse members() {
            return members(null);
        }

        public TeamMembersResponse members(Map<String, String> query) {
            return client.get("/team/members", TeamMembersResponse.class, query);
        }

        public TeamMemberData member(String userId) {
            return client.get("/team/members/" + segment(userId), TeamMemberData.class);
        }

        public TeamMemberData updateMemberAssignment(String userId, UpdateTeamMemberAssignmentData payload) {
            return client.put("/team/members/" + segment(userId) + "/assignment", payload, TeamMemberData.class);
        }
    }

    public static class WebhooksEndpoint {
        private final LettermintClient client;

        WebhooksEndpoint(LettermintClient client) {
            this.client = client;
        }

        public WebhookIndexResponse list() {
            return list(null);
        }

        public WebhookIndexResponse list(Map<String, String> query) {
            return client.get("/webhooks", WebhookIndexResponse.class, query);
        }

        public WebhookStoreResponse create(StoreWebhookData payload) {
            return client.post("/webhooks", payload, WebhookStoreResponse.class);
        }

        public WebhookData retrieve(String webhookId) {
            return client.get("/webhooks/" + segment(webhookId), WebhookData.class);
        }

        public WebhookUpdateResponse update(String webhookId, UpdateWebhookData payload) {
            return client.put("/webhooks/" + segment(webhookId), payload, WebhookUpdateResponse.class);
        }

        public WebhookDestroyResponse delete(String webhookId) {
            return client.delete("/webhooks/" + segment(webhookId), WebhookDestroyResponse.class);
        }

        public WebhookTestResponse test(String webhookId) {
            return client.post("/webhooks/" + segment(webhookId) + "/test", null, WebhookTestResponse.class);
        }

        public WebhookRegenerateSecretResponse regenerateSecret(String webhookId) {
            return client.post("/webhooks/" + segment(webhookId) + "/regenerate-secret", null, WebhookRegenerateSecretResponse.class);
        }

        public WebhookDeliveriesResponse deliveries(String webhookId) {
            return deliveries(webhookId, null);
        }

        public WebhookDeliveriesResponse deliveries(String webhookId, Map<String, String> query) {
            return client.get("/webhooks/" + segment(webhookId) + "/deliveries", WebhookDeliveriesResponse.class, query);
        }

        public WebhookDeliveryData delivery(String webhookId, String deliveryId) {
            return client.get("/webhooks/" + segment(webhookId) + "/deliveries/" + segment(deliveryId), WebhookDeliveryData.class);
        }
    }
}
