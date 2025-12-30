package scheduling;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class TiredExecutorTest {

    @Test
    void testExecutorLivenessUnderStress() throws InterruptedException {
        // CASE: Small pool, massive task load (Potential for handoff deadlock)
        int numThreads = 2;
        int numTasks = 100;
        TiredExecutor executor = new TiredExecutor(numThreads);
        AtomicInteger completedTasks = new AtomicInteger(0);

        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < numTasks; i++) {
            tasks.add(() -> {
                try { Thread.sleep(5); } catch (InterruptedException e) {}
                completedTasks.incrementAndGet();
            });
        }

        // Action: Submit all tasks and wait for blocking logic 
        executor.submitAll(tasks);

        assertEquals(numTasks, completedTasks.get(), "Executor should complete all tasks without hanging [cite: 195]");
        
        executor.shutdown(); // Clean exit [cite: 346]
    }

    @Test
    void testEmptyPoolFallback() {
        // CASE: 0 threads (Should run task on calling thread or handle gracefully)
        TiredExecutor executor = new TiredExecutor(0);
        AtomicInteger ran = new AtomicInteger(0);
        
        executor.submit(ran::incrementAndGet);
        assertEquals(1, ran.get(), "Executor with 0 threads should still execute tasks (likely on caller thread)");
    }
}