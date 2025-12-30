package scheduling;

import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import static org.junit.jupiter.api.Assertions.*;

class FairnessAndFatigueTest {

    @Test
    void testFairnessSchedulingLogic() throws InterruptedException {
        // We use 2 threads to observe switching behavior 
        TiredExecutor executor = new TiredExecutor(2);
        
        // 1. Task to tire out the first selected worker
        executor.submit(() -> {
            try { Thread.sleep(400); } catch (InterruptedException e) {}
        });

        // Small delay to ensure the first worker is busy and accumulating timeUsed 
        Thread.sleep(100);

        // 2. Submit a second task. 
        // Liveness/Fairness Check: It MUST be assigned to the idle, less-fatigued worker 
        long startTime = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(1);
        
        executor.submit(latch::countDown);
        
        // If it waits for the first worker (400ms), fairness is broken.
        // If it starts immediately on the 2nd worker, duration will be very low.
        latch.await();
        long duration = System.currentTimeMillis() - startTime;

        assertTrue(duration < 200, "Fairness Failure: Task was queued behind a fatigued worker instead of using an idle one [cite: 328, 381]");

        // 3. Verify worker report exists and shows distribution 
        String report = executor.getWorkerReport();
        assertNotNull(report);
        assertTrue(report.contains("Worker 0") && report.contains("Worker 1"), "Report should include all workers [cite: 381]");
        
        executor.shutdown();
    }
}