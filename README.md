# Lettermint Java SDK

![Maven Central Version](https://img.shields.io/maven-central/v/co.lettermint/lettermint)
![Build Status](https://img.shields.io/github/actions/workflow/status/lettermint/lettermint-java/release.yml?branch=main)
[![Join our Discord server](https://img.shields.io/discord/1305510095588819035?logo=discord&logoColor=eee&label=Discord&labelColor=464ce5&color=0D0E28&cacheSeconds=43200)](https://lettermint.co/r/discord)

The official Java SDK for [Lettermint](https://lettermint.co). It runs on Java 17 or later, uses the JDK's `java.net.http` client and depends only on Jackson.

Upgrading from 2.x? Read [UPGRADE.md](UPGRADE.md).

## Installation

### Maven

```xml
<dependency>
    <groupId>co.lettermint</groupId>
    <artifactId>lettermint</artifactId>
    <version>3.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'co.lettermint:lettermint:3.0.0'
```

## Quick start

Create a client with a project sending token and send an email:

```java
import co.lettermint.EmailMessage;
import co.lettermint.Lettermint;
import co.lettermint.types.SendMailResponse;

Lettermint lettermint = Lettermint.builder()
    .sendingToken(System.getenv("LETTERMINT_PROJECT_TOKEN"))
    .build();

SendMailResponse result = lettermint.emails().send(EmailMessage.create()
    .from("Acme <hello@acme.com>")
    .to("jane@example.com")
    .subject("Welcome to Acme")
    .html("<p>Thanks for signing up.</p>")
    .text("Thanks for signing up."));

System.out.println(result.messageId() + " " + result.status()); // "…", "pending"
```

The client is immutable and thread-safe and holds no message state. Create it once (for example as a Spring bean) and share it.

## Tokens

Lettermint has two kinds of API tokens:

| Builder method | Token | Used by | Sent as |
| --- | --- | --- | --- |
| `sendingToken(...)` | Project sending token (`lm_…`) | `lettermint.emails()` | `x-lettermint-token` header |
| `teamToken(...)` | Team API token (`lm_team_…`) | Every other part (domains, messages, projects, …) | `Authorization: Bearer` header |

Pass one or both:

```java
Lettermint lettermint = Lettermint.builder()
    .sendingToken(System.getenv("LETTERMINT_PROJECT_TOKEN"))
    .teamToken(System.getenv("LETTERMINT_TEAM_TOKEN"))
    .build();
```

Each part uses its own token and never falls back to the other one. If the token a method needs is missing, it throws a `LettermintConfigException` that names it (`domains.list needs teamToken; …`), before any request. `lettermint.ping()` uses the team token when it is set, otherwise the sending token. `messages().reschedule()` and `messages().cancel()` accept either token in the same way.

You can also pass a single token and let the SDK choose its type by the format: `lm_team_` followed by letters and digits is a team token, and `lm_` followed by letters and digits is a sending token.

```java
Lettermint lettermint = Lettermint.of(System.getenv("LETTERMINT_TOKEN"));
// With other options:
Lettermint lettermint = Lettermint.builder().token(System.getenv("LETTERMINT_TOKEN")).timeout(Duration.ofSeconds(10)).build();
```

Any other format, such as an SSO verification token (`lm_sso_…`), throws `LettermintConfigException`; use `sendingToken(...)` or `teamToken(...)` for those. Error messages never contain the token.

### Options

| Builder method | Default | Description |
| --- | --- | --- |
| `sendingToken(String)` | | Project sending token. |
| `teamToken(String)` | | Team API token. |
| `token(String)` | | Either token, detected by its format. |
| `baseUrl(String)` | `https://api.lettermint.co/v1` | API base URL. |
| `timeout(Duration)` | 30 seconds | Request timeout. It covers the whole request: connecting, sending, the response headers and the body. |
| `httpClient(HttpClient)` | a new `java.net.http.HttpClient` | Your own client, for example with a proxy, TLS settings or an executor. It must not follow redirects (`HttpClient.Redirect.NEVER`, the JDK default). |

## Sending email

### Messages

`EmailMessage` is immutable: every setter returns a new message and leaves the original unchanged. Each setter has an accessor of the same name (`message.subject()`).

```java
EmailMessage message = EmailMessage.create()
    .from("Acme <hello@acme.com>")
    .to("jane@example.com")
    .replyTo("support@acme.com")
    .subject("Your order has shipped")
    .html(html)
    .metadata(Map.of("order_id", "1234"));

lettermint.emails().send(message);
```

### The email builder

`emails().compose()` returns an immutable builder bound to the client, with the same setters plus `send()`. Every setter returns a new builder, so you can keep a base builder and reuse it, also across threads:

```java
EmailBuilder welcome = lettermint.emails().compose()
    .from("Acme <hello@acme.com>")
    .subject("Welcome to Acme")
    .tags(new MessageTagInput("campaign", "welcome"));

welcome.to("jane@example.com").html("<p>Hi Jane</p>").send();
welcome.to("john@example.com").html("<p>Hi John</p>").send();
```

When you build an email over several statements, keep the returned builder:

```java
EmailBuilder email = lettermint.emails().compose().from("hello@acme.com").to(user.email()).subject("Your invoice");
if (user.accountant() != null) {
    email = email.cc(user.accountant());
}
email.html(invoiceHtml).send();
```

| Method | Description |
| --- | --- |
| `from(address)` | Sender, for example `Acme <hello@acme.com>`. |
| `to(...)`, `cc(...)`, `bcc(...)`, `replyTo(...)` | Replace the recipient list (varargs or a collection). |
| `subject(text)` | Subject line. |
| `html(html)`, `text(text)` | Bodies. `null` removes one. |
| `headers(map)` | Custom email headers. |
| `metadata(map)` | Data stored with the message, not added as headers. |
| `tags(MessageTagInput...)`, `tag(name)` | Name/value tags, and the legacy single tag. |
| `route(slug)` | The route to send through. |
| `scheduledAt(String or Instant)` | Delivery time: an `Instant`, ISO 8601, or English such as `tomorrow 9am`. |
| `settings(SendMailRequestSettings)` | Per-email settings that override the route. |
| `sandboxResult(SandboxResult)` | The result a Sandbox project simulates. |
| `attach(EmailAttachment)` | Adds an attachment. |
| `send()`, `send(SendOptions)` | Sends the email. The builder can be sent again. |
| `build()` | Returns the `EmailMessage`. |

`emails().compose(message)` starts a builder from a message. `message.toRequest()` returns the API's wire format (`SendMailRequest`).

### Batch sending

Send up to 500 emails in one request:

```java
List<SendMailResponse> results = lettermint.emails().sendBatch(List.of(
    EmailMessage.create().from("hello@acme.com").to("jane@example.com").subject("Hi Jane").text("Hello"),
    welcome.to("john@example.com").html("<p>Hi John</p>").build()));
```

### Idempotency

Pass an idempotency key to make retries safe. The API processes a key once, so a retry with the same key does not send the email again. The key applies only to the call it is passed to.

```java
lettermint.emails().send(message, SendOptions.idempotencyKey("order-" + order.id() + "-confirmation"));
builder.send(SendOptions.idempotencyKey("welcome-jane"));
lettermint.emails().sendBatch(messages, SendOptions.idempotencyKey("newsletter-2026-10"));
```

The SDK never retries on its own.

### Scheduling

```java
SendMailResponse result = lettermint.emails().compose()
    .from("hello@acme.com")
    .to("jane@example.com")
    .subject("Your trial ends tomorrow")
    .text("…")
    .scheduledAt(Instant.now().plus(Duration.ofDays(1)))
    .send();

if (MessageStatus.SCHEDULED.equals(result.status())) {
    System.out.println(result.scheduledAt());
}

lettermint.messages().reschedule(result.messageId(), new RescheduleMessageRequest("2026-10-20T09:00:00Z"));
lettermint.messages().cancel(result.messageId());
```

### Sandbox

In a Sandbox project, nothing is delivered. Choose the simulated result per email:

```java
SendMailResponse result = lettermint.emails().compose()
    .from("hello@acme.com")
    .to("jane@example.com")
    .subject("Test")
    .text("Test")
    .sandboxResult(SandboxResult.HARD_BOUNCED)
    .send();

System.out.println(result.sandbox() + " " + result.sandboxResult()); // true hard_bounced
```

### Tags

`tags(...)` accepts up to 20 case-sensitive name/value tags (19 when the legacy `tag(...)` is also set). Names match `^[A-Za-z0-9_-]{1,32}$`, may not start with `__lettermint` and must be unique. Values match `^[A-Za-z0-9_-]{1,64}$`. The SDK checks this before the request and throws `LettermintValidationException`. Because messages and builders are immutable, a rejected tag leaves them unchanged.

### Attachments

```java
lettermint.emails().compose()
    .from("billing@acme.com")
    .to("jane@example.com")
    .subject("Your invoice")
    .html("<img src=\"cid:logo\"> Your invoice is attached.")
    .attach(EmailAttachment.of("invoice.pdf", Files.readAllBytes(invoicePath)).contentType("application/pdf"))
    .attach(EmailAttachment.ofBase64("logo.png", logoBase64).contentId("logo"))
    .send();
```

`EmailAttachment.of` takes raw bytes, which the SDK base64-encodes; `ofBase64` takes content that is already encoded. `lettermint.blockedFileTypes()` lists the extensions and MIME types the API rejects.

## Team API

With a team token, the client manages domains, messages, projects, routes, statistics, suppressions, the team and webhooks:

```java
Lettermint lettermint = Lettermint.builder().teamToken(System.getenv("LETTERMINT_TEAM_TOKEN")).build();

DomainData domain = lettermint.domains().create(new StoreDomainData("acme.com"));
lettermint.domains().verifyDnsRecords(domain.id());

ProjectCreatedData project = lettermint.projects().create(StoreProjectData.builder().name("Production").build());
System.out.println(project.apiToken()); // the new project's sending token, shown once

StatsData stats = lettermint.stats().retrieve(GetStatsQuery.builder().from("2026-10-01").to("2026-10-31").build());
String html = lettermint.messages().html("message-id");
```

| Accessor | Methods |
| --- | --- |
| `domains()` | `list`, `iterate`, `create`, `retrieve`, `delete`, `verifyDnsRecords`, `verifyDnsRecord`, `updateProjects` |
| `messages()` | `list`, `iterate`, `retrieve`, `events`, `iterateEvents`, `source`, `html`, `text`, `reschedule`, `cancel`, `process` |
| `projects()` | `list`, `iterate`, `create`, `retrieve`, `update`, `delete`, `rotateToken` |
| `projects().reportForwarding()` | `retrieve`, `update`, `delete`, `verify`, `resendCode` |
| `routes()` | `list(projectId)`, `iterate(projectId)`, `create(projectId, …)`, `retrieve`, `update`, `delete`, `verifyInboundDomain` |
| `stats()` | `retrieve` |
| `suppressions()` | `list`, `iterate`, `create`, `delete` |
| `team()` | `retrieve`, `update`, `usage`, `roles` |
| `team().members()` | `list`, `iterate`, `retrieve`, `updateAssignment` |
| `webhooks()` | `list`, `iterate`, `create`, `retrieve`, `update`, `delete`, `test`, `regenerateSecret` |
| `webhooks().deliveries()` | `list(webhookId)`, `iterate(webhookId)`, `retrieve(webhookId, deliveryId)` |
| (root) | `ping`, `analytics`, `blockedFileTypes` |

Request and response types are immutable records in `co.lettermint.types`. Build requests with their builder (`UpdateRouteData.builder().name("Main").build()`) or, for small ones, the constructor (`new StoreDomainData("acme.com")`). In update requests, a field that is optional and nullable is an `OptionalNullable`: leave it unset to keep the current value, or pass `null` to the builder to clear it:

```java
lettermint.routes().update(routeId, UpdateRouteData.builder().inboundDomain(null).build()); // {"inbound_domain": null}
```

### Query parameters and pagination

Query parameters are typed records with builders. The SDK sends them in the API's bracket syntax (`page[size]=30&filter[status]=verified&sort=-created_at`):

```java
CursorPage<DomainListData> page = lettermint.domains().list(ListDomainsQuery.builder()
    .pageSize(30)
    .filterStatus(DomainStatus.VERIFIED)
    .sort(List.of(ListDomainsQuerySortItem.CREATED_AT_DESC))
    .build());

System.out.println(page.data().size() + " " + page.nextCursor());
```

Every list has an `iterate()` method that follows `nextCursor` until the last page. It returns a `CursorIterable`, which you can loop over or stream; pages are requested only when you get to them:

```java
for (MessageListData message : lettermint.messages().iterate(ListMessagesQuery.builder().filterStatus(MessageStatus.HARD_BOUNCED).build())) {
    System.out.println(message.id() + " " + message.subject());
}

lettermint.webhooks().deliveries().iterate(webhookId).stream().limit(100).forEach(System.out::println);
```

### Timeouts and cancellation

Every method takes an optional last argument with per-call options: `RequestOptions.timeout(Duration)`, or `SendOptions` for calls that also take an idempotency key.

```java
lettermint.messages().list(null, RequestOptions.timeout(Duration.ofSeconds(5)));
```

To cancel a call, interrupt the calling thread. The SDK aborts the request and throws `java.util.concurrent.CancellationException` with the thread's interrupt status set.

The SDK is synchronous. On Java 21, virtual threads make blocking calls cheap; to run a call asynchronously, use `CompletableFuture.supplyAsync(() -> lettermint.emails().send(message), executor)`.

## Errors

Every exception the SDK throws is unchecked and extends `LettermintException`:

| Class | When | Accessors |
| --- | --- | --- |
| `ApiException` | Any 4xx or 5xx JSON (or empty) response | `getStatus`, `getCode`, `getMessage`, `getDetails`, `getBody` |
| `AuthenticationException` | 401 | |
| `PermissionException` | 403 | |
| `NotFoundException` | 404 | |
| `ConflictException` | 409 | |
| `ValidationException` | 422 | `getErrors` (field errors) |
| `RateLimitException` | 429 | `getRetryAfter` (`Duration`) |
| `ServerException` | 5xx | |
| `TimeoutException` | No complete response within the timeout | `getTimeout` |
| `ConnectionException` | The request failed (DNS, TLS, refused, reset) | `getCause` |
| `UnexpectedResponseException` | An empty or non-JSON body where JSON was expected, or an error page such as a proxy's HTML 502 | `getStatus`, `getBodyExcerpt` |
| `RedirectException` | A 3xx response. Redirects are never followed, so tokens never go elsewhere. | `getStatus` |
| `LettermintConfigException` | A missing or unrecognised token, an invalid option or ID | |
| `LettermintValidationException` | The SDK rejected the request before sending it, such as invalid tags | `getField` |
| `WebhookVerificationException` | A webhook delivery is not genuine | `getReason` |

The subclasses of `ApiException` extend it. `getCode` and `getMessage` come from the API's error body (`{"error": {"code", "message", "details"}}` or `{"message", "errors"}`). All exceptions are in `co.lettermint.exceptions`; note that `co.lettermint.exceptions.TimeoutException` is not `java.util.concurrent.TimeoutException`.

```java
try {
    lettermint.emails().send(message, SendOptions.idempotencyKey(key));
} catch (ValidationException error) {
    System.err.println(error.getMessage() + " " + error.getErrors());
} catch (RateLimitException error) {
    // Wait error.getRetryAfter(), then retry with the same idempotency key.
} catch (TimeoutException error) {
    // The outcome is unknown. Retry with the same idempotency key.
} catch (ApiException error) {
    System.err.println(error.getStatus() + " " + error.getCode() + " " + error.getMessage());
}
```

Exceptions never contain request headers or tokens, and `toString()` of the client, its builder and its sub-clients shows tokens as `[redacted]`. Records that carry credentials, such as `ProjectCreatedData.apiToken()` or `WebhookSecretData.secret()`, also print them as `[redacted]`.

## Webhooks

Verify each webhook delivery before you trust it. Use the webhook's signing secret (`whsec_…`), not an API token, and pass the **raw** request body: the signature covers the exact bytes, so parsing and re-serializing the JSON breaks it.

```java
import co.lettermint.Webhook;
import co.lettermint.WebhookPayload;
import co.lettermint.exceptions.WebhookVerificationException;

Webhook webhook = new Webhook(System.getenv("LETTERMINT_WEBHOOK_SECRET"));

WebhookPayload event = webhook.verify(rawBody, headers);
System.out.println(event.event() + " " + event.data());
```

`verify(rawBody, headers)` takes the body as `byte[]` or `String`, and the headers as a `Map<String, String>`, a `Map<String, List<String>>` (Spring's `HttpHeaders`, JAX-RS `MultivaluedMap`) or a lookup function such as a servlet's `request::getHeader`. It requires `X-Lettermint-Signature` and `X-Lettermint-Delivery` (header names are case-insensitive), checks the HMAC-SHA256 signature in constant time, checks that the delivery timestamp equals the signed one and is within the tolerance, and returns the payload. Otherwise it throws `WebhookVerificationException` with a `getReason()`.

### Spring Boot

```java
@RestController
class LettermintWebhookController {
    private final Webhook webhook = new Webhook(System.getenv("LETTERMINT_WEBHOOK_SECRET"));

    @PostMapping("/webhooks/lettermint")
    ResponseEntity<Void> receive(@RequestBody byte[] body, @RequestHeader HttpHeaders headers) {
        try {
            WebhookPayload event = webhook.verify(body, headers);
            // Handle event.event() and event.data() here.
            return ResponseEntity.noContent().build();
        } catch (WebhookVerificationException error) {
            return ResponseEntity.badRequest().build();
        }
    }
}
```

### Servlets

```java
byte[] body = request.getInputStream().readAllBytes();
WebhookPayload event = webhook.verify(body, request::getHeader);
```

### Options and lower-level verification

The default tolerance is 300 seconds in either direction. Change it with `new Webhook(secret, Duration.ofSeconds(60))`. `Duration.ZERO` accepts only the current second; it does not disable the check. A third argument takes a `java.time.Clock` for tests. A valid signature does not prevent a repeated delivery within the tolerance, so track `event.id()` if you must not process an event twice.

If the headers are not at hand, call `webhook.verifySignature(rawBody, signatureHeader, deliveryHeader)`.

`WebhookPayload` has `id()`, `event()` (a `WebhookEvent` such as `WebhookEvent.MESSAGE_DELIVERED`; unknown events keep their raw name), `timestamp()`, `data()` and `fields()` with every top-level field. `data(MyRecord.class)` converts the data with Jackson.

## Types

Request and response types are generated from the Lettermint API specification into `co.lettermint.types`, for example `SendMailRequest`, `SendMailResponse`, `DomainData` and `CursorPage<DomainListData>`. They are records: read fields with accessors such as `domain.id()`. Unknown JSON fields are ignored.

Enums are open: `MessageStatus`, `DomainStatus`, `WebhookEvent` and the others are classes with a constant per known value. A value that the API adds later decodes without an error and keeps its raw string: `status.value()` returns it and `status.isKnown()` tells whether this version knows it. Compare with `equals` (`MessageStatus.DELIVERED.equals(status)`) or switch on `status.value()`.

`co.lettermint.types.Operations` describes every API operation (path, token, request and response types).

## Requirements

- Java 17 or later (tested on 17, 21 and 25).
- Jackson Databind 2.x, the only dependency.
- The jar has the automatic module name `co.lettermint`.
- The `User-Agent` header is `lettermint-java/<version>`.
- Use API tokens on servers only, never in apps that you ship to users.

## Development

```bash
./gradlew build    # compile, test, javadoc and the generated-code check
./gradlew test
```

`src/main/java/co/lettermint/types/` is generated by the private [SDK generator](https://github.com/lettermint/sdk-generator). Do not edit it by hand. With a checkout of the generator, `./gradlew generateTypes` regenerates it and `./gradlew checkGeneratedTypes` verifies it; set `LETTERMINT_SDK_GENERATOR` to the checkout (default `../sdk-generator`). Without the generator, as in CI, `checkGeneratedTypes` (part of `build`) only verifies the generated headers.

## License

MIT License - see [LICENSE](LICENSE) for details.
