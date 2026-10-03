package co.lettermint;

import co.lettermint.api.ApiClient;
import co.lettermint.models.api.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WebhookBasicAuthTest {
    @Test
    void createAndUpdatePreserveAllCredentialStatesAndBearerAuth() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        WebhookBasicAuthData credentials = new WebhookBasicAuthData();
        credentials.username = " fixture user ";
        credentials.password = "";
        for (int state = 0; state < 3; state++) {
            try (MockWebServer server = new MockWebServer()) {
                for (int i = 0; i < 2; i++) server.enqueue(new MockResponse().setBody("{\"data\":{\"has_basic_auth\":true}}"));
                server.start();
                ApiClient api = Lettermint.api("fixture-token", server.url("/v1").toString());
                StoreWebhookData create = new StoreWebhookData();
                create.name = "Fixture";
                create.url = "https://example.test/hook";
                UpdateWebhookData update = new UpdateWebhookData();
                if (state == 1) { create.basicAuth = OptionalNullable.of(credentials); update.basicAuth = OptionalNullable.of(credentials); }
                if (state == 2) { create.basicAuth = OptionalNullable.nullValue(); update.basicAuth = OptionalNullable.nullValue(); }
                assertTrue(api.webhooks().create(create).data.hasBasicAuth);
                assertTrue(api.webhooks().update("webhook-id", update).data.hasBasicAuth);
                for (int i = 0; i < 2; i++) {
                    RecordedRequest request = server.takeRequest();
                    assertEquals(i == 0 ? "POST" : "PUT", request.getMethod());
                    assertEquals(i == 0 ? "/v1/webhooks" : "/v1/webhooks/webhook-id", request.getPath());
                    assertEquals("Bearer fixture-token", request.getHeader("Authorization"));
                    assertNull(request.getHeader("x-lettermint-token"));
                    JsonNode body = mapper.readTree(request.getBody().readUtf8());
                    assertEquals(state != 0, body.has("basic_auth"));
                    if (state == 1) { assertEquals("", body.get("basic_auth").get("password").asText()); assertEquals(" fixture user ", body.get("basic_auth").get("username").asText()); }
                    if (state == 2) assertTrue(body.get("basic_auth").isNull());
                }
                UpdateWebhookData decoded = mapper.readValue(mapper.writeValueAsString(update), UpdateWebhookData.class);
                assertEquals(state != 0, decoded.basicAuth != null);
                if (state == 1) assertEquals("", decoded.basicAuth.getValue().password);
                if (state == 2) assertNull(decoded.basicAuth.getValue());
            }
        }
    }

    @Test
    void allReadModelsExposeOnlyTheSafeFlag() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        assertTrue(mapper.readValue("{\"has_basic_auth\":true}", WebhookData.class).hasBasicAuth);
        assertTrue(mapper.readValue("{\"has_basic_auth\":true}", WebhookListData.class).hasBasicAuth);
        assertTrue(mapper.readValue("{\"has_basic_auth\":true}", WebhookSecretData.class).hasBasicAuth);
    }
}
