package justServes.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ServeTest {

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 0.5, 1.0})
    void acceptsAimInsideCourt(double value) {
        Serve serve = new Serve();
        serve.setAimX(value);
        serve.setAimY(value);
        assertEquals(value, serve.getAimX());
        assertEquals(value, serve.getAimY());
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.01, 1.01, Double.NaN})
    void rejectsAimOutsideCourt(double value) {
        Serve serve = new Serve();
        assertThrows(IllegalArgumentException.class, () -> serve.setAimX(value));
        assertThrows(IllegalArgumentException.class, () -> serve.setAimY(value));
    }

    @Test
    void rejectedAimLeavesPreviousValueUnchanged() {
        Serve serve = new Serve();
        serve.setAimX(0.3);
        assertThrows(IllegalArgumentException.class, () -> serve.setAimX(5.0));
        assertEquals(0.3, serve.getAimX());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 6})
    void acceptsValidReceiverCount(int count) {
        Serve serve = new Serve();
        serve.setReceiverCount(count);
        assertEquals(count, serve.getReceiverCount());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 7})
    void rejectsInvalidReceiverCount(int count) {
        Serve serve = new Serve();
        assertThrows(IllegalArgumentException.class, () -> serve.setReceiverCount(count));
    }

    @Test
    void rejectsNullServeType() {
        assertThrows(NullPointerException.class, () -> new Serve().setType(null));
    }

    @Test
    void rejectsNullOutcome() {
        assertThrows(NullPointerException.class, () -> new Serve().setOutcome(null));
    }
}