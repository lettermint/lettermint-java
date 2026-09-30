package co.lettermint;

import co.lettermint.api.ApiClient;
import co.lettermint.models.api.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

class AnalyticsForwardingTest {
    @Test
    void analyticsAndForwardingUseTheirWireContracts() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse().setBody("{\"data\":{\"summary\":{}},\"meta\":{\"timezone\":\"UTC\"},\"pagination\":{\"total_groups\":0,\"returned_groups\":0,\"next_cursor\":null,\"truncated\":false}}"));
            for (int i=0; i<4; i++) server.enqueue(new MockResponse().setBody("{\"data\":{\"destination\":null,\"verified\":false,\"verified_at\":null}}"));
            server.enqueue(new MockResponse().setResponseCode(204));
            server.start();
            ApiClient api = Lettermint.api("team-token", server.url("/v1").toString());
            AnalyticsRequest payload = new AnalyticsRequest();
            payload.metrics = Arrays.asList("accepted", "delivered");
            payload.include = Arrays.asList("summary");
            assertEquals("UTC", api.analytics(payload).meta.timezone);
            assertNull(api.projects().retrieveReportForwarding("project/id").data.destination);
            ReportForwardingRequest destination = new ReportForwardingRequest();
            destination.destination = "reports@example.com";
            api.projects().updateReportForwarding("project/id", destination);
            VerifyReportForwardingRequest code = new VerifyReportForwardingRequest();
            code.code = "123456";
            api.projects().verifyReportForwarding("project/id", code);
            api.projects().resendReportForwardingCode("project/id");
            api.projects().deleteReportForwarding("project/id");
            String base = "/v1/projects/project%2Fid/report-forwarding";
            String[] paths = {"/v1/analytics", base, base, base+"/verify", base+"/resend-code", base};
            String[] methods = {"POST", "GET", "PUT", "POST", "POST", "DELETE"};
            ObjectMapper mapper = new ObjectMapper();
            for (int i=0; i<6; i++) {
                RecordedRequest request = server.takeRequest();
                assertEquals(paths[i], request.getPath());
                assertEquals(methods[i], request.getMethod());
                assertEquals("Bearer team-token", request.getHeader("Authorization"));
                assertNull(request.getHeader("x-lettermint-token"));
                if (i==0) assertEquals("accepted", mapper.readTree(request.getBody().readUtf8()).get("metrics").get(0).asText());
                if (i==2) assertEquals("reports@example.com", mapper.readTree(request.getBody().readUtf8()).get("destination").asText());
                if (i==3) assertEquals("123456", mapper.readTree(request.getBody().readUtf8()).get("code").asText());
            }
        }
    }

    @Test
    void updatedResponseFieldsRemainAvailable() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ProjectStoreResponse created = mapper.readValue("{\"data\":{},\"message\":\"Created\",\"api_token\":\"project-token\"}", ProjectStoreResponse.class);
        assertEquals("project-token", created.apiToken);
        SuppressionDestroyResponse review = mapper.readValue("{\"success\":true,\"status\":\"review_ticket_exists\",\"message\":\"Review\",\"ticket_identifier\":\"ticket-1\",\"confidence\":0.9}", SuppressionDestroyResponse.class);
        assertEquals("ticket-1", review.ticketIdentifier);
        assertEquals(0.9, review.confidence);
    }
}
