package co.lettermint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.lettermint.exceptions.ServerException;
import co.lettermint.exceptions.TimeoutException;
import co.lettermint.exceptions.ValidationException;
import co.lettermint.types.AnalyticsBreakdownRow;
import co.lettermint.types.AnalyticsComparison;
import co.lettermint.types.AnalyticsGroupDimension;
import co.lettermint.types.AnalyticsInterval;
import co.lettermint.types.AnalyticsMetaComparison;
import co.lettermint.types.AnalyticsMetric;
import co.lettermint.types.AnalyticsMetricChange;
import co.lettermint.types.AnalyticsMetricValues;
import co.lettermint.types.AnalyticsPagination;
import co.lettermint.types.AnalyticsQuery;
import co.lettermint.types.AnalyticsRateBase;
import co.lettermint.types.AnalyticsRateBases;
import co.lettermint.types.AnalyticsResponse;
import co.lettermint.types.AnalyticsSection;
import co.lettermint.types.AnalyticsSummary;
import co.lettermint.types.AnalyticsTimeSeriesPoint;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AnalyticsTest {
    private static final String PAGE_ONE = fixture("analytics-page-1.json");
    private static final String PAGE_TWO = fixture("analytics-page-2.json");
    private static final String SUMMARY_ONLY = fixture("analytics-summary.json");
    private static final String QUERY_JSON = "{\"metrics\":[\"delivered\",\"bounced\",\"delivery_rate\",\"delivery_latency_p50_ms\"],"
            + "\"timezone\":\"Asia/Kolkata\",\"include\":[\"summary\",\"time_series\",\"breakdown\"],\"group_by\":[\"recipient_domain\"],"
            + "\"interval\":\"hour\",\"compare\":\"previous_period\",\"limit\":2}";
    private static final String QUERY_JSON_WITH_CURSOR = QUERY_JSON.replace("\"limit\":2}", "\"limit\":2,\"cursor\":\"cursor-page-2\"}");

    private FakeServer server;
    private Lettermint client;

    @BeforeEach
    void start() throws Exception {
        server = new FakeServer();
        client = Lettermint.builder().teamToken(ClientTest.TEAM).baseUrl(server.baseUrl()).build();
    }

    @AfterEach
    void stop() {
        server.close();
    }

    private static String fixture(String name) {
        try (InputStream in = AnalyticsTest.class.getResourceAsStream("/" + name)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
    }

    private static AnalyticsQuery query() {
        return AnalyticsQuery.builder()
                .metrics(List.of(AnalyticsMetric.DELIVERED, AnalyticsMetric.BOUNCED, AnalyticsMetric.DELIVERY_RATE, AnalyticsMetric.DELIVERY_LATENCY_P50_MS))
                .include(List.of(AnalyticsSection.SUMMARY, AnalyticsSection.TIME_SERIES, AnalyticsSection.BREAKDOWN))
                .groupBy(List.of(AnalyticsGroupDimension.RECIPIENT_DOMAIN))
                .interval(AnalyticsInterval.HOUR)
                .timezone("Asia/Kolkata")
                .compare(AnalyticsComparison.PREVIOUS_PERIOD)
                .limit(2)
                .build();
    }

    // ------------------------------------------------------------ responses
    @Test
    void keepsNullValuesEmptyRateBasesAndBothTimestampFormats() {
        server.enqueue(FakeServer.Response.json(200, PAGE_ONE));
        AnalyticsResponse result = client.analytics(query());

        assertEquals("POST", server.last().method());
        assertEquals("/v1/analytics", server.last().path());
        assertEquals(QUERY_JSON, server.last().body());
        AnalyticsSummary summary = result.data().summary();
        assertEquals(AnalyticsMetricValues.builder().delivered(1200).bounced(0).deliveryRate(0.9836).build(), summary.metrics());
        assertEquals(0L, summary.metrics().bounced());
        assertNull(summary.metrics().deliveryLatencyP50Ms());
        assertEquals(new AnalyticsRateBase(1200L, 1220L), summary.rateBases().deliveryRate());
        assertEquals(new AnalyticsRateBase(null, null), summary.previous().rateBases().deliveryRate());
        assertEquals(new AnalyticsMetricChange(null, null, null), summary.change().get("delivery_rate"));
        assertEquals(new AnalyticsMetricChange(100.0, 0.0909, null), summary.change().get("delivered"));

        AnalyticsTimeSeriesPoint complete = result.data().timeSeries().get(0);
        AnalyticsTimeSeriesPoint unavailable = result.data().timeSeries().get(1);
        assertEquals("2026-09-15T08:30:00+05:30", complete.from());
        assertEquals(Instant.parse("2026-09-15T03:00:00Z"), OffsetDateTime.parse(complete.from()).toInstant());
        assertFalse(unavailable.available());
        assertTrue(unavailable.partial());
        assertEquals(AnalyticsRateBases.builder().build(), unavailable.rateBases());
        assertNull(unavailable.metrics().delivered());

        assertEquals("2026-09-15T04:12:30.482915Z", result.meta().generatedAt());
        assertEquals(Instant.parse("2026-09-15T04:12:30Z").plusNanos(482_915_000), OffsetDateTime.parse(result.meta().generatedAt()).toInstant());
        assertEquals(Instant.parse("2026-09-15T03:00:00Z"), OffsetDateTime.parse(result.meta().from()).toInstant());
        assertNull(result.meta().lastIngestedAt());
        assertEquals(new AnalyticsMetaComparison("2026-09-15T01:00:00.000000Z", "2026-09-15T03:00:00.000000Z", false), result.meta().comparison());
        assertEquals(new AnalyticsPagination(3L, 2L, "cursor-page-2", false), result.pagination());
    }

    @Test
    void leavesOutTheSectionsAndComparisonAQueryDidNotAskFor() {
        server.enqueue(FakeServer.Response.json(200, SUMMARY_ONLY));
        AnalyticsResponse result = client.analytics(AnalyticsQuery.builder().metrics(List.of(AnalyticsMetric.DELIVERED)).build());
        assertEquals("{\"metrics\":[\"delivered\"]}", server.last().body());
        assertEquals(AnalyticsRateBases.builder().build(), result.data().summary().rateBases());
        assertNull(result.data().timeSeries());
        assertNull(result.data().breakdown());
        assertNull(result.data().summary().previous());
        assertNull(result.data().summary().change());
        assertNull(result.meta().comparison());
        assertNull(result.pagination().nextCursor());
    }

    @Test
    void keepsANullDimensionValueInABreakdownRow() {
        server.enqueue(FakeServer.Response.json(200, PAGE_TWO));
        AnalyticsResponse result = client.analytics(query().toBuilder().cursor("cursor-page-2").build());
        assertEquals(QUERY_JSON_WITH_CURSOR, server.last().body());
        assertEquals(1, result.data().breakdown().size());
        assertEquals(Collections.singletonMap("recipient_domain", null), result.data().breakdown().get(0).dimensions());
        assertEquals(50L, result.data().breakdown().get(0).metrics().delivered());
        assertNull(result.data().breakdown().get(0).metrics().bounced());
    }

    // ------------------------------------------------------------ analyticsPages
    @Test
    void analyticsPagesFollowsNextCursorAndYieldsEveryResponse() {
        server.enqueue(FakeServer.Response.json(200, PAGE_ONE));
        server.enqueue(FakeServer.Response.json(200, PAGE_TWO));
        AnalyticsQuery sent = query();
        List<AnalyticsResponse> pages = new ArrayList<>();
        for (AnalyticsResponse page : client.analyticsPages(sent)) {
            pages.add(page);
        }

        assertEquals(2, pages.size());
        assertEquals(2, server.requests().size());
        assertTrue(server.requests().stream().allMatch(request -> request.method().equals("POST") && request.path().equals("/v1/analytics")));
        assertTrue(server.requests().stream().allMatch(request -> ("Bearer " + ClientTest.TEAM).equals(request.header("authorization"))));
        assertEquals(QUERY_JSON, server.requests().get(0).body());
        assertEquals(QUERY_JSON_WITH_CURSOR, server.requests().get(1).body());
        assertEquals(query(), sent);
        assertNull(sent.cursor());

        List<String> domains = pages.stream().flatMap(page -> page.data().breakdown().stream())
                .map(AnalyticsBreakdownRow::dimensions).map(dimensions -> dimensions.get("recipient_domain")).toList();
        assertEquals(Arrays.asList("gmail.com", "outlook.com", null), domains);
        assertEquals(1L, pages.get(1).pagination().returnedGroups());
        assertNull(pages.get(1).pagination().nextCursor());
    }

    @Test
    void analyticsPagesMakesOneRequestForAQueryWithoutMorePages() {
        server.enqueue(FakeServer.Response.json(200, SUMMARY_ONLY));
        List<AnalyticsResponse> pages = client.analyticsPages(AnalyticsQuery.builder().metrics(List.of(AnalyticsMetric.DELIVERED)).build()).stream().toList();
        assertEquals(1, pages.size());
        assertEquals(1, server.requests().size());
    }

    @Test
    void analyticsPagesRequestsTheNextPageOnlyWhenItIsAskedFor() {
        server.fallback(request -> FakeServer.Response.json(200, PAGE_ONE));
        CursorIterable<AnalyticsResponse> pages = client.analyticsPages(query());
        assertTrue(server.requests().isEmpty(), "analyticsPages() is lazy");
        for (AnalyticsResponse page : pages) {
            assertEquals("cursor-page-2", page.pagination().nextCursor());
            break;
        }
        assertEquals(1, server.requests().size());
        assertEquals(1, client.analyticsPages(query()).stream().limit(1).count());
        assertEquals(2, server.requests().size());
    }

    @Test
    void analyticsPagesStopsWhenTheApiRepeatsACursor() {
        server.fallback(request -> FakeServer.Response.json(200, PAGE_ONE));
        assertEquals(2, client.analyticsPages(query()).stream().toList().size());
        assertEquals(2, server.requests().size());
    }

    @Test
    void analyticsPagesStopsWhenTheApiReturnsTheCursorTheQueryStartedFrom() {
        server.fallback(request -> FakeServer.Response.json(200, PAGE_ONE));
        assertEquals(1, client.analyticsPages(query().toBuilder().cursor("cursor-page-2").build()).stream().toList().size());
        assertEquals(1, server.requests().size());
        assertEquals(QUERY_JSON_WITH_CURSOR, server.requests().get(0).body());
    }

    @Test
    void analyticsPagesSurfacesAnExpiredCursor() {
        String message = "The analytics cursor is invalid or expired. Submit a new query.";
        server.enqueue(FakeServer.Response.json(200, PAGE_ONE));
        server.enqueue(FakeServer.Response.json(422, "{\"message\":\"" + message + "\",\"errors\":{\"cursor\":[\"" + message + "\"]}}"));
        Iterator<AnalyticsResponse> pages = client.analyticsPages(query()).iterator();
        assertEquals("cursor-page-2", pages.next().pagination().nextCursor());
        ValidationException error = assertThrows(ValidationException.class, pages::hasNext);
        assertEquals(Map.of("cursor", List.of(message)), error.getErrors());
        assertEquals(2, server.requests().size());
    }

    @Test
    void analyticsPagesPassesRequestOptionsToEveryPage() {
        RequestOptions options = RequestOptions.timeout(Duration.ofMillis(200));
        server.enqueue(FakeServer.Response.json(200, PAGE_ONE).delay(3000));
        TimeoutException first = assertThrows(TimeoutException.class, () -> client.analyticsPages(query(), options).iterator().hasNext());
        assertEquals(Duration.ofMillis(200), first.getTimeout());

        server.enqueue(FakeServer.Response.json(200, PAGE_ONE));
        server.enqueue(FakeServer.Response.json(200, PAGE_TWO).delay(3000));
        Iterator<AnalyticsResponse> pages = client.analyticsPages(query(), options).iterator();
        pages.next();
        TimeoutException second = assertThrows(TimeoutException.class, pages::next);
        assertEquals(Duration.ofMillis(200), second.getTimeout());
        assertEquals(QUERY_JSON_WITH_CURSOR, server.last().body());
    }

    // ------------------------------------------------------------ errors
    @Test
    void readsFieldErrorsFromA422() {
        String message = "smtp_response_group can only be used in group_by.";
        server.enqueue(FakeServer.Response.json(422, "{\"message\":\"" + message + "\",\"errors\":{\"filters\":[\"" + message + "\"]}}"));
        ValidationException error = assertThrows(ValidationException.class, () -> client.analytics(query()));
        assertEquals(422, error.getStatus());
        assertEquals(message, error.getMessage());
        assertEquals(Map.of("filters", List.of(message)), error.getErrors());
    }

    @Test
    void readsRetryAfterFromA503() {
        server.enqueue(FakeServer.Response.json(503, "{\"error\":{\"code\":\"SERVICE_UNAVAILABLE\",\"message\":\"Try again shortly.\"}}").header("Retry-After", "2"));
        ServerException error = assertThrows(ServerException.class, () -> client.analytics(query()));
        assertEquals(503, error.getStatus());
        assertEquals("SERVICE_UNAVAILABLE", error.getCode());
        assertEquals(Duration.ofSeconds(2), error.getRetryAfter());
    }

    @Test
    void hasNoRetryAfterForA503Or504WithoutTheHeader() {
        server.enqueue(FakeServer.Response.json(503, "{\"message\":\"Analytics is unavailable.\"}"));
        ServerException unavailable = assertThrows(ServerException.class, () -> client.analytics(query()));
        assertEquals(ServerException.class, unavailable.getClass());
        assertNull(unavailable.getRetryAfter());

        String message = "Analytics exceeded the query time limit. Retry with a shorter period or fewer dimensions.";
        server.enqueue(FakeServer.Response.json(504, "{\"message\":\"" + message + "\"}"));
        ServerException timedOut = assertThrows(ServerException.class, () -> client.analytics(query()));
        assertEquals(504, timedOut.getStatus());
        assertEquals(message, timedOut.getMessage());
        assertNull(timedOut.getRetryAfter());
    }
}
