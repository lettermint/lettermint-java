package co.lettermint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.lettermint.exceptions.ApiException;
import co.lettermint.exceptions.AuthenticationException;
import co.lettermint.exceptions.ConflictException;
import co.lettermint.exceptions.ConnectionException;
import co.lettermint.exceptions.LettermintConfigException;
import co.lettermint.exceptions.LettermintException;
import co.lettermint.exceptions.LettermintValidationException;
import co.lettermint.exceptions.NotFoundException;
import co.lettermint.exceptions.PermissionException;
import co.lettermint.exceptions.RateLimitException;
import co.lettermint.exceptions.RedirectException;
import co.lettermint.exceptions.ServerException;
import co.lettermint.exceptions.TimeoutException;
import co.lettermint.exceptions.UnexpectedResponseException;
import co.lettermint.exceptions.ValidationException;
import co.lettermint.types.DomainStatus;
import co.lettermint.types.GetDomainQueryIncludeItem;
import co.lettermint.types.GetDomainQuery;
import co.lettermint.types.ListDomainsQuery;
import co.lettermint.types.ListDomainsQuerySortItem;
import co.lettermint.types.ListMessagesQuery;
import co.lettermint.types.ListMessagesQueryFilter;
import co.lettermint.types.ListMessagesQueryFilterTagsItem;
import co.lettermint.types.ListWebhooksQuery;
import co.lettermint.types.MessageStatus;
import co.lettermint.types.SendMailResponse;
import java.net.ServerSocket;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransportTest {
    private FakeServer server;
    private Lettermint client;

    @BeforeEach
    void start() throws Exception {
        server = new FakeServer();
        client = Lettermint.builder().sendingToken(ClientTest.SENDING).teamToken(ClientTest.TEAM).baseUrl(server.baseUrl()).build();
    }

    @AfterEach
    void stop() {
        server.close();
    }

    private SendMailResponse send() {
        return client.emails().send(EmailMessage.create().from("a@example.com").to("b@example.com").subject("Hi").text("Hello"));
    }

    @Test
    void decodesUnknownEnumsAndFields() {
        server.enqueue(FakeServer.Response.json(202, "{\"message_id\":\"m1\",\"status\":\"some_future_status\",\"some_future_field\":{\"nested\":[1]}}"));
        SendMailResponse response = send();
        assertEquals("m1", response.messageId());
        assertEquals("some_future_status", response.status().value());
        server.enqueue(FakeServer.Response.json(202, "{\"message_id\":\"m2\",\"status\":\"pending\"}"));
        assertEquals(MessageStatus.PENDING, send().status());
    }

    @Test
    void emptyAndInvalidBodiesAreUnexpectedResponses() {
        server.enqueue(FakeServer.Response.text(202, "application/json", ""));
        UnexpectedResponseException empty = assertThrows(UnexpectedResponseException.class, this::send);
        assertEquals(202, empty.getStatus());
        server.enqueue(FakeServer.Response.text(200, "application/json", "not json"));
        assertEquals(200, assertThrows(UnexpectedResponseException.class, this::send).getStatus());
        server.enqueue(FakeServer.Response.json(200, "[1,2]"));
        assertEquals(200, assertThrows(UnexpectedResponseException.class, this::send).getStatus());
        String html = "<html><head><title>502 Bad Gateway</title></head><body>" + "x".repeat(300) + "</body></html>";
        server.enqueue(FakeServer.Response.text(502, "text/html; charset=UTF-8", html));
        UnexpectedResponseException page = assertThrows(UnexpectedResponseException.class, this::send);
        assertEquals(502, page.getStatus());
        assertTrue(page.getMessage().contains("(text/html)"), page.getMessage());
        assertEquals(201, page.getBodyExcerpt().length());
        server.enqueue(FakeServer.Response.empty(304));
        assertThrows(RedirectException.class, this::send);
    }

    @Test
    void mapsErrorStatuses() {
        Map<Integer, Class<? extends ApiException>> expected = Map.of(400, ApiException.class, 401, AuthenticationException.class, 403, PermissionException.class,
                404, NotFoundException.class, 409, ConflictException.class, 422, ValidationException.class, 429, RateLimitException.class,
                500, ServerException.class, 503, ServerException.class);
        for (Map.Entry<Integer, Class<? extends ApiException>> entry : expected.entrySet()) {
            server.enqueue(FakeServer.Response.json(entry.getKey(), "{\"error\":{\"code\":\"E_CODE\",\"message\":\"Nope\",\"details\":{\"a\":1}}}"));
            ApiException error = assertThrows(ApiException.class, this::send);
            assertEquals(entry.getValue(), error.getClass());
            assertEquals(entry.getKey(), error.getStatus());
            assertEquals("E_CODE", error.getCode());
            assertEquals("Nope", error.getMessage());
            assertEquals(Map.of("a", 1), error.getDetails());
        }
        server.enqueue(FakeServer.Response.json(422, "{\"message\":\"The to field is required.\",\"errors\":{\"to\":[\"The to field is required.\"]}}"));
        ValidationException validation = assertThrows(ValidationException.class, this::send);
        assertEquals("The to field is required.", validation.getMessage());
        assertEquals(Map.of("to", List.of("The to field is required.")), validation.getErrors());
        assertNull(validation.getCode());

        server.enqueue(FakeServer.Response.json(403, "{\"error\":\"plan_required\",\"message\":\"Upgrade\"}"));
        PermissionException permission = assertThrows(PermissionException.class, this::send);
        assertEquals("plan_required", permission.getCode());
        assertEquals("Upgrade", permission.getMessage());

        server.enqueue(FakeServer.Response.empty(500));
        ServerException empty = assertThrows(ServerException.class, this::send);
        assertEquals("HTTP 500", empty.getMessage());
        assertNull(empty.getBody());

        server.enqueue(FakeServer.Response.json(429, "{\"message\":\"Slow down\"}").header("Retry-After", "7"));
        assertEquals(Duration.ofSeconds(7), assertThrows(RateLimitException.class, this::send).getRetryAfter());
        String date = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now().plusSeconds(30));
        server.enqueue(FakeServer.Response.json(429, "{}").header("Retry-After", date));
        long seconds = assertThrows(RateLimitException.class, this::send).getRetryAfter().getSeconds();
        assertTrue(seconds > 20 && seconds <= 31, String.valueOf(seconds));
        server.enqueue(FakeServer.Response.json(429, "{}"));
        assertNull(assertThrows(RateLimitException.class, this::send).getRetryAfter());
    }

    @Test
    void neverFollowsRedirects() throws Exception {
        try (FakeServer foreign = new FakeServer()) {
            server.enqueue(FakeServer.Response.json(307, "{\"message\":\"Moved\"}").header("Location", foreign.baseUrl() + "/send"));
            RedirectException error = assertThrows(RedirectException.class, this::send);
            assertEquals(307, error.getStatus());
            server.enqueue(FakeServer.Response.empty(302).header("Location", foreign.baseUrl() + "/ping"));
            assertThrows(RedirectException.class, () -> client.ping());
            assertTrue(foreign.requests().isEmpty());
        }
    }

    @Test
    void doesNotRetry() {
        server.enqueue(FakeServer.Response.json(500, "{}"));
        assertThrows(ServerException.class, this::send);
        assertEquals(1, server.requests().size());
    }

    @Test
    void theTimeoutCoversTheBody() {
        Lettermint fast = Lettermint.builder().sendingToken(ClientTest.SENDING).baseUrl(server.baseUrl()).timeout(Duration.ofMillis(300)).build();
        server.enqueue(FakeServer.Response.json(202, "{\"message_id\":\"m\",\"status\":\"pending\"}").stallBody(3000));
        long started = System.nanoTime();
        TimeoutException error = assertThrows(TimeoutException.class,
                () -> fast.emails().send(EmailMessage.create().from("a@b.c").to("d@e.f").subject("s")));
        assertTrue(System.nanoTime() - started < 2_500_000_000L);
        assertEquals(Duration.ofMillis(300), error.getTimeout());

        server.enqueue(FakeServer.Response.json(202, "{}").delay(3000));
        assertThrows(TimeoutException.class, () -> client.emails().send(EmailMessage.create().from("a@b.c"),
                SendOptions.timeout(Duration.ofMillis(200))));
        server.enqueue(FakeServer.Response.json(200, "{}").delay(3000));
        assertThrows(TimeoutException.class, () -> client.domains().list(null, RequestOptions.timeout(Duration.ofMillis(200))));
        assertThrows(LettermintConfigException.class, () -> client.domains().list(null, RequestOptions.timeout(Duration.ZERO)));
    }

    @Test
    void connectionFailuresAreTyped() throws Exception {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        Lettermint unreachable = Lettermint.builder().teamToken(ClientTest.TEAM).baseUrl("http://127.0.0.1:" + port + "/v1").build();
        LettermintException error = assertThrows(LettermintException.class, unreachable::ping);
        assertInstanceOf(ConnectionException.class, error);
        assertTrue(error.getMessage().startsWith("Could not reach the Lettermint API"));
    }

    @Test
    void interruptingTheThreadCancelsTheRequest() throws Exception {
        server.enqueue(FakeServer.Response.json(200, "{}").delay(5000));
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        AtomicReference<Boolean> interrupted = new AtomicReference<>();
        Thread thread = new Thread(() -> {
            try {
                client.domains().list();
            } catch (Throwable error) {
                thrown.set(error);
                interrupted.set(Thread.currentThread().isInterrupted());
            }
        });
        thread.start();
        while (server.requests().isEmpty()) {
            Thread.sleep(10);
        }
        thread.interrupt();
        thread.join(3000);
        assertInstanceOf(CancellationException.class, thrown.get());
        assertTrue(interrupted.get());
    }

    @Test
    void encodesAndChecksPathParameters() {
        client.domains().retrieve("a b/c?d#é");
        assertEquals("/v1/domains/a%20b%2Fc%3Fd%23%C3%A9", server.last().path());
        client.webhooks().deliveries().retrieve("wh!*'()", "d~._-");
        assertEquals("/v1/webhooks/wh!*'()/deliveries/d~._-", server.last().path());
        int before = server.requests().size();
        for (String invalid : new String[] {"", ".", "..", null}) {
            LettermintConfigException error = assertThrows(LettermintConfigException.class, () -> client.domains().delete(invalid));
            assertEquals("domains.delete: domainId must be a non-empty string other than \".\" and \"..\".", error.getMessage());
        }
        assertThrows(LettermintConfigException.class, () -> client.webhooks().deliveries().retrieve("wh", ".."));
        assertEquals(before, server.requests().size());
    }

    @Test
    void serializesQueriesLikeTheNodeSdk() {
        client.domains().list(ListDomainsQuery.builder().pageSize(30).filterStatus(DomainStatus.VERIFIED)
                .sort(List.of(ListDomainsQuerySortItem.CREATED_AT_DESC, ListDomainsQuerySortItem.DOMAIN)).build());
        assertEquals("page%5Bsize%5D=30&sort=-created_at%2Cdomain&filter%5Bstatus%5D=verified", server.last().rawQuery());

        client.messages().list(ListMessagesQuery.builder()
                .filter(ListMessagesQueryFilter.builder().tags(List.of(
                        ListMessagesQueryFilterTagsItem.builder().name("campaign").value("welcome").build(),
                        ListMessagesQueryFilterTagsItem.builder().name("plan").value("pro").build())).build())
                .filterSearch("hello world & more").filterStatus(MessageStatus.of("a_future_status")).build());
        assertEquals("filter%5Bsearch%5D=hello+world+%26+more"
                + "&filter%5Btags%5D%5B0%5D%5Bname%5D=campaign&filter%5Btags%5D%5B0%5D%5Bvalue%5D=welcome"
                + "&filter%5Btags%5D%5B1%5D%5Bname%5D=plan&filter%5Btags%5D%5B1%5D%5Bvalue%5D=pro"
                + "&filter%5Bstatus%5D=a_future_status", server.last().rawQuery());

        client.webhooks().list(ListWebhooksQuery.builder().filterEnabled(true).build());
        assertEquals("filter%5Benabled%5D=1", server.last().rawQuery());
        client.webhooks().list(ListWebhooksQuery.builder().filterEnabled(false).sort(List.of()).build());
        assertEquals("filter%5Benabled%5D=0", server.last().rawQuery());

        client.domains().retrieve("d1", GetDomainQuery.builder().include(List.of(GetDomainQueryIncludeItem.DNS_RECORDS)).build());
        assertEquals("include=dnsRecords", server.last().rawQuery());
        client.domains().list(ListDomainsQuery.builder().build());
        assertNull(server.last().rawQuery());
    }

    @Test
    void idempotencyKeysArePerCall() {
        server.fallback(request -> FakeServer.Response.json(202, "{\"message_id\":\"m\",\"status\":\"pending\"}"));
        EmailMessage message = EmailMessage.create().from("a@b.c").to("d@e.f").subject("s");
        client.emails().send(message, SendOptions.idempotencyKey("key-1"));
        assertEquals("key-1", server.last().header("idempotency-key"));
        client.emails().send(message);
        assertNull(server.last().header("idempotency-key"));
        client.messages().process("msg_1", SendOptions.idempotencyKey("process-1"));
        assertEquals("process-1", server.last().header("idempotency-key"));
        int before = server.requests().size();
        for (String invalid : new String[] {"", "a\nb", "a\rb", "a\u0000b"}) {
            LettermintValidationException error = assertThrows(LettermintValidationException.class,
                    () -> client.emails().send(message, SendOptions.idempotencyKey(invalid)));
            assertEquals("idempotencyKey", error.getField());
        }
        assertEquals(before, server.requests().size());
    }

    @Test
    void handlesEmptyAndTextResponses() {
        server.enqueue(FakeServer.Response.empty(204));
        client.projects().reportForwarding().delete("p1");
        assertEquals("DELETE", server.last().method());
        server.enqueue(FakeServer.Response.text(200, "text/html; charset=UTF-8", "<p>Hi</p>\n"));
        assertEquals("<p>Hi</p>\n", client.messages().html("m1"));
        server.enqueue(FakeServer.Response.text(200, "message/rfc822", "Subject: x\r\n\r\nbody"));
        assertEquals("Subject: x\r\n\r\nbody", client.messages().source("m1"));
        server.enqueue(FakeServer.Response.text(200, "text/plain", "plain"));
        assertEquals("plain", client.messages().text("m1"));
        server.enqueue(FakeServer.Response.text(200, "text/html", " pong \n"));
        assertEquals("pong", client.ping());
    }

    @Test
    void requestBodiesAreJson() {
        server.fallback(request -> FakeServer.Response.json(202, "{\"message_id\":\"m\",\"status\":\"pending\"}"));
        send();
        assertEquals("POST", server.last().method());
        assertEquals("/v1/send", server.last().path());
        assertTrue(server.last().header("content-type").startsWith("application/json"));
        assertEquals("{\"from\":\"a@example.com\",\"to\":[\"b@example.com\"],\"subject\":\"Hi\",\"text\":\"Hello\"}", server.last().body());
        client.domains().verifyDnsRecords("d1");
        assertNull(server.last().header("content-type"));
        assertEquals("", server.last().body());
    }
}
