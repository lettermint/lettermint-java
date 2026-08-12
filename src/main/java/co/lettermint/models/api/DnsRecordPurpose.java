package co.lettermint.models.api;

public final class DnsRecordPurpose {
    public static final String RETURNPATH = "return_path";
    public static final String DMARC = "dmarc";
    public static final String DKIMLEGACY = "dkim_legacy";
    public static final String DKIMPRIMARY = "dkim_primary";
    public static final String DKIMSECONDARY = "dkim_secondary";

    private DnsRecordPurpose() {}
}
