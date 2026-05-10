# Upgrade To v2

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
