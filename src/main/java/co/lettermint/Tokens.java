package co.lettermint;

import co.lettermint.exceptions.LettermintConfigException;
import java.util.regex.Pattern;

/** Token formats: {@code ApiToken::TEAM_PREFIX} and {@code ::PROJECT_PREFIX} in the Lettermint backend. */
final class Tokens {
    /** Team API tokens. Checked first: every team token also starts with {@code lm_}. */
    private static final Pattern TEAM = Pattern.compile("^lm_team_[0-9A-Za-z]+$");
    /** Project sending tokens (32 or 22 random characters). */
    private static final Pattern SENDING = Pattern.compile("^lm_[0-9A-Za-z]+$");
    /** Characters allowed in a token, so that it is a valid HTTP header value. */
    private static final Pattern HEADER_SAFE = Pattern.compile("^[\\x21-\\x7e]+$");

    enum Kind {
        SENDING,
        TEAM
    }

    private Tokens() {
    }

    /** Classifies a token passed as {@code Lettermint.of(token)}. The message never contains the token. */
    static Kind detect(String token) {
        if (token != null) {
            if (TEAM.matcher(token).matches()) {
                return Kind.TEAM;
            }
            if (SENDING.matcher(token).matches()) {
                return Kind.SENDING;
            }
        }
        throw new LettermintConfigException(
                "Unrecognised token format; pass sendingToken(...) or teamToken(...) to Lettermint.builder() instead.");
    }

    /** Validates an explicitly configured token; null means not set. */
    static Secret check(String option, String token) {
        if (token == null) {
            return null;
        }
        if (token.isEmpty()) {
            throw new LettermintConfigException(option + " must be a non-empty string.");
        }
        if (!HEADER_SAFE.matcher(token).matches()) {
            throw new LettermintConfigException(option + " contains whitespace or characters that are not allowed in an HTTP header.");
        }
        return new Secret(token);
    }
}
