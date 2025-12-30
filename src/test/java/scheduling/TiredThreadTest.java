package scheduling;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TiredThreadTest {

    @Test
    void testThreadResilienceAndShutdown() throws InterruptedException {
        // Test 1: Resilience to Runtime Exceptions
        TiredThread thread = new TiredThread(1, 1.0);
        thread.start();

        // TASK TO FAIL: Should not kill the worker thread
        thread.newTask(() -> {
            throw new RuntimeException("Intentional failure to test resilience");
        });

        Thread.sleep(100);
        assertTrue(thread.isAlive(), "Worker should remain alive even after a task throws an exception [cite: 332]");

        // Test 2: Measurements after execution
        double fatigueBefore = thread.getFatigue();
        thread.newTask(() -> {
            try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        
        Thread.sleep(150);
        assertTrue(thread.getFatigue() > fatigueBefore, "Fatigue should increase proportionally to timeUsed [cite: 336, 338]");

        // Test 3: Liveness - Proper shutdown with Poison Pill
        thread.shutdown(); // Sends POISON_PILL 
        thread.join(1000);
        assertFalse(thread.isAlive(), "Thread should exit its run loop after receiving poison pill [cite: 330]");
    }
}