package co.lettermint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.lettermint.exceptions.NotFoundException;
import co.lettermint.exceptions.UnexpectedResponseException;
import co.lettermint.types.AnalyticsMetric;
import co.lettermint.types.AnalyticsQuery;
import co.lettermint.types.CursorPage;
import co.lettermint.types.DomainListData;
import co.lettermint.types.GetStatsQuery;
import co.lettermint.types.ListDomainsQuery;
import co.lettermint.types.ListWebhookDeliveriesQuery;
import co.lettermint.types.Operations;
import co.lettermint.types.ProjectAccessScope;
import co.lettermint.types.ReportForwardingRequest;
import co.lettermint.types.RescheduleMessageRequest;
import co.lettermint.types.RouteType;
import co.lettermint.types.StoreDomainData;
import co.lettermint.types.StoreProjectData;
import co.lettermint.types.StoreRouteData;
import co.lettermint.types.StoreSuppressionData;
import co.lettermint.types.StoreWebhookData;
import co.lettermint.types.SuppressionCreateReason;
import co.lettermint.types.SuppressionCreateScope;
import co.lettermint.types.UpdateDomainProjectsData;
import co.lettermint.types.UpdateProjectData;
import co.lettermint.types.UpdateRouteData;
import co.lettermint.types.UpdateTeamData;
import co.lettermint.types.UpdateTeamMemberAssignmentData;
import co.lettermint.types.UpdateTeamMemberAssignmentDataProjectAccess;
import co.lettermint.types.UpdateWebhookData;
import co.lettermint.types.VerifyReportForwardingRequest;
import co.lettermint.types.WebhookEvent;
import co.lettermint.types.WebhookListData;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TeamApiTest {
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
                () -> client.webhooks().deliveries().retrieve("w1", "dl1"));
        calls.forEach(Runnable::run);

        Set<String> reached = new TreeSet<>();
        for (FakeServer.Request request : server.requests()) {
            reached.add(operationKey(request));
        }
        assertEquals(new TreeSet<>(Operations.ALL.keySet()), reached);
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
