package co.lettermint.models.api;

public final class RbacConflictCode {
    public static final String STALERESOURCE = "stale_resource";
    public static final String OWNERPROTECTED = "owner_protected";
    public static final String LASTOWNER = "last_owner";
    public static final String BUILTINROLEIMMUTABLE = "built_in_role_immutable";
    public static final String CUSTOMROLEREQUIRESPRO = "custom_role_requires_pro";

    private RbacConflictCode() {}
}
