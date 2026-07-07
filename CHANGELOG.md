# [2.0.0](https://github.com/lettermint/lettermint-java/compare/v1.2.0...v2.0.0) (2026-05-11)

### Features

* add Team API endpoints support ([#21](https://github.com/lettermint/lettermint-java/issues/21)) ([9ca8009](https://github.com/lettermint/lettermint-java/commit/9ca8009d7ba9a17addf34702376ff67a1986d00f))
* release SDK v2 ([a7436d5](https://github.com/lettermint/lettermint-java/commit/a7436d5c8acf9c8a5b0a37e05c11761c487ade8b))

### BREAKING CHANGES

* SDK v2 changes client structure, response types, and token configuration.

# [1.2.0](https://github.com/lettermint/lettermint-java/compare/v1.1.1...v1.2.0) (2026-03-27)

### Bug Fixes

* downgrade okhttp for compatibility ([ad246c9](https://github.com/lettermint/lettermint-java/commit/ad246c9ff0f2e7fad9b370d450d1bde2efb6b958))

### Features

* add contentType parameter to Attachment and EmailEndpoint.attach method ([bfcffe4](https://github.com/lettermint/lettermint-java/commit/bfcffe4e38927a54608cff4d58ef366fda1adcc9))

## [v2.1.0](https://github.com/lettermint/lettermint-java/compare/v1.1.1...v2.1.0) - 2026-07-07

### What's Changed

* chore: update README for v2 API by @bjarn in https://github.com/lettermint/lettermint-java/pull/25
* ci(release): publish Java SDK from GitHub releases by @bjarn in https://github.com/lettermint/lettermint-java/pull/33
* feat: add route settings, webhook auto-reply events, and blocked file types by @bjarn in https://github.com/lettermint/lettermint-java/pull/32

**Full Changelog**: https://github.com/lettermint/lettermint-java/compare/v2.0.0...v2.1.0

## [1.1.1](https://github.com/lettermint/lettermint-java/compare/v1.1.0...v1.1.1) (2026-03-18)

### Bug Fixes

* use okhttp-jvm artifact to resolve ClassNotFoundException for Maven consumers ([10f0588](https://github.com/lettermint/lettermint-java/commit/10f058870ceaf64b2fca4d5e847864b2a53e72b5))

# [1.1.0](https://github.com/lettermint/lettermint-java/compare/v1.0.2...v1.1.0) (2026-02-27)

### Features

* support multiple reply-to addresses in EmailEndpoint ([2787307](https://github.com/lettermint/lettermint-java/commit/2787307d9d8c89216dc5914a0781929f4fd5a08f))

## [1.0.2](https://github.com/lettermint/lettermint-java/compare/v1.0.1...v1.0.2) (2026-02-25)

### Bug Fixes

* naming in docs ([608ff3b](https://github.com/lettermint/lettermint-java/commit/608ff3b402731f01c836cf281f6a04c11ca74a62))
* rename routeId parameter to route ([fae9ec6](https://github.com/lettermint/lettermint-java/commit/fae9ec6de53d4d21af1d7101eff934d1af042ca8))

## [1.0.1](https://github.com/lettermint/lettermint-java/compare/v1.0.0...v1.0.1) (2026-01-29)

### Bug Fixes

* update release process to include manual OSSRH upload ([64c27ef](https://github.com/lettermint/lettermint-java/commit/64c27ef7d75939f4c78577d9d7014756085c3edd))

# 1.0.0 (2026-01-29)

### Features

* initial commit ([1d2c8b6](https://github.com/lettermint/lettermint-java/commit/1d2c8b617d9f7ebb0ac6f34833524d01ae1ef27d))
