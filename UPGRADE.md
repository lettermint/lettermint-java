# Upgrade guide

- [Upgrade from 2.x to 3.0](#upgrade-from-2x-to-30)
- [Upgrade from 1.x to 2.0](#upgrade-from-1x-to-20)

# Upgrade from 2.x to 3.0

2.x no longer receives updates, including fixes. Upgrade to 3.0 to keep getting them.

3.0 is a new major version. The main reason is safety: in 2.x, `Lettermint.email(token)` returned one mutable, non-thread-safe `EmailEndpoint`, and the README suggested keeping it as the client. Two emails composed at the same time on it, for example in two web requests, could mix recipients, content and `Idempotency-Key`, and an email abandoned halfway (for example because `tags()` threw) leaked into the next send. 2.x also followed redirects with the sending token, could silently re-send `POST /send` after a connection failure, and treated a webhook tolerance of `0` as "no check". 3.0 stores nothing about a message on the client and fixes the rest.

## Highlights

- One thread-safe client: `Lettermint.builder().sendingToken(...).teamToken(...).build()`, or `Lettermint.of(token)`. It replaces `Lettermint.email()`, `Lettermint.api()`, `new Lettermint(token)`, `ApiClient` and `LettermintClient`.
- Sending is stateless: `emails().send(message, options)`, `emails().sendBatch(messages, options)` and an immutable `emails().compose()` builder. The `Idempotency-Key` is a per-call option.
- Each part uses its own token: `emails()` uses the sending token, the Team API uses the team token. The SDK never falls back to the other token.
- Typed exceptions for every outcome, including empty or HTML responses, redirects, timeouts and network failures.
- Redirects are never followed, and nothing is retried.
- A configurable timeout that covers the whole request, an injectable `java.net.http.HttpClient`, and cancellation by interrupting the thread.
- Tokens never appear in `toString()` or exception messages.
- Webhook verification is an object, `new Webhook(secret)`, and requires both signature headers.
- Typed query records and `iterate()` methods that follow `next_cursor`.
- Types are immutable records generated from the current API specification and use its names (see [Type names](#type-names)). Enums are open.
- Java 17 or later. OkHttp is no longer a dependency.

## Requirements

- **Java 17 or later** (2.x: Java 8). Java 8 and 11 are past their mainstream support, and 3.0 relies on Java 17: records for the generated types, and the JDK's `java.net.http` client, which since Java 16 aborts a request when its future is cancelled, so timeouts and thread interrupts really stop the request.
- The only dependency is Jackson Databind 2.x. OkHttp and its Kotlin standard library are gone; if your code used OkHttp only through this SDK, you can drop it.

## Upgrade with a coding agent

You can let a coding agent (Claude Code, Codex, Cursor, Copilot, …) do the upgrade. Copy this instruction into the agent from your project's root, then review its changes:

````text
Upgrade this project from the Lettermint Java SDK 2.x (co.lettermint:lettermint) to 3.0.

1. Change the dependency to co.lettermint:lettermint:3.0.0 in pom.xml, build.gradle(.kts) or the version catalog. 3.0 needs Java 17 or newer: check the compiler release/target, toolchains, CI workflows and Dockerfiles, and report anything older.
2. Read the upgrade guide before changing code: https://github.com/lettermint/lettermint-java/blob/main/UPGRADE.md (the sources jar, co.lettermint:lettermint:3.0.0:sources, has the Javadoc of every class). Treat the guide as the source of truth and don't guess APIs.
3. Find every use of the SDK: imports from co.lettermint, Lettermint.email(, Lettermint.api(, new Lettermint(, getClient(, EmailEndpoint, ApiClient, LettermintClient, .idempotencyKey(, .attach(, .sendBatch(, Webhook.verify(, HttpRequestException, ValidationException, InvalidSignatureException, TimestampToleranceException, co.lettermint.models, and the 2.x type names from the guide's type-name table.
4. Rewrite each use following the guide's before/after examples:
   - Create one Lettermint client with Lettermint.builder().sendingToken(...), adding .teamToken(...) only where the Team API is used, and share it (for example as one Spring bean). Keep the project's existing environment variable or property names.
   - Replace fluent EmailEndpoint chains with lettermint.emails().send(EmailMessage.create()...) or lettermint.emails().compose()...send(). Builders are immutable: assign the result of every setter. Never keep a half-built email in a field.
   - Move idempotency keys into SendOptions.idempotencyKey(...) on send()/sendBatch(). Attachments become EmailAttachment.ofBase64(filename, base64) or EmailAttachment.of(filename, bytes), with .contentType(...) and .contentId(...).
   - Team API: use the same client (lettermint.domains(), ...), typed query records instead of Map<String, String> with bracket keys, record accessors (domain.id()) instead of public fields (domain.id), builders for request bodies, and the renamed methods from the guide.
   - Enum constants are now objects, not strings: compare with equals() or use .value(); constant names gained underscores (SOFTBOUNCED -> SOFT_BOUNCED).
   - Exceptions: switch to the 3.0 classes in co.lettermint.exceptions (ApiException and its subclasses, TimeoutException, ...). Accessors are getStatus(), getBody(), getCode().
   - Webhooks: new Webhook(secret).verify(rawBody, headers). Keep passing the raw request body, keep the secret's whsec_ prefix, and make sure the X-Lettermint-Signature and X-Lettermint-Delivery headers reach the handler.
   - Rename types using the guide's type-name table; co.lettermint.models.api becomes co.lettermint.types.
5. Compile and run the tests, and fix every error. Don't send real email or call the live API while testing.
6. Finish with a summary: the files you changed, anything you could not migrate with certainty, and behaviour changes I should review.

Never print, log or commit API tokens or webhook secrets.
````

## Create the client

`Lettermint.email(...)`, `Lettermint.api(...)`, `new Lettermint(token[, baseUrl])`, `lettermint.email()`, `getClient()`, `ApiClient`, `LettermintClient` and `Endpoint` are removed.

```java
// 2.x
EmailEndpoint email = Lettermint.email(System.getenv("LETTERMINT_PROJECT_TOKEN"));
ApiClient api = Lettermint.api(System.getenv("LETTERMINT_API_TOKEN"), "https://api.lettermint.co/v1");
Lettermint legacy = new Lettermint(System.getenv("LETTERMINT_PROJECT_TOKEN"));

// 3.0
Lettermint lettermint = Lettermint.builder()
    .sendingToken(System.getenv("LETTERMINT_PROJECT_TOKEN")) // for lettermint.emails()
    .teamToken(System.getenv("LETTERMINT_TEAM_TOKEN"))       // for the Team API
    .baseUrl("https://api.lettermint.co/v1")                 // optional
    .timeout(Duration.ofSeconds(10))                         // new; default 30 seconds
    .build();
```

Pass one token or both. With only one, calling a part that needs the other throws `LettermintConfigException` (for example `domains.list needs teamToken; …`) before any request.

You can also pass a token string; the SDK chooses its type by the prefix:

```java
Lettermint lettermint = Lettermint.of("lm_team_...");                                         // team token
Lettermint lettermint = Lettermint.of("lm_...");                                              // project sending token
Lettermint lettermint = Lettermint.builder().token(token).timeout(Duration.ofSeconds(10)).build(); // with options
```

Any other format (SSO tokens, OAuth tokens, an empty string) throws `LettermintConfigException`; use `sendingToken(...)` or `teamToken(...)` for those.

New options: `timeout(Duration)` (2.x had a fixed 30 seconds per connect, read and write phase; 3.0 limits the whole request) and `httpClient(HttpClient)` for a proxy, TLS settings or an executor. The client must not follow redirects. 2.x created a new OkHttp client for every `Lettermint.email(...)`/`Lettermint.api(...)` call; create the 3.0 client once and share it.

## Send an email

The 2.x `EmailEndpoint` was the client and a mutable builder at once, and was reset after each send. In 3.0, emails are immutable values: `EmailMessage`, or the `EmailBuilder` that `emails().compose()` returns. Each setter returns a new object and leaves the old one unchanged. Chaining works as before; if you built an email over several statements, assign the result of each setter.

```java
// 2.x
EmailEndpoint email = Lettermint.email(token);
SendEmailResponse response = email
    .from("Acme <hello@acme.com>")
    .to("jane@example.com")
    .subject("Welcome")
    .html("<p>Hi Jane</p>")
    .idempotencyKey("welcome-jane")
    .send();
String id = response.getMessageId();

// 3.0: builder
SendMailResponse response = lettermint.emails().compose()
    .from("Acme <hello@acme.com>")
    .to("jane@example.com")
    .subject("Welcome")
    .html("<p>Hi Jane</p>")
    .send(SendOptions.idempotencyKey("welcome-jane"));
String id = response.messageId();

// 3.0: message value
EmailMessage message = EmailMessage.create().from("Acme <hello@acme.com>").to("jane@example.com").subject("Welcome").html("<p>Hi Jane</p>");
lettermint.emails().send(message, SendOptions.idempotencyKey("welcome-jane"));
```

```java
// 2.x: statements changed the shared endpoint
email.from("hello@acme.com");
email.to("jane@example.com");
if (copy) email.cc("team@acme.com");
email.subject("Hi").send();

// 3.0: keep the returned builder
EmailBuilder draft = lettermint.emails().compose().from("hello@acme.com").to("jane@example.com");
if (copy) draft = draft.cc("team@acme.com");
draft.subject("Hi").send();
```

A base builder can now be shared safely, also between threads:

```java
EmailBuilder welcome = lettermint.emails().compose().from("Acme <hello@acme.com>").subject("Welcome");
welcome.to("jane@example.com").html(janeHtml).send();
welcome.to("john@example.com").html(johnHtml).send(SendOptions.idempotencyKey("welcome-john"));
```

### Changed builder methods

| 2.x (`EmailEndpoint`) | 3.0 (`EmailBuilder` and `EmailMessage`) |
| --- | --- |
| Setters change the endpoint and return it | Return a new builder or message |
| `.idempotencyKey(key).send()` | `.send(SendOptions.idempotencyKey(key))` |
| `.send()` returns `SendEmailResponse` (`getMessageId()`, `getStatus()`) | `.send()` returns `SendMailResponse` (`messageId()`, `status()` as `MessageStatus`, `sandbox()`, `sandboxResult()`, `scheduledAt()`) |
| `.attach(filename, base64)`, `.attach(filename, base64, contentId)`, `.attach(filename, base64, contentId, contentType)` | `.attach(EmailAttachment.ofBase64(filename, base64).contentId(id).contentType(type))`, or `EmailAttachment.of(filename, bytes)`, which base64-encodes for you |
| `.header(name, value)` | Removed; pass every header to `.headers(map)` |
| `.metadata(Map<String, Object>)`, `.metadata(key, value)` | `.metadata(Map<String, String>)` (the API stores strings) |
| `.tags(MessageTag...)` with `co.lettermint.models.MessageTag`, `.tags(Map<String, String>...)` | `.tags(MessageTagInput...)` or `.tags(Collection<MessageTagInput>)` with `co.lettermint.types.MessageTagInput` |
| `.settings(Map<String, Object>)` | `.settings(SendMailRequestSettings)` |
| `.sandboxResult(String)` | `.sandboxResult(SandboxResult)` (`SandboxResult.of("…")` for any value) |
| `.scheduledAt(String)` | `.scheduledAt(String)` or `.scheduledAt(Instant)` |
| `.html(null)`, `.text(null)` | `null` removes the field; so do `tag(null)`, `route(null)` and the other single-value setters |
| Invalid tags threw `IllegalArgumentException` | Throw `LettermintValidationException` (with `getField()`); the builder is unchanged |
| — | `builder.build()` returns the `EmailMessage`; `message.toRequest()` the wire format |

Unchanged setters: `from`, `to`, `cc`, `bcc`, `replyTo` (each replaces its list), `subject`, `html`, `text`, `headers`, `route`, `tag`.

## Batch sending and ping

```java
// 2.x
SendMailRequest request = new SendMailRequest();
request.fromValue = "hello@acme.com";
request.to = List.of("jane@example.com");
request.subject = "Hi";
Lettermint.email(token).idempotencyKey("batch-1").sendBatch(List.of(request));
Lettermint.email(token).ping();
Lettermint.api(token).ping();

// 3.0
lettermint.emails().sendBatch(List.of(
    EmailMessage.create().from("hello@acme.com").to("jane@example.com").subject("Hi"),
    builder.build()), SendOptions.idempotencyKey("batch-1"));
lettermint.emails().ping(); // sending token
lettermint.ping();          // team token if configured, otherwise the sending token
```

## Team API

The endpoint groups move from `Lettermint.api(token).x()` to `lettermint.x()`. Query parameters are typed records instead of `Map<String, String>` with bracket keys, and every list has an `iterate()` method that follows `next_cursor`.

```java
// 2.x
ApiClient api = Lettermint.api(token);
DomainIndexResponse page = api.domains().list(Map.of("page[size]", "10", "filter[status]", "verified"));
for (DomainListData domain : page.data) { System.out.println(domain.domain); }

// 3.0
CursorPage<DomainListData> page = lettermint.domains().list(ListDomainsQuery.builder().pageSize(10).filterStatus(DomainStatus.VERIFIED).build());
for (DomainListData domain : lettermint.domains().iterate(ListDomainsQuery.builder().filterStatus(DomainStatus.VERIFIED).build())) {
    System.out.println(domain.domain());
}
```

| 2.x (`api = Lettermint.api(token)`) | 3.0 (`lettermint = Lettermint.builder().teamToken(token).build()`) |
| --- | --- |
| `api.ping()` | `lettermint.ping()` |
| `api.blockedFileTypes()` | `lettermint.blockedFileTypes()` |
| `api.analytics(AnalyticsRequest)` | `lettermint.analytics(AnalyticsQuery)` |
| `api.domains().list(map)` | `lettermint.domains().list(ListDomainsQuery)`, `lettermint.domains().iterate(query)` |
| `api.domains().create(payload)` | `lettermint.domains().create(payload)` |
| `api.domains().retrieve(id)` | `lettermint.domains().retrieve(id)` or `retrieve(id, GetDomainQuery)` (`include`: `GetDomainQueryIncludeItem.DNS_RECORDS`) |
| `api.domains().delete(id)` | `lettermint.domains().delete(id)` |
| `api.domains().verifyDnsRecords(id)` | `lettermint.domains().verifyDnsRecords(id)` |
| `api.domains().verifyDnsRecord(id, recordId)` | `lettermint.domains().verifyDnsRecord(id, recordId)` |
| `api.domains().updateProjects(id, payload)` | `lettermint.domains().updateProjects(id, payload)` |
| `api.messages().list(map)` | `lettermint.messages().list(ListMessagesQuery)`, `lettermint.messages().iterate(query)` |
| `api.messages().retrieve(id)` | `lettermint.messages().retrieve(id)` |
| `api.messages().events(id[, map])` | `lettermint.messages().events(id[, ListMessageEventsQuery])`, `lettermint.messages().iterateEvents(id[, query])` |
| `api.messages().source(id)` / `.html(id)` / `.text(id)` | unchanged, on `lettermint.messages()` |
| `api.messages().reschedule(id, payload)` | `lettermint.messages().reschedule(id, payload)`; accepts either token |
| `api.messages().cancel(id)` | `lettermint.messages().cancel(id)`; accepts either token |
| `api.messages().process(id)` | `lettermint.messages().process(id[, SendOptions.idempotencyKey(key)])` |
| `api.projects().list(map)` | `lettermint.projects().list(ListProjectsQuery)`, `lettermint.projects().iterate(query)` |
| `api.projects().create(payload)` | `lettermint.projects().create(payload)` |
| `api.projects().retrieve(id)` | `lettermint.projects().retrieve(id[, GetProjectQuery])` |
| `api.projects().update(id, payload)` | `lettermint.projects().update(id, payload)` |
| `api.projects().delete(id)` | `lettermint.projects().delete(id)` |
| `api.projects().rotateToken(id)` | `lettermint.projects().rotateToken(id)` (deprecated by the API) |
| `api.projects().routes(projectId[, map])` | `lettermint.routes().list(projectId[, ListRoutesQuery])`, `lettermint.routes().iterate(projectId[, query])` |
| `api.projects().createRoute(projectId, payload)` | `lettermint.routes().create(projectId, payload)` |
| `api.projects().retrieveReportForwarding(id)` | `lettermint.projects().reportForwarding().retrieve(id)` |
| `api.projects().updateReportForwarding(id, payload)` | `lettermint.projects().reportForwarding().update(id, payload)` |
| `api.projects().deleteReportForwarding(id)` | `lettermint.projects().reportForwarding().delete(id)` |
| `api.projects().verifyReportForwarding(id, payload)` | `lettermint.projects().reportForwarding().verify(id, payload)` |
| `api.projects().resendReportForwardingCode(id)` | `lettermint.projects().reportForwarding().resendCode(id)` |
| `api.routes().retrieve(id)` | `lettermint.routes().retrieve(id[, GetRouteQuery])` |
| `api.routes().update(id, payload)` | `lettermint.routes().update(id, payload)` |
| `api.routes().delete(id)` | `lettermint.routes().delete(id)` |
| `api.routes().verifyInboundDomain(id)` | `lettermint.routes().verifyInboundDomain(id)` |
| `api.stats().retrieve(map)` | `lettermint.stats().retrieve(GetStatsQuery.builder().from(...).to(...).build())` |
| `api.suppressions().list(map)` | `lettermint.suppressions().list(ListSuppressionsQuery)`, `lettermint.suppressions().iterate(query)` |
| `api.suppressions().create(payload)` | `lettermint.suppressions().create(payload)` |
| `api.suppressions().delete(id)` | `lettermint.suppressions().delete(id)` |
| `api.team().retrieve()` | `lettermint.team().retrieve([GetTeamQuery])` |
| `api.team().update(payload)` | `lettermint.team().update(payload)` |
| `api.team().usage([map])` | `lettermint.team().usage()` (the endpoint takes no parameters) |
| `api.team().roles()` | `lettermint.team().roles()` |
| `api.team().members([map])` | `lettermint.team().members().list([ListTeamMembersQuery])`, `lettermint.team().members().iterate([query])` |
| `api.team().member(userId)` | `lettermint.team().members().retrieve(userId)` |
| `api.team().updateMemberAssignment(userId, payload)` | `lettermint.team().members().updateAssignment(userId, payload)` |
| `api.webhooks().list(map)` | `lettermint.webhooks().list(ListWebhooksQuery)`, `lettermint.webhooks().iterate(query)` |
| `api.webhooks().create(payload)` | `lettermint.webhooks().create(payload)` |
| `api.webhooks().retrieve(id)` | `lettermint.webhooks().retrieve(id)` |
| `api.webhooks().update(id, payload)` | `lettermint.webhooks().update(id, payload)` |
| `api.webhooks().delete(id)` | `lettermint.webhooks().delete(id)` |
| `api.webhooks().test(id)` | `lettermint.webhooks().test(id)` |
| `api.webhooks().regenerateSecret(id)` | `lettermint.webhooks().regenerateSecret(id)` |
| `api.webhooks().deliveries(id[, map])` | `lettermint.webhooks().deliveries().list(id[, ListWebhookDeliveriesQuery])`, `lettermint.webhooks().deliveries().iterate(id[, query])` |
| `api.webhooks().delivery(id, deliveryId)` | `lettermint.webhooks().deliveries().retrieve(id, deliveryId)` |

Every method also has an overload with a last `RequestOptions` argument (`RequestOptions.timeout(Duration)`); `messages().process()` takes `SendOptions`.

### Query parameters

Query records have one builder method per parameter, named after the wire name in camel case. Arrays of values are joined with commas, arrays of objects are indexed, and booleans are sent as `1`/`0`.

| 2.x | 3.0 |
| --- | --- |
| `Map.of("page[size]", "30", "page[cursor]", c)` | `ListDomainsQuery.builder().pageSize(30).pageCursor(c).build()` |
| `Map.of("filter[status]", "verified")` | `.filterStatus(DomainStatus.VERIFIED)` |
| `Map.of("sort", "-created_at,domain")` | `.sort(List.of(ListDomainsQuerySortItem.CREATED_AT_DESC, ListDomainsQuerySortItem.DOMAIN))` |
| `Map.of("filter[enabled]", "true")` | `ListWebhooksQuery.builder().filterEnabled(true)` |
| `Map.of("filter[tags][0][name]", "a", "filter[tags][0][value]", "b")` | `ListMessagesQuery.builder().filter(ListMessagesQueryFilter.builder().tags(List.of(ListMessagesQueryFilterTagsItem.builder().name("a").value("b").build())).build())` |
| webhooks: `Map.of("cursor", c)` | unchanged wire name: `ListWebhooksQuery.builder().cursor(c)` (these lists use `cursor`, not `page[cursor]`) |

### Path parameters

IDs are still URL-encoded. An empty or null ID, `.` or `..` now throws `LettermintConfigException` before the request.

### Message lists

2.x typed message and event lists with a `meta` map. The API returns a flat cursor page, which 3.0 types as `CursorPage<T>`: read `page.nextCursor()`, or use `iterate()`.

## Models

The generated types moved from `co.lettermint.models.api` to `co.lettermint.types` and became immutable records:

```java
// 2.x: public mutable fields
DomainData domain = api.domains().retrieve(id);
String name = domain.domain;
StoreWebhookData payload = new StoreWebhookData();
payload.name = "Hook";
payload.url = "https://acme.com/hook";
payload.events = List.of(WebhookEvent.MESSAGE_DELIVERED);

// 3.0: accessors and builders
String name = lettermint.domains().retrieve(id).domain();
StoreWebhookData payload = StoreWebhookData.builder()
    .name("Hook")
    .url("https://acme.com/hook")
    .events(List.of(WebhookEvent.MESSAGE_DELIVERED))
    .build();
```

- Field access becomes an accessor (`domain.domain` → `domain.domain()`; 2.x `fromValue` → `from()`). Requests are built with `X.builder()…build()`, `toBuilder()` or the record constructor.
- Lists and maps in records are unmodifiable.
- Integers are `Long` and numbers `Double` (builders also take `long` and `double`).
- **Enums are open objects, not strings.** In 2.x, `MessageStatus.DELIVERED` was the `String` `"delivered"` and fields were `String`. In 3.0 fields are typed (`MessageStatus status()`), constants are `MessageStatus` instances, and a value the API adds later decodes without an error: `status.value()` returns the raw string and `status.isKnown()` tells whether this version knows it. Compare with `MessageStatus.DELIVERED.equals(status)` or switch on `status.value()`; build a value with `MessageStatus.of("…")`.
- **Enum constant names gained underscores**: `SOFTBOUNCED` → `SOFT_BOUNCED`, `HARDBOUNCED` → `HARD_BOUNCED`, `SPAMCOMPLAINT` → `SPAM_COMPLAINT`, `POLICYREJECTED` → `POLICY_REJECTED`, `WebhookEvent.MESSAGEDELIVERED` → `MESSAGE_DELIVERED`, and so on. Sort values with a leading `-` end in `_DESC` (`-created_at` → `CREATED_AT_DESC`).
- `OptionalNullable` moved to `co.lettermint.types`: `OptionalNullable.nullValue()` → `ofNull()`, `getValue()` → `value()`. Request builders create it for you: an optional and nullable field that you leave unset is omitted, and `null` sends JSON `null`.

## Errors

`HttpRequestException` and the 2.x `ValidationException` are replaced. Every SDK exception still extends `co.lettermint.exceptions.LettermintException` (unchecked).

| Situation | 2.x | 3.0 |
| --- | --- | --- |
| HTTP 400 and other 4xx | `HttpRequestException` (`getStatusCode()`, `getResponseBody()`) | `ApiException` (`getStatus()`, `getCode()`, `getMessage()`, `getDetails()`, `getBody()`) |
| HTTP 401 | `HttpRequestException` | `AuthenticationException` |
| HTTP 403 | `HttpRequestException` | `PermissionException` |
| HTTP 404 | `HttpRequestException` | `NotFoundException` |
| HTTP 409 | `HttpRequestException` | `ConflictException` |
| HTTP 422 | `ValidationException` (`getResponseBody()` as a string) | `ValidationException` (`getErrors()` field errors, `getBody()`) |
| HTTP 429 | `HttpRequestException` | `RateLimitException` (`getRetryAfter()` as a `Duration`) |
| HTTP 5xx | `HttpRequestException` | `ServerException` |
| Empty or invalid JSON body, HTML error page | `LettermintException` ("Request failed: …") or `HttpRequestException` | `UnexpectedResponseException` (`getStatus()`, `getBodyExcerpt()`) |
| Redirect (3xx) | followed, also to another host, with the token | `RedirectException` (`getStatus()`); never followed |
| Timeout | `LettermintException` ("Request timed out") | `co.lettermint.exceptions.TimeoutException` (`getTimeout()`); covers the whole request |
| Network failure | `LettermintException` ("Request failed: …"); OkHttp could also re-send the request | `ConnectionException`; never retried |
| Invalid tags | `IllegalArgumentException` | `LettermintValidationException` (`getField()`) |
| Missing or wrong token, bad option | `IllegalArgumentException` | `LettermintConfigException` |

Property renames: `getStatusCode()` → `getStatus()`, `getResponseBody()` (a string) → `getBody()` (decoded JSON). `getCode()` is new and comes from `{"error": {"code"}}` or a string `error` field.

```java
// 2.x
try {
    email.send();
} catch (ValidationException e) {
    log.warn(e.getResponseBody());
} catch (HttpRequestException e) {
    if (e.getStatusCode() == 429) retryLater();
}

// 3.0
try {
    builder.send();
} catch (ValidationException e) {
    log.warn("{} {}", e.getMessage(), e.getErrors());
} catch (RateLimitException e) {
    retryLater(e.getRetryAfter());
}
```

The SDK does not retry requests. Pass an idempotency key when you retry a send. Interrupting the calling thread aborts the request and throws `java.util.concurrent.CancellationException`, not an SDK exception.

## Webhooks

The static `Webhook.verify(payload, signature, secret[, tolerance])` is replaced by a verifier object. `verify()` now takes the raw body and the request headers, requires both `X-Lettermint-Signature` and `X-Lettermint-Delivery`, and returns a `WebhookPayload` instead of `Map<String, Object>`.

```java
// 2.x
Map<String, Object> payload = Webhook.verify(rawPayload, signatureHeader, secret);
Map<String, Object> payload = Webhook.verify(rawPayload, signatureHeader, secret, 600);
String event = (String) payload.get("event");

// 3.0
Webhook webhook = new Webhook(secret);                           // create once
Webhook webhook = new Webhook(secret, Duration.ofSeconds(600));
WebhookPayload payload = webhook.verify(rawBody, headers);        // Map<String, String>, Map<String, List<String>>, or request::getHeader
WebhookPayload payload = webhook.verifySignature(rawBody, signatureHeader, deliveryHeader);
WebhookEvent event = payload.event();
Map<String, Object> all = payload.fields();
```

- The body may be a `String` or the raw `byte[]`.
- `X-Lettermint-Delivery` must be present and equal the signed timestamp. 2.x ignored it.
- Every `v1` signature in the header is checked, so key rotation works; 2.x used only the last one.
- A tolerance of `0` now accepts only the current second; 2.x skipped the timestamp check for `0` or less. A negative tolerance or an empty secret throws `LettermintConfigException` (2.x: `WebhookVerificationException` for an empty secret).
- `InvalidSignatureException` and `TimestampToleranceException` are removed. Every failure is a `co.lettermint.exceptions.WebhookVerificationException` (it moved out of `co.lettermint.exceptions.webhook`) with `getReason()`: `SIGNATURE_HEADER_MISSING`, `SIGNATURE_HEADER_MALFORMED`, `DELIVERY_HEADER_MISSING`, `DELIVERY_TIMESTAMP_MISMATCH`, `TIMESTAMP_OUT_OF_TOLERANCE`, `SIGNATURE_MISMATCH`, `BODY_INVALID` or `PAYLOAD_INVALID` (`reason.code()` gives the shared code, such as `signature_mismatch`).
- `Webhook` moved from `co.lettermint.webhooks` to `co.lettermint`.

## Type names

The types are generated from the API specification of lettermint#2582 and use its names. They moved from `co.lettermint.models.api` to `co.lettermint.types`. Some shapes also changed:

- Java has no type aliases, so list responses are `CursorPage<Item>` and `sendBatch()` returns `List<SendMailResponse>`.
- `SendMailResponse` has every field of a pending and a scheduled send; check `status()`. `PendingSendMailResponse` and `ScheduledSendMailResponse` describe the two cases.
- `MessageTag` (in `co.lettermint.types`) describes tags in responses; `MessageTagInput` describes tags you send.
- `SuppressionStoreResponse.message()` is a `String` (2.x: `Object`).

These classes keep their name: `AnalyticsResponse`, `AttachmentDelivery`, `BuiltInTeamRole`, `DeliveryMode`, `DkimMode`, `DnsRecordPurpose`, `DnsRecordStatus`, `DnsVerificationScope`, `DomainData`, `DomainDnsRecordData`, `DomainListData`, `DomainStatus`, `GetReportForwardingResponse`, `InitialRoutes`, `MessageAttachmentData`, `MessageData`, `MessageEventData`, `MessageEventType`, `MessageListData`, `MessageRecipientData`, `MessageStatsData`, `MessageStatus`, `MessageType`, `OptionalNullable`, `Plan`, `ProcessInboundMessageResponse`, `ProjectAccessScope`, `ProjectCreatedData`, `ProjectData`, `ProjectListData`, `RbacConflictCode`, `RbacPermission`, `RecordType`, `ReportForwardingRequest`, `ReportForwardingResource`, `RescheduleMessageRequest`, `ResendReportForwardingCodeResponse`, `RouteData`, `RouteListData`, `RouteStatisticData`, `RouteType`, `SandboxResult`, `SendMailRequest`, `SendMailResponse`, `SpamSymbol`, `StatsDailyData`, `StatsData`, `StatsInboundData`, `StatsTotalsData`, `StatsTypeData`, `StoreDomainData`, `StoreProjectData`, `StoreRouteData`, `StoreSuppressionData`, `StoreWebhookData`, `SuppressedRecipientData`, `SuppressionAppliesTo`, `SuppressionReason`, `SuppressionScope`, `SuppressionSourceMessageData`, `SuppressionStoreResponse`, `SuppressionType`, `TeamAddonData`, `TeamData`, `TeamMemberData`, `TeamMemberProjectAccessData`, `TeamRoleData`, `TeamType`, `TeamUsageDetailData`, `TeamUsagePeriodData`, `TlsPolicy`, `UpdateDomainProjectsData`, `UpdateProjectData`, `UpdateReportForwardingResponse`, `UpdateRouteData`, `UpdateRouteInboundSettingsData`, `UpdateRouteSettingsData`, `UpdateTeamData`, `UpdateTeamMemberAssignmentData`, `UpdateWebhookData`, `VerifyReportForwardingRequest`, `VerifyReportForwardingResponse`, `WebhookBasicAuthData`, `WebhookData`, `WebhookDeliveryData`, `WebhookDeliveryListData`, `WebhookDeliveryModeFilter`, `WebhookDeliveryStatus`, `WebhookEvent`, `WebhookListData`, `WebhookScope`, `WebhookSecretData`.

| 2.x (`co.lettermint.models.api`) | 3.0 (`co.lettermint.types`) |
| --- | --- |
| `AnalyticsRequest` | `AnalyticsQuery` |
| `AnalyticsRequestFiltersItem` | `AnalyticsFilter` |
| `AnalyticsRequestSort` | `AnalyticsSort` |
| `AnalyticsResponseMeta` | `AnalyticsMeta` |
| `AnalyticsResponseMetaComparison` | `AnalyticsMetaComparison` |
| `AnalyticsResponsePagination` | `AnalyticsPagination` |
| `AnalyticsResponsePayload` | `AnalyticsResults` |
| `AnalyticsResponsePayloadBreakdownItem` | `AnalyticsBreakdownRow` |
| `AnalyticsResponsePayloadSummary` | `AnalyticsSummary` |
| `AnalyticsResponsePayloadTimeSeriesItem, AnalyticsResponsePayloadBreakdownItemTrendItem` | `AnalyticsTimeSeriesPoint` |
| AnalyticsResponsePayload…Metrics, …PreviousMetrics (8 classes) | `AnalyticsMetricValues` |
| AnalyticsResponsePayload…Previous (4 classes) | `AnalyticsComparisonValues` |
| AnalyticsResponsePayload…RateBases, …PreviousRateBases (8 classes) | `AnalyticsRateBases` |
| AnalyticsResponsePayload…RateBases…Rate (56 classes, for example AnalyticsResponsePayloadSummaryRateBasesBounceRate) | `AnalyticsRateBase` |
| `BlockedFileTypesResponse` | `BlockedFileTypes` |
| `CancelScheduledMessageResponse` | `ScheduledMessage` |
| `CursorPaginator` | `CursorPage<T>` |
| `DomainDestroyResponse` | `MessageResponse` |
| `DomainIndexResponse` | `CursorPage<DomainListData>` |
| `DomainUpdateProjectsResponse` | `DomainMutationResponse` |
| `DomainVerifyDnsRecordsResponse` | `DnsVerificationSuccessResponse` |
| `DomainVerifySpecificDnsRecordResponse` | `MessageResponse` |
| `MessageEventsResponse` | `CursorPage<MessageEventData>` |
| `MessageIndexResponse` | `CursorPage<MessageListData>` |
| `ProjectDestroyResponse` | `MessageResponse` |
| `ProjectIndexResponse` | `CursorPage<ProjectListData>` |
| `ProjectRotateTokenResponse` | `RotateProjectTokenResponse` |
| `ProjectStoreResponse` | `ProjectCreatedData` |
| `ProjectUpdateResponse` | `ProjectMutationResponse` |
| `RescheduleMessageResponse` | `ScheduledMessage` |
| `RouteDestroyResponse` | `MessageResponse` |
| `RouteIndexResponse` | `CursorPage<RouteListData>` |
| `RouteStoreResponse` | `RouteMutationResponse` |
| `RouteUpdateResponse` | `RouteMutationResponse` |
| `RouteVerifyInboundDomainResponse` | `InboundDomainVerificationResponse` |
| `SuppressionDestroyResponse` | `DeleteSuppressionResponse` |
| `SuppressionIndexResponse` | `CursorPage<SuppressedRecipientData>` |
| `TeamMembersResponse` | `CursorPage<TeamMemberData>` |
| `TeamRolesResponse` | `TeamRoleListResponse` |
| `TeamUpdateResponse` | `TeamMutationResponse` |
| `UpdateReportForwardingRequest` | `ReportForwardingRequest` |
| `WebhookDeliveriesResponse` | `CursorPage<WebhookDeliveryListData>` |
| `WebhookDestroyResponse` | `MessageResponse` |
| `WebhookIndexResponse` | `CursorPage<WebhookListData>` |
| `WebhookRegenerateSecretResponse` | `WebhookSecretResponse` |
| `WebhookStoreResponse` | `WebhookSecretResponse` |
| `WebhookTestResponse` | `TestWebhookResponse` |
| `WebhookUpdateResponse` | `WebhookMutationResponse` |

| 2.x (`co.lettermint.models`) | 3.0 |
| --- | --- |
| `SendEmailResponse` | `co.lettermint.types.SendMailResponse` |
| `MessageTag` | `co.lettermint.types.MessageTagInput` |
| `Attachment` | `co.lettermint.EmailAttachment` |

### Removed types

lettermint#2582 removed these schemas from the API specification:

| 2.x | 3.0 |
| --- | --- |
| `AnalyticsResponseData` | Removed. Use `AnalyticsResponse` (`data()` is `AnalyticsResults`). |
| `StatsRequestData` | Removed. Use `GetStatsQuery`, the parameters of `stats().retrieve()`. |
| Message list `meta` (`MessageIndexResponse.meta`, `MessageEventsResponse.meta`) | Removed; 2.x had no class for it, only a `Map` field. Lists are flat `CursorPage<T>`. |
| `SuppressionStoreResponseMessage1` | Removed; not exported by 2.x. `SuppressionStoreResponse.message()` is a `String`. |

### Removed classes and helpers

| 2.x | 3.0 |
| --- | --- |
| `co.lettermint.Lettermint`: `new Lettermint(token[, baseUrl])`, `email()`, `getClient()`, static `email(token[, baseUrl])`, static `api(token[, baseUrl])` | `co.lettermint.Lettermint`: `Lettermint.builder()…build()`, `Lettermint.of(token)`, `emails()`, `domains()`, … |
| `co.lettermint.api.ApiClient` and its nested `DomainsEndpoint`, `MessagesEndpoint`, `ProjectsEndpoint`, `RoutesEndpoint`, `StatsEndpoint`, `SuppressionsEndpoint`, `TeamEndpoint`, `WebhooksEndpoint` | `Lettermint` with `Domains`, `Messages`, `Projects` (and `ReportForwarding`), `Routes`, `Stats`, `Suppressions`, `Team` (and `TeamMembers`), `Webhooks` (and `WebhookDeliveries`) |
| `co.lettermint.client.LettermintClient` (`get`, `post`, `put`, `patch`, `delete`, `getRaw`, `getObjectMapper`) and `LettermintClient.AuthMode` | Removed. Every documented endpoint has a method. |
| `co.lettermint.endpoints.EmailEndpoint` | `Emails` (`lettermint.emails()`) and `EmailBuilder` (`emails().compose()`), plus `EmailMessage` |
| `co.lettermint.endpoints.Endpoint` | Removed |
| `co.lettermint.BuildInfo` | No longer public; the version is in the jar manifest and the `User-Agent` |
| `co.lettermint.exceptions.HttpRequestException` | `ApiException` and its subclasses |
| `co.lettermint.exceptions.ValidationException` (2.x) | `ValidationException` (3.0, extends `ApiException`) |
| `co.lettermint.exceptions.webhook.WebhookVerificationException`, `InvalidSignatureException`, `TimestampToleranceException` | `co.lettermint.exceptions.WebhookVerificationException` with `getReason()` |
| `co.lettermint.webhooks.Webhook` (static `verify`) | `co.lettermint.Webhook` (`new Webhook(secret).verify(…)`) |
| `co.lettermint.models.Attachment`, `MessageTag`, `SendEmailResponse` | See the table above |

# Upgrade from 1.x to 2.0
This guide covers upgrading from the latest released v1 Java SDK to v2.

## Highlights

- Sending email is available through `Lettermint.email(token)`.
- The full Lettermint API is available through `Lettermint.api(token)`.
- Sending tokens use `x-lettermint-token`; full API tokens use `Authorization: Bearer`.
- `ping()` returns the raw trimmed `pong` response.
- API request and response model classes are generated from the OpenAPI specs.

## Sending

```java
String pong = Lettermint.email("sending-token").ping();
```

Existing `new Lettermint("token").email()` sending usage still works.

## Full API

```java
ApiClient api = Lettermint.api("api-token");
DomainIndexResponse domains = api.domains().list();
```

## Batch Sending

```java
SendMailRequest payload = new SendMailRequest();
payload.fromValue = "sender@example.com";
payload.to = Collections.singletonList("user@example.com");
payload.subject = "Hello";
payload.text = "Hi";

List<SendMailResponse> response = Lettermint.email(token).sendBatch(Collections.singletonList(payload));
```
