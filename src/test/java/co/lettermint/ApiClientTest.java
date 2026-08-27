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
    void apiBlockedFileTypesUsesBearerToken() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .setHeader("Content-Type", "application/json")
                    .setBody("{\"extensions\":[\"exe\"],\"mime_types\":[\"application/x-msdownload\"]}"));
            server.start();

            ApiClient api = Lettermint.api("api-token", server.url("/v1").toString());
            co.lettermint.models.api.BlockedFileTypesResponse response = api.blockedFileTypes();

            RecordedRequest request = server.takeRequest();
            assertEquals("/v1/blocked-file-types", request.getPath());
            assertEquals("Bearer api-token", request.getHeader("Authorization"));
            assertNull(request.getHeader("x-lettermint-token"));
            assertEquals("exe", response.extensions.get(0));
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
                    .idempotencyKey("batch-key")
                    .sendBatch(Collections.singletonList(payload));

            RecordedRequest request = server.takeRequest();
            assertEquals("/v1/send/batch", request.getPath());
            assertEquals("sending-token", request.getHeader("x-lettermint-token"));
            assertEquals("batch-key", request.getHeader("Idempotency-Key"));
            assertEquals("msg_123", response.get(0).messageId);
        }
    }

    @Test
    void apiEndpointPathsAreEncodedAndTyped() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse().setHeader("Content-Type", "application/json").setBody("{\"id\":\"domain_123\"}"));
            server.enqueue(new MockResponse().setBody("<html></html>"));
            server.enqueue(new MockResponse().setHeader("Content-Type", "application/json").setBody("{\"data\":{\"verified\":true}}"));
            server.enqueue(new MockResponse().setHeader("Content-Type", "application/json").setBody("{\"data\":[]}"));
            server.enqueue(new MockResponse().setHeader("Content-Type", "application/json").setBody("{\"id\":\"user/id\"}"));
            server.enqueue(new MockResponse().setHeader("Content-Type", "application/json").setBody("{\"id\":\"user/id\"}"));
            server.start();

            ApiClient api = Lettermint.api("api-token", server.url("/v1").toString());

            assertEquals("domain_123", api.domains().retrieve("domain/id").id);
            assertEquals("<html></html>", api.messages().html("msg/id"));
            assertTrue(api.routes().verifyInboundDomain("route/id").data.containsKey("verified"));
            assertTrue(api.team().roles().data.isEmpty());
            assertEquals("user/id", api.team().member("user/id").id);

            co.lettermint.models.api.UpdateTeamMemberAssignmentData assignment =
                    new co.lettermint.models.api.UpdateTeamMemberAssignmentData();
            assignment.roleId = "role_123";
            assignment.projectAccess = Collections.<String, Object>singletonMap("scope", "all");
            assertEquals("user/id", api.team().updateMemberAssignment("user/id", assignment).id);

            assertEquals("/v1/domains/domain%2Fid", server.takeRequest().getPath());
            assertEquals("/v1/messages/msg%2Fid/html", server.takeRequest().getPath());
            assertEquals("/v1/routes/route%2Fid/verify-inbound-domain", server.takeRequest().getPath());
            assertEquals("/v1/team/roles", server.takeRequest().getPath());
            assertEquals("/v1/team/members/user%2Fid", server.takeRequest().getPath());
            RecordedRequest assignmentRequest = server.takeRequest();
            assertEquals("PUT", assignmentRequest.getMethod());
            assertEquals("/v1/team/members/user%2Fid/assignment", assignmentRequest.getPath());
            assertTrue(assignmentRequest.getBody().readUtf8().contains("\"role_id\":\"role_123\""));
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
    void schedulesMessageChangesWithTypedResponses() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse().setHeader("Content-Type", "application/json").setBody("{\"message_id\":\"msg/id\",\"status\":\"scheduled\",\"scheduled_at\":\"2026-08-27T09:00:00Z\"}"));
            server.enqueue(new MockResponse().setHeader("Content-Type", "application/json").setBody("{\"message_id\":\"msg/id\",\"status\":\"canceled\",\"scheduled_at\":null}"));
            server.start();

            ApiClient api = Lettermint.api("api-token", server.url("/v1").toString());
            co.lettermint.models.api.RescheduleMessageRequest payload = new co.lettermint.models.api.RescheduleMessageRequest();
            payload.scheduledAt = "2026-08-27T09:00:00Z";
            assertEquals("scheduled", api.messages().reschedule("msg/id", payload).status);
            assertEquals("canceled", api.messages().cancel("msg/id").status);

            RecordedRequest reschedule = server.takeRequest();
            assertEquals("PATCH", reschedule.getMethod());
            assertEquals("/v1/messages/msg%2Fid", reschedule.getPath());
            assertTrue(reschedule.getBody().readUtf8().contains("scheduled_at"));
            RecordedRequest cancel = server.takeRequest();
            assertEquals("POST", cancel.getMethod());
            assertEquals("/v1/messages/msg%2Fid/cancel", cancel.getPath());
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
    void generatedApiModelsMatchCurrentTeamSchema() {
        assertEquals("scheduled", co.lettermint.models.api.MessageEventType.SCHEDULED);
        assertEquals("message.auto_replied", co.lettermint.models.api.WebhookEvent.MESSAGEAUTOREPLIED);
        assertEquals("admin", co.lettermint.models.api.BuiltInTeamRole.ADMIN);
        assertEquals("enforced", co.lettermint.models.api.TlsPolicy.ENFORCED);

        co.lettermint.models.api.UpdateRouteSettingsData settings = new co.lettermint.models.api.UpdateRouteSettingsData();
        settings.redactEmailContent = true;
        settings.generatePlaintextFallback = false;
        settings.tls = co.lettermint.models.api.TlsPolicy.ENFORCED;

        co.lettermint.models.api.UpdateRouteInboundSettingsData inboundSettings = new co.lettermint.models.api.UpdateRouteInboundSettingsData();
        inboundSettings.inboundSpamThreshold = 3.0;

        co.lettermint.models.api.UpdateRouteData routeUpdate = new co.lettermint.models.api.UpdateRouteData();
        routeUpdate.settings = settings;
        routeUpdate.inboundSettings = inboundSettings;

        co.lettermint.models.api.UpdateProjectData projectUpdate = new co.lettermint.models.api.UpdateProjectData();
        projectUpdate.redactEmailContent = false;

        co.lettermint.models.api.ProjectData project = new co.lettermint.models.api.ProjectData();
        project.redactEmailContent = true;

        co.lettermint.models.api.StoreProjectData projectCreate = new co.lettermint.models.api.StoreProjectData();
        projectCreate.shortToken = true;

        co.lettermint.models.api.BlockedFileTypesResponse blockedFileTypes = new co.lettermint.models.api.BlockedFileTypesResponse();
        blockedFileTypes.extensions = Collections.singletonList("exe");
        blockedFileTypes.mimeTypes = Collections.singletonList("application/x-msdownload");

        co.lettermint.models.api.TeamData team = new co.lettermint.models.api.TeamData();
        team.includedVolume = 300000;

        co.lettermint.models.api.TeamRoleData role = new co.lettermint.models.api.TeamRoleData();
        role.assignable = true;
        role.permissions = Collections.singletonList(co.lettermint.models.api.RbacPermission.MEMBERSMANAGE);

        co.lettermint.models.api.UpdateTeamMemberAssignmentData assignment =
                new co.lettermint.models.api.UpdateTeamMemberAssignmentData();
        assignment.roleId = "role_123";
        assignment.projectAccess = Collections.<String, Object>singletonMap("scope", "selected");

        co.lettermint.models.api.DomainData domain = new co.lettermint.models.api.DomainData();
        domain.dkimMode = co.lettermint.models.api.DkimMode.MANAGEDCNAME;

        co.lettermint.models.api.SuppressedRecipientData recipient = new co.lettermint.models.api.SuppressedRecipientData();
        recipient.sourceMessage = new co.lettermint.models.api.SuppressionSourceMessageData();
        recipient.sourceMessage.id = "msg_123";

        co.lettermint.models.api.MessageListData message = new co.lettermint.models.api.MessageListData();
        message.spamScore = 2.5;
        message.scheduledAt = "2026-08-27T09:00:00Z";

        assertFalse(routeUpdate.settings.generatePlaintextFallback);
        assertEquals("enforced", routeUpdate.settings.tls);
        assertEquals(3.0, routeUpdate.inboundSettings.inboundSpamThreshold);
        assertFalse(projectUpdate.redactEmailContent);
        assertTrue(projectCreate.shortToken);
        assertTrue(project.redactEmailContent);
        assertEquals("global", co.lettermint.models.api.SuppressionScope.GLOBAL);
        assertEquals("application/x-msdownload", blockedFileTypes.mimeTypes.get(0));
        assertEquals(300000, team.includedVolume);
        assertTrue(role.assignable);
        assertEquals("members:manage", role.permissions.get(0));
        assertEquals("role_123", assignment.roleId);
        assertEquals("managed_cname", domain.dkimMode);
        assertEquals("msg_123", recipient.sourceMessage.id);
        assertEquals(2.5, message.spamScore);
        assertEquals("2026-08-27T09:00:00Z", message.scheduledAt);
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
        methods.put("v1.blockedFileTypes", api.getClass().getMethod("blockedFileTypes"));
        methods.put("message.index", api.messages().getClass().getMethod("list"));
        methods.put("message.show", api.messages().getClass().getMethod("retrieve", String.class));
        methods.put("rescheduleMessage", api.messages().getClass().getMethod("reschedule", String.class, co.lettermint.models.api.RescheduleMessageRequest.class));
        methods.put("cancelScheduledMessage", api.messages().getClass().getMethod("cancel", String.class));
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
        methods.put("team.roles", api.team().getClass().getMethod("roles"));
        methods.put("team.members", api.team().getClass().getMethod("members"));
        methods.put("team.members.show", api.team().getClass().getMethod("member", String.class));
        methods.put("team.members.assignment.update", api.team().getClass().getMethod("updateMemberAssignment", String.class, co.lettermint.models.api.UpdateTeamMemberAssignmentData.class));
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
