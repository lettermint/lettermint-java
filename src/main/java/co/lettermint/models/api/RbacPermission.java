package co.lettermint.models.api;

public final class RbacPermission {
    public static final String TEAMMANAGE = "team:manage";
    public static final String BILLINGMANAGE = "billing:manage";
    public static final String SECURITYMANAGE = "security:manage";
    public static final String AUDITREAD = "audit:read";
    public static final String SUPPORTMANAGE = "support:manage";
    public static final String MEMBERSREAD = "members:read";
    public static final String MEMBERSMANAGE = "members:manage";
    public static final String ROLESMANAGE = "roles:manage";
    public static final String TEAMTOKENSREAD = "team_tokens:read";
    public static final String TEAMTOKENSMANAGE = "team_tokens:manage";
    public static final String TEAMTOKENSROTATE = "team_tokens:rotate";
    public static final String TEAMTOKENSREVOKE = "team_tokens:revoke";
    public static final String PROJECTSCREATE = "projects:create";
    public static final String TEAMSUPPRESSIONSREAD = "team_suppressions:read";
    public static final String TEAMSUPPRESSIONSADD = "team_suppressions:add";
    public static final String TEAMSUPPRESSIONSREMOVE = "team_suppressions:remove";
    public static final String PROJECTSREAD = "projects:read";
    public static final String PROJECTSMANAGE = "projects:manage";
    public static final String PROJECTSDELETE = "projects:delete";
    public static final String ROUTESREAD = "routes:read";
    public static final String ROUTESMANAGE = "routes:manage";
    public static final String ROUTESDELETE = "routes:delete";
    public static final String DOMAINSREAD = "domains:read";
    public static final String DOMAINSMANAGE = "domains:manage";
    public static final String DOMAINSDELETE = "domains:delete";
    public static final String PROJECTTOKENSREAD = "project_tokens:read";
    public static final String PROJECTTOKENSMANAGE = "project_tokens:manage";
    public static final String PROJECTTOKENSROTATE = "project_tokens:rotate";
    public static final String PROJECTTOKENSREVOKE = "project_tokens:revoke";
    public static final String WEBHOOKSREAD = "webhooks:read";
    public static final String WEBHOOKSMANAGE = "webhooks:manage";
    public static final String WEBHOOKSDELETE = "webhooks:delete";
    public static final String WEBHOOKSROTATESECRET = "webhooks:rotate_secret";
    public static final String STATSREAD = "stats:read";
    public static final String MESSAGESREAD = "messages:read";
    public static final String MESSAGESREADCONTENT = "messages:read_content";
    public static final String MESSAGESSEND = "messages:send";
    public static final String SUPPRESSIONSREAD = "suppressions:read";
    public static final String SUPPRESSIONSADD = "suppressions:add";
    public static final String SUPPRESSIONSREMOVE = "suppressions:remove";

    private RbacPermission() {}
}
