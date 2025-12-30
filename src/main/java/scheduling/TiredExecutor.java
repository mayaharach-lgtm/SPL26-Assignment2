package scheduling;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.management.RuntimeErrorException;

public class TiredExecutor {
    private final TiredThread[] workers;
    private final PriorityBlockingQueue<TiredThread> idleMinHeap = new PriorityBlockingQueue<>();
    private final AtomicInteger inFlight = new AtomicInteger(0);

    public TiredExecutor(int numThreads) {
        workers=new TiredThread[numThreads];
        for(int i=0;i<workers.length;i++){
            workers[i]=new TiredThread(i, 0.5 + Math.random());
            workers[i].start();
            idleMinHeap.add(workers[i]);
        }
    }

    public void submit(Runnable task) {
        if (workers.length == 0) {
            task.run();
            return;
        }
        synchronized (this) {
            while (idleMinHeap.isEmpty()) {
                try {
                    this.wait();
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            TiredThread currworker = idleMinHeap.poll();
            inFlight.incrementAndGet();
            Runnable newtask = () -> {
                try {
                    task.run();
                } 
                finally {
                    inFlight.decrementAndGet();
                    synchronized (this) {
                        idleMinHeap.add(currworker);
                        this.notifyAll(); 
                    }
                }
            };
            currworker.newTask(newtask);
        }
    }
    

    public void submitAll(Iterable<Runnable> tasks) {
       for (Runnable task : tasks) {
            submit(task);
       }
       synchronized (this) {
       while (inFlight.get() > 0) {
            try {
                this.wait();
            } 
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break; 
            }
        }
    }   
    }

    public void shutdown() throws InterruptedException {
        for(TiredThread worker:workers){
            if(worker!=null)
                worker.shutdown();
        }
        for(TiredThread worker:workers){
            if(worker!=null)
                worker.join();
        }
    }

    public synchronized String getWorkerReport() {
        String report = "";

        for (TiredThread worker : workers) {
            if (worker != null) {
                long totalIdle = worker.getTimeIdle(); 
                report += "Worker " + worker.getId() +
                          " | FatigueFactor=" + worker.getFatigue() +
                          " | Busy=" + worker.isBusy() +
                          " | Alive=" + worker.isAlive() +
                          " | TimeUsed=" + worker.getTimeUsed() + " ns" +
                          " | TimeIdle=" + totalIdle + " ns\n";
            }
        }

        return report;
    }
}
