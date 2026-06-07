# Lettermint Java SDK

![Maven Central Version](https://img.shields.io/maven-central/v/co.lettermint/lettermint)
![Build Status](https://img.shields.io/github/actions/workflow/status/lettermint/lettermint-java/release.yml?branch=main)
[![Join our Discord server](https://img.shields.io/discord/1305510095588819035?logo=discord&logoColor=eee&label=Discord&labelColor=464ce5&color=0D0E28&cacheSeconds=43200)](https://lettermint.co/r/discord)

The official Java SDK for [Lettermint](https://lettermint.co).

## Requirements

- Java 8 or higher

## Installation

### Maven

```xml
<dependency>
    <groupId>co.lettermint</groupId>
    <artifactId>lettermint</artifactId>
    <version>2.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'co.lettermint:lettermint:2.0.0'
```

## Quick Start

```java
import co.lettermint.Lettermint;
import co.lettermint.endpoints.EmailEndpoint;
import co.lettermint.models.SendEmailResponse;

EmailEndpoint email = Lettermint.email("your-sending-token");

SendEmailResponse response = email
    .from("sender@example.com")
    .to("recipient@example.com")
    .subject("Hello from Lettermint")
    .html("<p>Hello World!</p>")
    .send();

System.out.println("Message ID: " + response.getMessageId());
```

## Usage

### Sending Emails

Use a project sending token with `Lettermint.email(...)`. Sending tokens authenticate with the `x-lettermint-token` header.

The SDK provides a fluent builder interface for composing emails:

```java
import co.lettermint.Lettermint;
import co.lettermint.endpoints.EmailEndpoint;
import co.lettermint.models.SendEmailResponse;

import java.util.HashMap;
import java.util.Map;

EmailEndpoint email = Lettermint.email("your-sending-token");

Map<String, String> headers = new HashMap<>();
headers.put("X-Custom-Header", "value");

Map<String, Object> metadata = new HashMap<>();
metadata.put("userId", "123");
metadata.put("campaign", "welcome");

SendEmailResponse response = email
    // Sender
    .from("John Doe <sender@example.com>")

    // Recipients (varargs)
    .to("recipient1@example.com", "recipient2@example.com")
    .cc("cc@example.com")
    .bcc("bcc@example.com")
    .replyTo("reply@example.com")

    // Content
    .subject("Welcome!")
    .html("<p>Hello <b>World</b></p>")
    .text("Hello World")

    // Custom headers
    .headers(headers)
    // Or add headers individually
    .header("X-Another-Header", "value")

    // Attachments
    .attach("document.pdf", base64EncodedContent)
    .attach("logo.png", base64EncodedContent, "logo")  // Inline with content ID

    // Routing
    .route("route-id")

    // Metadata and tags
    .metadata(metadata)
    .tag("welcome", "onboarding")

    // Idempotency
    .idempotencyKey("unique-request-key")

    .send();
```

Existing constructor-based sending usage still works:

```java
Lettermint lettermint = new Lettermint("your-sending-token");
lettermint.email().from("sender@example.com").to("recipient@example.com").subject("Hello").send();
```

### Batch Sending

```java
import co.lettermint.models.api.SendMailRequest;
import co.lettermint.models.api.SendMailResponse;

import java.util.Collections;
import java.util.List;

SendMailRequest message = new SendMailRequest();
message.fromValue = "sender@example.com";
message.to = Collections.singletonList("recipient@example.com");
message.subject = "Hello from Lettermint";
message.text = "This is a batch email.";

List<SendMailResponse> response = Lettermint.email("your-sending-token")
    .sendBatch(Collections.singletonList(message));
```

Both sending and API clients support `ping()`:

```java
String pong = Lettermint.email("your-sending-token").ping();
```

### Team API

Use a team API token with `Lettermint.api(...)`. API tokens authenticate with `Authorization: Bearer ...` and are separate from project sending tokens.

```java
import co.lettermint.Lettermint;
import co.lettermint.api.ApiClient;
import co.lettermint.models.api.DomainIndexResponse;
import co.lettermint.models.api.TeamData;

import java.util.Collections;
import java.util.Map;

ApiClient api = Lettermint.api("your-api-token");

Map<String, String> query = Collections.singletonMap("page[size]", "10");

DomainIndexResponse domains = api.domains().list(query);
TeamData team = api.team().retrieve();
String messageHtml = api.messages().html("message-id");
String pong = api.ping();
```

Endpoint groups are available as `domains()`, `messages()`, `projects()`, `routes()`, `stats()`, `suppressions()`, `team()`, and `webhooks()`.

### Webhook Verification

Verify webhook signatures to ensure requests are from Lettermint:

```java
import co.lettermint.webhooks.Webhook;
import co.lettermint.exceptions.webhook.WebhookVerificationException;

import java.util.Map;

String rawPayload = "..."; // Raw JSON body from request
String signature = "...";  // Value of X-Lettermint-Signature header
String secret = "whsec_..."; // Your webhook signing secret

try {
    Map<String, Object> payload = Webhook.verify(rawPayload, signature, secret);

    String event = (String) payload.get("event");
    Map<String, Object> data = (Map<String, Object>) payload.get("data");

    // Handle the webhook event
} catch (WebhookVerificationException e) {
    // Invalid signature
}
```

With custom timestamp tolerance (in seconds):

```java
// Allow signatures up to 10 minutes old
Map<String, Object> payload = Webhook.verify(rawPayload, signature, secret, 600);

// Disable timestamp checking
Map<String, Object> payload = Webhook.verify(rawPayload, signature, secret, 0);
```

## Exception Handling

The SDK uses unchecked exceptions that extend `RuntimeException`:

```java
import co.lettermint.exceptions.*;

try {
    lettermint.email()
        .from("sender@example.com")
        .to("recipient@example.com")
        .subject("Test")
        .send();
} catch (ValidationException e) {
    // HTTP 422 - validation errors
    System.err.println("Validation failed: " + e.getMessage());
    System.err.println("Response: " + e.getResponseBody());
} catch (HttpRequestException e) {
    // Other HTTP errors
    System.err.println("HTTP " + e.getStatusCode() + ": " + e.getMessage());
} catch (LettermintException e) {
    // Other SDK errors (including timeouts)
    System.err.println("Error: " + e.getMessage());
}
```

### Webhook Exceptions

```java
import co.lettermint.exceptions.webhook.*;

try {
    Webhook.verify(payload, signature, secret);
} catch (InvalidSignatureException e) {
    // Signature doesn't match
} catch (TimestampToleranceException e) {
    // Timestamp too old
    System.err.println("Timestamp: " + e.getTimestamp());
    System.err.println("Tolerance: " + e.getTolerance());
} catch (WebhookVerificationException e) {
    // Other verification errors
}
```

## Building

### Maven

```bash
mvn clean install
```

### Gradle

```bash
./gradlew build
```

## Testing

### Maven

```bash
mvn test
```

### Gradle

```bash
./gradlew test
```

## License

MIT License - see [LICENSE](LICENSE) for details.
