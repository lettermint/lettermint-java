package co.lettermint;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.lettermint.exceptions.LettermintConfigException;
import co.lettermint.exceptions.ServerException;
import co.lettermint.types.RescheduleMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClientTest {
    static final String SENDING = "lm_SendingTestToken0123456789abcdefAB";
    static final String TEAM = "lm_team_TeamTestToken0123456789abcdefABCDEFGH";

    private FakeServer server;

    @BeforeEach
    void start() throws Exception {
        server = new FakeServer();
        server.fallback(request -> request.path().equals("/v1/ping")
                ? FakeServer.Response.text(200, "text/html; charset=UTF-8", "pong\n")
                : FakeServer.Response.json(200, "{}"));
    }

    @AfterEach
    void stop() {
        server.close();
    }

    private Lettermint.Builder builder() {
        return Lettermint.builder().baseUrl(server.baseUrl());
    }

    @Test
    void requiresAToken() {
        LettermintConfigException error = assertThrows(LettermintConfigException.class, () -> Lettermint.builder().build());
        assertEquals("Pass sendingToken, teamToken or both.", error.getMessage());
        assertThrows(LettermintConfigException.class, () -> Lettermint.builder().sendingToken(""));
        assertThrows(LettermintConfigException.class, () -> Lettermint.builder().teamToken("lm_team_with space"));
        assertThrows(LettermintConfigException.class, () -> Lettermint.builder().sendingToken("lm_abc\r\nx"));
    }

    @Test
    void detectsTheTokenFormat() {
        Lettermint team = builder().token(TEAM).build();
        assertEquals("pong", team.ping());
        assertEquals("Bearer " + TEAM, server.last().header("authorization"));
        assertNull(server.last().header("x-lettermint-token"));

        for (String sending : List.of("lm_Proj32Conformance0Token1Fake2Val", "lm_Proj22Conformance0Toke")) {
            Lettermint client = builder().token(sending).build();
            assertEquals("pong", client.ping());
            assertEquals(sending, server.last().header("x-lettermint-token"));
            assertNull(server.last().header("authorization"));
        }
        int before = server.requests().size();
        for (String invalid : new String[] {"lm_sso_SsoToken123", "", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4In0.c2ln", "sk_live_123", "lm_team_", "lm_", null}) {
            LettermintConfigException error = assertThrows(LettermintConfigException.class, () -> Lettermint.of(invalid));
            assertTrue(error.getMessage().startsWith("Unrecognised token format"), error.getMessage());
            if (invalid != null && invalid.length() > 3) {
                assertFalse(error.getMessage().contains(invalid));
            }
        }
        assertEquals(before, server.requests().size());
    }

    @Test
    void eachPartUsesItsOwnTokenAndNeverFallsBack() {
        Lettermint sending = builder().sendingToken(SENDING).build();
        LettermintConfigException error = assertThrows(LettermintConfigException.class, () -> sending.domains().list());
        assertEquals("domains.list needs teamToken; set it with Lettermint.builder().teamToken(...).", error.getMessage());
        assertThrows(LettermintConfigException.class, () -> sending.webhooks().deliveries().iterate("wh_1"));
        assertThrows(LettermintConfigException.class, () -> sending.analytics(null));

        Lettermint team = builder().teamToken(TEAM).build();
        error = assertThrows(LettermintConfigException.class, () -> team.emails().send(EmailMessage.create()));
        assertEquals("emails.send needs sendingToken; set it with Lettermint.builder().sendingToken(...).", error.getMessage());
        assertThrows(LettermintConfigException.class, () -> team.emails().compose());
        assertThrows(LettermintConfigException.class, () -> team.emails().ping());
        assertTrue(server.requests().isEmpty());
    }

    @Test
    void pingRescheduleAndCancelPreferTheTeamToken() {
        Lettermint both = builder().sendingToken(SENDING).teamToken(TEAM).build();
        both.ping();
        assertEquals("Bearer " + TEAM, server.last().header("authorization"));
        both.emails().ping();
        assertEquals(SENDING, server.last().header("x-lettermint-token"));
        assertNull(server.last().header("authorization"));
        both.messages().cancel("msg_1");
        assertEquals("Bearer " + TEAM, server.last().header("authorization"));

        Lettermint sending = builder().sendingToken(SENDING).build();
        sending.ping();
        assertEquals(SENDING, server.last().header("x-lettermint-token"));
        sending.messages().reschedule("msg_1", RescheduleMessageRequest.builder().scheduledAt("2026-10-20T09:00:00Z").build());
        assertEquals("PATCH", server.last().method());
        assertEquals(SENDING, server.last().header("x-lettermint-token"));
        sending.messages().cancel("msg_1");
        assertEquals(SENDING, server.last().header("x-lettermint-token"));
        assertNull(server.last().header("authorization"));
    }

    @Test
    void validatesOptions() {
        assertThrows(LettermintConfigException.class, () -> Lettermint.builder().baseUrl("ftp://example.com"));
        assertThrows(LettermintConfigException.class, () -> Lettermint.builder().baseUrl("/v1"));
        assertThrows(LettermintConfigException.class, () -> Lettermint.builder().baseUrl("https://user:pass@example.com/v1"));
        assertThrows(LettermintConfigException.class, () -> Lettermint.builder().baseUrl("https://example.com/v1?x=1"));
        assertThrows(LettermintConfigException.class, () -> Lettermint.builder().timeout(Duration.ZERO));
        assertThrows(LettermintConfigException.class, () -> Lettermint.builder().timeout(Duration.ofSeconds(-1)));
        HttpClient following = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
        LettermintConfigException error = assertThrows(LettermintConfigException.class, () -> Lettermint.builder().httpClient(following));
        assertTrue(error.getMessage().contains("must not follow redirects"));
        assertDoesNotThrow(() -> Lettermint.builder().httpClient(HttpClient.newHttpClient()));

        Lettermint client = Lettermint.builder().sendingToken(SENDING).baseUrl("https://example.com/v1///").build();
        assertTrue(client.toString().contains("baseUrl=https://example.com/v1,"), client.toString());
    }

    @Test
    void usesAnInjectedHttpClient() {
        Lettermint client = builder().teamToken(TEAM).httpClient(HttpClient.newBuilder().build()).build();
        assertEquals("pong", client.ping());
    }

    @Test
    void sendsTheUserAgent() {
        builder().teamToken(TEAM).build().ping();
        assertTrue(server.last().header("user-agent").startsWith("lettermint-java/"), server.last().header("user-agent"));
        assertEquals("application/json", server.last().header("accept"));
    }

    @Test
    void neverPrintsTokens() throws Exception {
        Lettermint client = builder().sendingToken(SENDING).teamToken(TEAM).timeout(Duration.ofSeconds(5)).build();
        Lettermint.Builder builder = Lettermint.builder().sendingToken(SENDING).teamToken(TEAM);
        server.enqueue(FakeServer.Response.json(500, "{\"message\":\"Server Error\"}"));
        ServerException failure = assertThrows(ServerException.class, () -> client.emails().compose().from("a@b.c").to("d@e.f").subject("s").send());

        List<Object> subjects = new ArrayList<>(List.of(client, builder, client.emails(), client.domains(), client.messages(), client.projects(),
                client.projects().reportForwarding(), client.routes(), client.stats(), client.suppressions(), client.team(), client.team().members(),
                client.webhooks(), client.webhooks().deliveries(), client.emails().compose().from("a@b.c"), client.domains().iterate(), failure));
        ObjectMapper lenient = new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        for (Object subject : subjects) {
            List<String> renders = new ArrayList<>(List.of(subject.toString(), String.valueOf(subject), String.format("%s", subject)));
            try {
                renders.add(lenient.writeValueAsString(subject));
            } catch (Exception ignored) {
                // A failed serialization does not leak anything.
            }
            for (String render : renders) {
                assertFalse(render.contains(SENDING), subject.getClass() + ": " + render);
                assertFalse(render.contains(TEAM), subject.getClass() + ": " + render);
            }
        }
        assertEquals("Lettermint{baseUrl=" + server.baseUrl() + ", timeout=PT5S, sendingToken=[redacted], teamToken=[redacted]}", client.toString());
        assertEquals("Domains{}", client.domains().toString());
        assertFalse(failure.getMessage().contains(SENDING));
    }
}
