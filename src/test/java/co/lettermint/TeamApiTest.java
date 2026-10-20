package co.lettermint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.lettermint.exceptions.ApiException;
import co.lettermint.exceptions.AuthenticationException;
import co.lettermint.exceptions.ConflictException;
import co.lettermint.exceptions.LettermintConfigException;
import co.lettermint.exceptions.NotFoundException;
import co.lettermint.exceptions.RateLimitException;
import co.lettermint.exceptions.UnexpectedResponseException;
import co.lettermint.exceptions.ValidationException;
import co.lettermint.types.AnalyticsMetric;
import co.lettermint.types.AnalyticsQuery;
import co.lettermint.types.CreateTeamInviteData;
import co.lettermint.types.CreateTeamInviteDataProjectAccess;
import co.lettermint.types.CursorPage;
import co.lettermint.types.DomainListData;
import co.lettermint.types.GetStatsQuery;
import co.lettermint.types.ListDomainsQuery;
import co.lettermint.types.ListTeamInvitesQuery;
import co.lettermint.types.ListWebhookDeliveriesQuery;
import co.lettermint.types.MessageData;
import co.lettermint.types.MessageResponse;
import co.lettermint.types.Operations;
import co.lettermint.types.PendingSendMailResponse;
import co.lettermint.types.ProjectAccessScope;
import co.lettermint.types.ReportForwardingRequest;
import co.lettermint.types.RescheduleMessageRequest;
import co.lettermint.types.ResendMessageRequest;
import co.lettermint.types.RetryWebhookDeliveryResponse;
import co.lettermint.types.RouteType;
import co.lettermint.types.StoreDomainData;
import co.lettermint.types.StoreProjectData;
import co.lettermint.types.StoreRouteData;
import co.lettermint.types.StoreSuppressionData;
import co.lettermint.types.StoreWebhookData;
import co.lettermint.types.SuppressionCreateReason;
import co.lettermint.types.SuppressionCreateScope;
import co.lettermint.types.TeamInviteData;
import co.lettermint.types.UpdateDomainProjectsData;
import co.lettermint.types.UpdateProjectData;
import co.lettermint.types.UpdateRouteData;
import co.lettermint.types.UpdateTeamData;
import co.lettermint.types.UpdateTeamInviteData;
import co.lettermint.types.UpdateTeamInviteDataProjectAccess;
import co.lettermint.types.UpdateTeamMemberAssignmentData;
import co.lettermint.types.UpdateTeamMemberAssignmentDataProjectAccess;
import co.lettermint.types.UpdateWebhookData;
import co.lettermint.types.VerifyReportForwardingRequest;
import co.lettermint.types.WebhookDeliveryData;
import co.lettermint.types.WebhookDeliveryListData;
import co.lettermint.types.WebhookEvent;
import co.lettermint.types.WebhookListData;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TeamApiTest {
    /**
     * Operations that have no method yet: they are added by the next PRs in this stack. Each of those
     * PRs removes its operations from this list, and the last one removes the list.
     */
    private static final Set<String> PENDING = Set.of(
            "GET /team/roles/{roleId}",
            "POST /team/roles",
            "PUT /team/roles/{roleId}",
            "DELETE /team/roles/{roleId}",
            "POST /team/roles/{roleId}/duplicate",
            "DELETE /team/members/{userId}");

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

    private static String page(String cursor, String... ids) {
        String data = List.of(ids).stream().map(id -> "{\"id\":\"" + id + "\"}").collect(Collectors.joining(","));
        return "{\"data\":[" + data + "],\"path\":null,\"per_page\":2,\"next_cursor\":" + (cursor == null ? "null" : "\"" + cursor + "\"")
                + ",\"next_page_url\":null,\"prev_cursor\":null,\"prev_page_url\":null}";
    }

    /** Maps a recorded request back to its operation key by matching the path templates. */
    private static String operationKey(FakeServer.Request request) {
        String best = request.method() + " " + request.path();
        int fewest = Integer.MAX_VALUE;
        for (Operations.Operation<?> operation : Operations.ALL.values()) {
            String pattern = "/v1" + operation.path().replaceAll("\\{\\w+}", "[^/]+");
            // Prefer literal segments over placeholders, e.g. /send/batch is not /send/{x}.
            if (operation.method().equals(request.method()) && request.path().matches(pattern) && operation.pathParams().size() < fewest) {
                best = operation.key();
                fewest = operation.pathParams().size();
            }
        }
        return best;
    }

    @SuppressWarnings("deprecation")
    private void rotateToken(String projectId) {
        client.projects().rotateToken(projectId);
    }

    @Test
    void everyOperationIsReachable() {
        server.fallback(request -> {
            String key = operationKey(request);
            if (key.equals("GET /ping") || key.endsWith("/html") || key.endsWith("/text") || key.endsWith("/source")) {
                return FakeServer.Response.text(200, "text/plain", "pong");
            }
            if (key.equals("DELETE /projects/{projectId}/report-forwarding")) {
                return FakeServer.Response.empty(204);
            }
            Operations.Operation<?> operation = Operations.ALL.get(key);
            if (operation != null && operation.pagination() != null) {
                return FakeServer.Response.json(200, page(null, "x"));
            }
            if (key.equals("POST /send/batch")) {
                return FakeServer.Response.json(202, "[]");
            }
            return FakeServer.Response.json(200, "{}");
        });
        EmailMessage email = EmailMessage.create().from("a@b.c").to("d@e.f").subject("s");
        List<Runnable> calls = List.of(
                () -> client.ping(),
                () -> client.analytics(AnalyticsQuery.builder().metrics(List.of(AnalyticsMetric.of("sent"))).build()),
                () -> client.analyticsPages(AnalyticsQuery.builder().metrics(List.of(AnalyticsMetric.of("sent"))).build()).forEach(item -> { }),
                () -> client.blockedFileTypes(),
                () -> client.emails().send(email),
                () -> client.emails().sendBatch(List.of(email)),
                () -> client.domains().list(),
                () -> client.domains().iterate().forEach(item -> { }),
                () -> client.domains().create(new StoreDomainData("acme.com")),
                () -> client.domains().retrieve("d1"),
                () -> client.domains().delete("d1"),
                () -> client.domains().verifyDnsRecords("d1"),
                () -> client.domains().verifyDnsRecord("d1", "r1"),
                () -> client.domains().updateProjects("d1", new UpdateDomainProjectsData(List.of("p1"))),
                () -> client.messages().list(),
                () -> client.messages().iterate().forEach(item -> { }),
                () -> client.messages().retrieve("m1"),
                () -> client.messages().events("m1"),
                () -> client.messages().iterateEvents("m1").forEach(item -> { }),
                () -> client.messages().source("m1"),
                () -> client.messages().html("m1"),
                () -> client.messages().text("m1"),
                () -> client.messages().reschedule("m1", new RescheduleMessageRequest("2026-10-20T09:00:00Z")),
                () -> client.messages().cancel("m1"),
                () -> client.messages().process("m1"),
                () -> client.messages().resend("m1"),
                () -> client.projects().list(),
                () -> client.projects().iterate().forEach(item -> { }),
                () -> client.projects().create(StoreProjectData.builder().name("Production").build()),
                () -> client.projects().retrieve("p1"),
                () -> client.projects().update("p1", UpdateProjectData.builder().name("Renamed").build()),
                () -> client.projects().delete("p1"),
                () -> rotateToken("p1"),
                () -> client.projects().reportForwarding().retrieve("p1"),
                () -> client.projects().reportForwarding().update("p1", new ReportForwardingRequest("dmarc@acme.com")),
                () -> client.projects().reportForwarding().delete("p1"),
                () -> client.projects().reportForwarding().verify("p1", new VerifyReportForwardingRequest("123456")),
                () -> client.projects().reportForwarding().resendCode("p1"),
                () -> client.routes().list("p1"),
                () -> client.routes().iterate("p1").forEach(item -> { }),
                () -> client.routes().create("p1", StoreRouteData.builder().name("Inbound").routeType(RouteType.of("inbound")).build()),
                () -> client.routes().retrieve("r1"),
                () -> client.routes().update("r1", UpdateRouteData.builder().name("New").build()),
                () -> client.routes().delete("r1"),
                () -> client.routes().verifyInboundDomain("r1"),
                () -> client.stats().retrieve(GetStatsQuery.builder().from("2026-10-01").to("2026-10-31").build()),
                () -> client.suppressions().list(),
                () -> client.suppressions().iterate().forEach(item -> { }),
                () -> client.suppressions().create(StoreSuppressionData.builder().email("a@b.c")
                        .reason(SuppressionCreateReason.of("manual")).scope(SuppressionCreateScope.of("team")).build()),
                () -> client.suppressions().delete("s1"),
                () -> client.team().retrieve(),
                () -> client.team().update(UpdateTeamData.builder().name("Acme").build()),
                () -> client.team().usage(),
                () -> client.team().roles(),
                () -> client.team().members().list(),
                () -> client.team().members().iterate().forEach(item -> { }),
                () -> client.team().members().retrieve("u1"),
                () -> client.team().members().updateAssignment("u1", new UpdateTeamMemberAssignmentData("role1",
                        new UpdateTeamMemberAssignmentDataProjectAccess(ProjectAccessScope.of("all"), null))),
                () -> client.team().invites().list(),
                () -> client.team().invites().iterate().forEach(item -> { }),
                () -> client.team().invites().retrieve("i1"),
                () -> client.team().invites().create(new CreateTeamInviteData("a@b.c", "role1",
                        new CreateTeamInviteDataProjectAccess(ProjectAccessScope.of("all"), null))),
                () -> client.team().invites().update("i1", new UpdateTeamInviteData("a@b.c", "role1",
                        new UpdateTeamInviteDataProjectAccess(ProjectAccessScope.of("all"), null))),
                () -> client.team().invites().revoke("i1"),
                () -> client.webhooks().list(),
                () -> client.webhooks().iterate().forEach(item -> { }),
                () -> client.webhooks().create(StoreWebhookData.builder().name("Hook").url("https://acme.com/hook")
                        .events(List.of(WebhookEvent.MESSAGE_DELIVERED)).build()),
                () -> client.webhooks().retrieve("w1"),
                () -> client.webhooks().update("w1", UpdateWebhookData.builder().enabled(false).build()),
                () -> client.webhooks().delete("w1"),
                () -> client.webhooks().test("w1"),
                () -> client.webhooks().regenerateSecret("w1"),
                () -> client.webhooks().deliveries().list("w1"),
                () -> client.webhooks().deliveries().iterate("w1").forEach(item -> { }),
                () -> client.webhooks().deliveries().retrieve("w1", "dl1"),
                () -> client.webhooks().deliveries().retry("w1", "dl1"));
        calls.forEach(Runnable::run);

        Set<String> reached = new TreeSet<>();
        for (FakeServer.Request request : server.requests()) {
            reached.add(operationKey(request));
        }
        assertTrue(Operations.ALL.keySet().containsAll(PENDING), "every pending operation is generated");
        Set<String> expected = new TreeSet<>(Operations.ALL.keySet());
        expected.removeAll(PENDING);
        assertEquals(expected, reached);
    }

    @Test
    void teamCallsUseTheBearerToken() {
        client.domains().create(new StoreDomainData("acme.com"));
        assertEquals("POST", server.last().method());
        assertEquals("/v1/domains", server.last().path());
        assertEquals("Bearer " + ClientTest.TEAM, server.last().header("authorization"));
        assertNull(server.last().header("x-lettermint-token"));
        assertEquals("{\"domain\":\"acme.com\"}", server.last().body());
    }

    @Test
    void resendsAMessage() {
        server.fallback(request -> FakeServer.Response.json(202, "{\"message_id\":\"m2\",\"status\":\"pending\"}"));
        PendingSendMailResponse resent = client.messages().resend("m 1");
        assertEquals("POST", server.last().method());
        assertEquals("/v1/messages/m%201/resend", server.last().path());
        assertEquals("Bearer " + ClientTest.TEAM, server.last().header("authorization"));
        assertNull(server.last().header("x-lettermint-token"));
        assertEquals("", server.last().body());
        assertNull(server.last().header("content-type"));
        assertNull(server.last().header("idempotency-key"));
        assertEquals("m2", resent.messageId());
        assertEquals("pending", resent.status());

        client.messages().resend("m1", ResendMessageRequest.builder().build());
        assertEquals("{}", server.last().body());

        client.messages().resend("m1", new ResendMessageRequest(List.of("a@b.c", "d@e.f")), SendOptions.idempotencyKey("resend-1"));
        assertEquals("/v1/messages/m1/resend", server.last().path());
        assertTrue(server.last().header("content-type").startsWith("application/json"));
        assertEquals("{\"recipients\":[\"a@b.c\",\"d@e.f\"]}", server.last().body());
        assertEquals("resend-1", server.last().header("idempotency-key"));

        client.messages().resend("m1", null, SendOptions.idempotencyKey("resend-2"));
        assertEquals("", server.last().body());
        assertEquals("resend-2", server.last().header("idempotency-key"));

        int before = server.requests().size();
        assertThrows(LettermintConfigException.class, () -> client.messages().resend(""));
        assertEquals(before, server.requests().size());

        Lettermint sending = Lettermint.builder().sendingToken(ClientTest.SENDING).baseUrl(server.baseUrl()).build();
        sending.messages().resend("m1");
        assertEquals(ClientTest.SENDING, server.last().header("x-lettermint-token"));
        assertNull(server.last().header("authorization"));
    }

    @Test
    void resendMapsGoneToAnApiException() {
        server.enqueue(FakeServer.Response.json(410, "{\"error\":{\"code\":\"MESSAGE_CONTENT_EXPIRED\",\"message\":\"The message content is no longer retained.\"}}"));
        ApiException error = assertThrows(ApiException.class, () -> client.messages().resend("m1"));
        assertEquals(ApiException.class, error.getClass());
        assertEquals(410, error.getStatus());
        assertEquals("MESSAGE_CONTENT_EXPIRED", error.getCode());
        assertEquals("The message content is no longer retained.", error.getMessage());
    }

    @Test
    void messagesExposeTheResentFromMessageId() {
        server.enqueue(FakeServer.Response.json(200, "{\"id\":\"m2\",\"resent_from_message_id\":\"m1\"}"));
        MessageData message = client.messages().retrieve("m2");
        assertEquals("m1", message.resentFromMessageId());
    }

    @Test
    void retriesAWebhookDelivery() {
        server.fallback(request -> FakeServer.Response.json(202, "{\"message\":\"Delivery retry queued.\",\"delivery_id\":\"dl 1\"}"));
        RetryWebhookDeliveryResponse retried = client.webhooks().deliveries().retry("w 1", "dl 1");
        assertEquals("POST", server.last().method());
        assertEquals("/v1/webhooks/w%201/deliveries/dl%201/retry", server.last().path());
        assertEquals("Bearer " + ClientTest.TEAM, server.last().header("authorization"));
        assertNull(server.last().header("x-lettermint-token"));
        assertEquals("", server.last().body());
        assertNull(server.last().header("content-type"));
        assertNull(server.last().header("idempotency-key"));
        assertEquals("Delivery retry queued.", retried.message());
        assertEquals("dl 1", retried.deliveryId());

        client.webhooks().deliveries().retry("w1", "dl1", RequestOptions.timeout(Duration.ofSeconds(5)));
        assertEquals("/v1/webhooks/w1/deliveries/dl1/retry", server.last().path());

        int before = server.requests().size();
        assertThrows(LettermintConfigException.class, () -> client.webhooks().deliveries().retry("", "dl1"));
        assertThrows(LettermintConfigException.class, () -> client.webhooks().deliveries().retry("w1", ".."));
        assertThrows(LettermintConfigException.class, () -> client.webhooks().deliveries().retry("w1", null));
        assertEquals(before, server.requests().size());

        Lettermint sending = Lettermint.builder().sendingToken(ClientTest.SENDING).baseUrl(server.baseUrl()).build();
        assertThrows(LettermintConfigException.class, () -> sending.webhooks().deliveries().retry("w1", "dl1"));
        assertEquals(before, server.requests().size());
    }

    @Test
    void retryingADeliveryMapsErrorsToApiExceptions() {
        server.enqueue(FakeServer.Response.json(409, "{\"error\":{\"code\":\"WEBHOOK_DELIVERY_NOT_RETRYABLE\",\"message\":\"This delivery cannot be retried.\"}}"));
        ConflictException conflict = assertThrows(ConflictException.class, () -> client.webhooks().deliveries().retry("w1", "dl1"));
        assertEquals(409, conflict.getStatus());
        assertEquals("WEBHOOK_DELIVERY_NOT_RETRYABLE", conflict.getCode());
        assertEquals("This delivery cannot be retried.", conflict.getMessage());

        server.enqueue(FakeServer.Response.json(429, "{\"message\":\"Too Many Attempts.\"}").header("Retry-After", "30"));
        RateLimitException limited = assertThrows(RateLimitException.class, () -> client.webhooks().deliveries().retry("w1", "dl1"));
        assertEquals("Too Many Attempts.", limited.getMessage());
        assertNull(limited.getCode());
        assertEquals(Duration.ofSeconds(30), limited.getRetryAfter());

        server.enqueue(FakeServer.Response.json(429, "{\"error\":{\"code\":\"RATE_LIMITED\",\"message\":\"Slow down.\"}}"));
        RateLimitException coded = assertThrows(RateLimitException.class, () -> client.webhooks().deliveries().retry("w1", "dl1"));
        assertEquals("RATE_LIMITED", coded.getCode());
        assertEquals("Slow down.", coded.getMessage());

        server.enqueue(FakeServer.Response.json(401, "{\"message\":\"Unauthenticated.\"}"));
        AuthenticationException unauthenticated = assertThrows(AuthenticationException.class, () -> client.webhooks().deliveries().retry("w1", "dl1"));
        assertEquals("Unauthenticated.", unauthenticated.getMessage());

        server.enqueue(FakeServer.Response.json(404, "{\"message\":\"Not found\"}"));
        assertThrows(NotFoundException.class, () -> client.webhooks().deliveries().retry("w1", "dl1"));
    }

    @Test
    void webhookDeliveriesExposeTheNextRetryAt() {
        server.enqueue(FakeServer.Response.json(200, "{\"id\":\"dl1\",\"next_retry_at\":\"2026-10-18T12:00:00.000000Z\"}"));
        WebhookDeliveryData scheduled = client.webhooks().deliveries().retrieve("w1", "dl1");
        assertEquals("2026-10-18T12:00:00.000000Z", scheduled.nextRetryAt());

        server.enqueue(FakeServer.Response.json(200, "{\"id\":\"dl1\",\"next_retry_at\":null}"));
        assertNull(client.webhooks().deliveries().retrieve("w1", "dl1").nextRetryAt());

        server.enqueue(FakeServer.Response.json(200, "{\"data\":[{\"id\":\"dl1\",\"next_retry_at\":\"2026-10-18T12:00:00.000000Z\"},{\"id\":\"dl2\",\"next_retry_at\":null},{\"id\":\"dl3\"}],\"next_cursor\":null}"));
        List<WebhookDeliveryListData> deliveries = client.webhooks().deliveries().list("w1").data();
        assertEquals("2026-10-18T12:00:00.000000Z", deliveries.get(0).nextRetryAt());
        assertNull(deliveries.get(1).nextRetryAt());
        assertNull(deliveries.get(2).nextRetryAt());
    }

    private static final String INVITE = "{\"id\":\"i 1\",\"email\":\"ada@acme.com\",\"role\":{\"id\":\"role1\",\"name\":\"Developer\"},"
            + "\"project_access\":{\"scope\":\"selected\",\"projects\":[{\"id\":\"p1\",\"name\":\"Production\"}]},\"created_at\":\"2026-10-20T09:00:00.000000Z\"}";

    private void assertTeamRequest(String method, String path) {
        assertEquals(method, server.last().method());
        assertEquals(path, server.last().path());
        assertEquals("Bearer " + ClientTest.TEAM, server.last().header("authorization"));
        assertNull(server.last().header("x-lettermint-token"));
        assertNull(server.last().header("idempotency-key"));
    }

    @Test
    void createsAndUpdatesTeamInvites() {
        server.enqueue(FakeServer.Response.json(201, INVITE));
        TeamInviteData created = client.team().invites().create(new CreateTeamInviteData("ada@acme.com", "role1",
                new CreateTeamInviteDataProjectAccess(ProjectAccessScope.of("selected"), List.of("p1"))));
        assertTeamRequest("POST", "/v1/team/invites");
        assertEquals("{\"email\":\"ada@acme.com\",\"role_id\":\"role1\",\"project_access\":{\"scope\":\"selected\",\"project_ids\":[\"p1\"]}}",
                server.last().body());
        assertEquals("i 1", created.id());
        assertEquals("ada@acme.com", created.email());
        assertEquals("role1", created.role().id());
        assertEquals("Developer", created.role().name());
        assertEquals(ProjectAccessScope.of("selected"), created.projectAccess().scope());
        assertEquals("Production", created.projectAccess().projects().get(0).name());
        assertEquals("2026-10-20T09:00:00.000000Z", created.createdAt());

        server.enqueue(FakeServer.Response.json(201, INVITE));
        client.team().invites().create(CreateTeamInviteData.builder().email("ada@acme.com").roleId("role1")
                .projectAccess(CreateTeamInviteDataProjectAccess.builder().scope(ProjectAccessScope.of("all")).build()).build(),
                RequestOptions.timeout(Duration.ofSeconds(5)));
        assertEquals("{\"email\":\"ada@acme.com\",\"role_id\":\"role1\",\"project_access\":{\"scope\":\"all\"}}", server.last().body());

        server.enqueue(FakeServer.Response.json(200, INVITE));
        TeamInviteData updated = client.team().invites().update("i 1", new UpdateTeamInviteData("ada@acme.com", "role2",
                new UpdateTeamInviteDataProjectAccess(ProjectAccessScope.of("all"), null)));
        assertTeamRequest("PUT", "/v1/team/invites/i%201");
        assertEquals("{\"email\":\"ada@acme.com\",\"role_id\":\"role2\",\"project_access\":{\"scope\":\"all\"}}", server.last().body());
        assertEquals("i 1", updated.id());
    }

    @Test
    void listsRetrievesAndRevokesTeamInvites() {
        server.enqueue(FakeServer.Response.json(200, "{\"data\":[" + INVITE + "],\"path\":null,\"per_page\":2,\"next_cursor\":\"c2\","
                + "\"next_page_url\":null,\"prev_cursor\":null,\"prev_page_url\":null}"));
        CursorPage<TeamInviteData> page = client.team().invites().list(ListTeamInvitesQuery.builder().pageSize(2).pageCursor("c1").build());
        assertTeamRequest("GET", "/v1/team/invites");
        assertEquals("page%5Bsize%5D=2&page%5Bcursor%5D=c1", server.last().rawQuery());
        assertEquals("ada@acme.com", page.data().get(0).email());
        assertEquals("c2", page.nextCursor());

        server.enqueue(FakeServer.Response.json(200, page("c2", "i1", "i2")));
        server.enqueue(FakeServer.Response.json(200, page(null, "i3")));
        List<String> ids = client.team().invites().iterate(ListTeamInvitesQuery.builder().pageSize(2).build()).stream().map(TeamInviteData::id).toList();
        assertEquals(List.of("i1", "i2", "i3"), ids);
        assertEquals("page%5Bsize%5D=2&page%5Bcursor%5D=c2", server.last().rawQuery());

        server.enqueue(FakeServer.Response.json(200, INVITE));
        TeamInviteData invite = client.team().invites().retrieve("i 1");
        assertTeamRequest("GET", "/v1/team/invites/i%201");
        assertEquals("Developer", invite.role().name());

        server.enqueue(FakeServer.Response.json(200, "{\"message\":\"Invite revoked.\"}"));
        MessageResponse revoked = client.team().invites().revoke("i 1");
        assertTeamRequest("DELETE", "/v1/team/invites/i%201");
        assertEquals("", server.last().body());
        assertEquals("Invite revoked.", revoked.message());
    }

    @Test
    void teamInvitesValidateIdsAndNeedTheTeamToken() {
        UpdateTeamInviteData body = new UpdateTeamInviteData("ada@acme.com", "role1",
                new UpdateTeamInviteDataProjectAccess(ProjectAccessScope.of("all"), null));
        assertThrows(LettermintConfigException.class, () -> client.team().invites().retrieve(""));
        assertThrows(LettermintConfigException.class, () -> client.team().invites().update("..", body));
        assertThrows(LettermintConfigException.class, () -> client.team().invites().revoke(null));
        Lettermint sending = Lettermint.builder().sendingToken(ClientTest.SENDING).baseUrl(server.baseUrl()).build();
        assertThrows(LettermintConfigException.class, () -> sending.team().invites().list());
        assertTrue(server.requests().isEmpty());
    }

    @Test
    void teamInviteErrorsMapToApiExceptions() {
        CreateTeamInviteData body = new CreateTeamInviteData("ada@acme.com", "owner",
                new CreateTeamInviteDataProjectAccess(ProjectAccessScope.of("all"), null));
        server.enqueue(FakeServer.Response.json(409, "{\"error\":{\"code\":\"owner_protected\",\"message\":\"The Owner role cannot be assigned.\"}}"));
        ConflictException conflict = assertThrows(ConflictException.class, () -> client.team().invites().create(body));
        assertEquals(409, conflict.getStatus());
        assertEquals("owner_protected", conflict.getCode());
        assertEquals("The Owner role cannot be assigned.", conflict.getMessage());

        server.enqueue(FakeServer.Response.json(422, "{\"message\":\"The email has already been invited.\",\"errors\":{\"email\":[\"The email has already been invited.\"]}}"));
        ValidationException invalid = assertThrows(ValidationException.class, () -> client.team().invites().create(body));
        assertEquals(422, invalid.getStatus());
        assertEquals(List.of("The email has already been invited."), invalid.getErrors().get("email"));

        server.enqueue(FakeServer.Response.json(404, "{\"message\":\"Not found\"}"));
        assertThrows(NotFoundException.class, () -> client.team().invites().revoke("i1"));
    }

    @Test
    void updatesSendExplicitNulls() {
        client.routes().update("r1", UpdateRouteData.builder().inboundDomain(null).name("Main").build());
        assertEquals("PUT", server.last().method());
        assertEquals("{\"name\":\"Main\",\"inbound_domain\":null}", server.last().body());
    }

    @Test
    void listsReturnTypedPages() {
        server.enqueue(FakeServer.Response.json(200, page("c2", "d1", "d2")));
        CursorPage<DomainListData> first = client.domains().list(ListDomainsQuery.builder().pageSize(2).build());
        assertEquals(List.of("d1", "d2"), first.data().stream().map(DomainListData::id).toList());
        assertEquals("c2", first.nextCursor());
        assertTrue(first.hasNextPage());
        assertEquals(2L, first.perPage());
    }

    @Test
    void iterateFollowsTheCursor() {
        server.enqueue(FakeServer.Response.json(200, page("c2", "d1", "d2")));
        server.enqueue(FakeServer.Response.json(200, page("c3", "d3")));
        server.enqueue(FakeServer.Response.json(200, page(null, "d4")));
        CursorIterable<DomainListData> domains = client.domains().iterate(ListDomainsQuery.builder().pageSize(2).build());
        assertTrue(server.requests().isEmpty(), "iterate() is lazy");
        List<String> ids = new ArrayList<>();
        for (DomainListData domain : domains) {
            ids.add(domain.id());
        }
        assertEquals(List.of("d1", "d2", "d3", "d4"), ids);
        List<String> queries = server.requests().stream().map(FakeServer.Request::rawQuery).toList();
        assertEquals(List.of("page%5Bsize%5D=2", "page%5Bsize%5D=2&page%5Bcursor%5D=c2", "page%5Bsize%5D=2&page%5Bcursor%5D=c3"), queries);
    }

    @Test
    void iterateUsesTheOperationsCursorParameterAndStopsOnARepeatedCursor() {
        server.enqueue(FakeServer.Response.json(200, page("same", "w1")));
        server.enqueue(FakeServer.Response.json(200, page("same", "w2")));
        server.enqueue(FakeServer.Response.json(200, page("never", "w3")));
        List<String> ids = client.webhooks().iterate().stream().map(WebhookListData::id).toList();
        assertEquals(List.of("w1", "w2"), ids);
        assertEquals("cursor=same", server.requests().get(1).rawQuery());
        assertEquals(2, server.requests().size());

        server.enqueue(FakeServer.Response.json(200, page("c2", "dl1")));
        server.enqueue(FakeServer.Response.json(200, page(null, "dl2")));
        client.webhooks().deliveries().iterate("w1", ListWebhookDeliveriesQuery.builder().filterEventType("message.delivered").build())
                .forEach(item -> { });
        assertEquals("filter%5Bevent_type%5D=message.delivered&cursor=c2", server.last().rawQuery());
    }

    @Test
    void streamsStopEarly() {
        server.enqueue(FakeServer.Response.json(200, page("c2", "d1", "d2")));
        server.enqueue(FakeServer.Response.json(200, page(null, "d3")));
        assertEquals(List.of("d1"), client.domains().iterate().stream().limit(1).map(DomainListData::id).toList());
        assertEquals(1, server.requests().size());
    }

    @Test
    void iterateReportsBadPagesAndErrors() {
        server.enqueue(FakeServer.Response.json(200, "{\"next_cursor\":null}"));
        UnexpectedResponseException bad = assertThrows(UnexpectedResponseException.class, () -> client.domains().iterate().iterator().hasNext());
        assertTrue(bad.getMessage().startsWith("domains.iterate: the API returned a page without a data array."));
        server.enqueue(FakeServer.Response.json(404, "{\"message\":\"Not found\"}"));
        assertThrows(NotFoundException.class, () -> client.routes().iterate("p1").iterator().next());
    }
}
