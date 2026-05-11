package co.lettermint.models.api;

public final class DomainStatus {
    public static final String VERIFIED = "verified";
    public static final String PARTIALLYVERIFIED = "partially_verified";
    public static final String PENDINGVERIFICATION = "pending_verification";
    public static final String FAILEDVERIFICATION = "failed_verification";

    private DomainStatus() {}
}
