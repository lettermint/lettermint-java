package co.lettermint;

import co.lettermint.api.ApiClient;
import co.lettermint.models.api.SendMailRequest;
import co.lettermint.models.api.SendMailResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiClientTest {

    @Test
    void staticEmailUsesSendingTokenAndRawPing() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse().setBody("pong\n"));
            server.start();

            String pong = Lettermint.email("sending-token", server.url("/v1").toString()).ping();

            RecordedRequest request = server.takeRequest();
            assertEquals("/v1/ping", request.getPath());
            assertEquals("sending-token", request.getHeader("x-lettermint-token"));
            assertNull(request.getHeader("Authorization"));
            assertEquals("pong", pong);
        }
    }

    @Test
    void staticApiUsesBearerTokenAndRawPing() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse().setBody("pong\n"));
            server.start();

            ApiClient api = Lettermint.api("api-token", server.url("/v1").toString());
            String pong = api.ping();

            RecordedRequest request = server.takeRequest();
            assertEquals("/v1/ping", request.getPath());
            assertEquals("Bearer api-token", request.getHeader("Authorization"));
            assertNull(request.getHeader("x-lettermint-token"));
            assertEquals("pong", pong);
        }
    }

    @Test
    void sendsBatchEmailsWithTypedPayloads() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .setHeader("Content-Type", "application/json")
                    .setBody("[{\"message_id\":\"msg_123\",\"status\":\"queued\"}]"));
            server.start();

            SendMailRequest payload = new SendMailRequest();
            payload.fromValue = "from@example.com";
            payload.to = Collections.singletonList("to@example.com");
            payload.subject = "Hello";

            List<SendMailResponse> response = Lettermint
                    .email("sending-token", server.url("/v1").toString())
                    .sendBatch(Collections.singletonList(payload));

            RecordedRequest request = server.takeRequest();
            assertEquals("/v1/send/batch", request.getPath());
            assertEquals("sending-token", request.getHeader("x-lettermint-token"));
            assertEquals("msg_123", response.get(0).messageId);
        }
    }

    @Test
    void apiEndpointPathsAreEncodedAndTyped() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse().setHeader("Content-Type", "application/json").setBody("{\"id\":\"domain_123\"}"));
            server.enqueue(new MockResponse().setBody("<html></html>"));
            server.enqueue(new MockResponse().setHeader("Content-Type", "application/json").setBody("{\"data\":{\"verified\":true}}"));
            server.start();

            ApiClient api = Lettermint.api("api-token", server.url("/v1").toString());

            assertEquals("domain_123", api.domains().retrieve("domain/id").id);
            assertEquals("<html></html>", api.messages().html("msg/id"));
            assertTrue(api.routes().verifyInboundDomain("route/id").data.containsKey("verified"));

            assertEquals("/v1/domains/domain%2Fid", server.takeRequest().getPath());
            assertEquals("/v1/messages/msg%2Fid/html", server.takeRequest().getPath());
            assertEquals("/v1/routes/route%2Fid/verify-inbound-domain", server.takeRequest().getPath());
        }
    }

    @Test
    void rawMessageEndpointsPreserveResponseBody() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            String rawBody = "Subject: Test\r\n\r\nBody with trailing newline\n";
            server.enqueue(new MockResponse().setBody(rawBody));
            server.start();

            ApiClient api = Lettermint.api("api-token", server.url("/v1").toString());

            assertEquals(rawBody, api.messages().source("msg_123"));
        }
    }

    @Test
    void publicClientRejectsAbsoluteRequestPaths() throws Exception {
        Lettermint lettermint = new Lettermint("sending-token", "https://api.example.test/v1");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> lettermint.getClient().post("https://attacker.example.test/collect", Collections.emptyMap(), Map.class)
        );

        assertEquals("Request path must be relative", exception.getMessage());
    }

    @Test
    void apiExposesDocumentedOperations() throws Exception {
        ApiClient api = Lettermint.api("api-token");
        Map<String, Object> methods = new HashMap<>();
        methods.put("domain.index", api.domains().getClass().getMethod("list"));
        methods.put("domain.store", api.domains().getClass().getMethod("create", co.lettermint.models.api.StoreDomainData.class));
        methods.put("domain.show", api.domains().getClass().getMethod("retrieve", String.class));
        methods.put("domain.destroy", api.domains().getClass().getMethod("delete", String.class));
        methods.put("domain.verifyDnsRecords", api.domains().getClass().getMethod("verifyDnsRecords", String.class));
        methods.put("domain.verifySpecificDnsRecord", api.domains().getClass().getMethod("verifyDnsRecord", String.class, String.class));
        methods.put("domain.updateProjects", api.domains().getClass().getMethod("updateProjects", String.class, co.lettermint.models.api.UpdateDomainProjectsData.class));
        methods.put("v1.ping", api.getClass().getMethod("ping"));
        methods.put("message.index", api.messages().getClass().getMethod("list"));
        methods.put("message.show", api.messages().getClass().getMethod("retrieve", String.class));
        methods.put("message.events", api.messages().getClass().getMethod("events", String.class));
        methods.put("message.source", api.messages().getClass().getMethod("source", String.class));
        methods.put("message.html", api.messages().getClass().getMethod("html", String.class));
        methods.put("message.text", api.messages().getClass().getMethod("text", String.class));
        methods.put("project.index", api.projects().getClass().getMethod("list"));
        methods.put("project.store", api.projects().getClass().getMethod("create", co.lettermint.models.api.StoreProjectData.class));
        methods.put("project.show", api.projects().getClass().getMethod("retrieve", String.class));
        methods.put("project.update", api.projects().getClass().getMethod("update", String.class, co.lettermint.models.api.UpdateProjectData.class));
        methods.put("project.destroy", api.projects().getClass().getMethod("delete", String.class));
        methods.put("project.rotateToken", api.projects().getClass().getMethod("rotateToken", String.class));
        methods.put("project.updateMembers", api.projects().getClass().getMethod("updateMembers", String.class, co.lettermint.models.api.UpdateProjectMembersData.class));
        methods.put("project.addMember", api.projects().getClass().getMethod("addMember", String.class, String.class));
        methods.put("project.removeMember", api.projects().getClass().getMethod("removeMember", String.class, String.class));
        methods.put("route.index", api.projects().getClass().getMethod("routes", String.class));
        methods.put("route.store", api.projects().getClass().getMethod("createRoute", String.class, co.lettermint.models.api.StoreRouteData.class));
        methods.put("route.show", api.routes().getClass().getMethod("retrieve", String.class));
        methods.put("route.update", api.routes().getClass().getMethod("update", String.class, co.lettermint.models.api.UpdateRouteData.class));
        methods.put("route.destroy", api.routes().getClass().getMethod("delete", String.class));
        methods.put("route.verifyInboundDomain", api.routes().getClass().getMethod("verifyInboundDomain", String.class));
        methods.put("stats.index", api.stats().getClass().getMethod("retrieve"));
        methods.put("suppression.index", api.suppressions().getClass().getMethod("list"));
        methods.put("suppression.store", api.suppressions().getClass().getMethod("create", co.lettermint.models.api.StoreSuppressionData.class));
        methods.put("suppression.destroy", api.suppressions().getClass().getMethod("delete", String.class));
        methods.put("team.show", api.team().getClass().getMethod("retrieve"));
        methods.put("team.update", api.team().getClass().getMethod("update", co.lettermint.models.api.UpdateTeamData.class));
        methods.put("team.usage", api.team().getClass().getMethod("usage"));
        methods.put("team.members", api.team().getClass().getMethod("members"));
        methods.put("webhook.index", api.webhooks().getClass().getMethod("list"));
        methods.put("webhook.store", api.webhooks().getClass().getMethod("create", co.lettermint.models.api.StoreWebhookData.class));
        methods.put("webhook.show", api.webhooks().getClass().getMethod("retrieve", String.class));
        methods.put("webhook.update", api.webhooks().getClass().getMethod("update", String.class, co.lettermint.models.api.UpdateWebhookData.class));
        methods.put("webhook.destroy", api.webhooks().getClass().getMethod("delete", String.class));
        methods.put("webhook.test", api.webhooks().getClass().getMethod("test", String.class));
        methods.put("webhook.regenerateSecret", api.webhooks().getClass().getMethod("regenerateSecret", String.class));
        methods.put("webhook.deliveries", api.webhooks().getClass().getMethod("deliveries", String.class));
        methods.put("webhook.showDelivery", api.webhooks().getClass().getMethod("delivery", String.class, String.class));

        for (Map.Entry<String, Object> entry : methods.entrySet()) {
            assertNotNull(entry.getValue(), "Missing SDK method for " + entry.getKey());
        }
    }
}
