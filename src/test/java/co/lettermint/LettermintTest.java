package co.lettermint;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LettermintTest {

    @Test
    void testCreateWithToken() {
        Lettermint lettermint = new Lettermint("test-token");
        assertNotNull(lettermint);
        assertNotNull(lettermint.email());
        assertNotNull(lettermint.getClient());
    }

    @Test
    void testCreateWithCustomBaseUrl() {
        Lettermint lettermint = new Lettermint("test-token", "https://custom.api.com/v1");
        assertNotNull(lettermint);
    }

    @Test
    void testCreateWithNullToken() {
        assertThrows(IllegalArgumentException.class, () -> new Lettermint(null));
    }

    @Test
    void testCreateWithEmptyToken() {
        assertThrows(IllegalArgumentException.class, () -> new Lettermint(""));
    }

    @Test
    void testEmailEndpointIsNewInstanceForThreadSafety() {
        Lettermint lettermint = new Lettermint("test-token");
        // Each call returns a new instance to ensure thread-safety
        assertNotSame(lettermint.email(), lettermint.email());
    }
}
