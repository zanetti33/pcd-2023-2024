package pcd.ass01.simengineconc;

import pcd.lab05.monitors.ex_latch.CyclicCountDownLatch;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

public class EventsBoardImpl implements EventsBoard {
    /* CountDownLatch for initialization */
    private final CountDownLatch initializeComplete;
    /* CountDownLatch for sense step */
    private final CyclicCountDownLatch sensePhaseCompleted;
    /* Semaphore for act step */
    private final Semaphore stepCompleted;
    /* Semaphore for environment step  */
    private final Semaphore environmentPhaseCompleted;
    private final int numberOfThreads;
    private int currentStep;
    private int currentDt;

    public EventsBoardImpl(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
        // this latch checks all the agents initialization
        this.initializeComplete = new CountDownLatch(numberOfThreads);
        // this latch checks all the agents sense steps
        this.sensePhaseCompleted = new CyclicCountDownLatch(numberOfThreads);
        // this semaphore gives permission to execute the environment step after every agent has completed his
        // previous act phase
        this.stepCompleted = new Semaphore(0);
        // this semaphore gives permission to agents to start their step after environment step
        this.environmentPhaseCompleted = new Semaphore(0);
    }

    @Override
    public void waitInitEnd() {
        try {
            this.initializeComplete.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void notifyInitCompleted() {
        this.initializeComplete.countDown();
    }

    @Override
    public int waitStepStart() {
        try {
            this.environmentPhaseCompleted.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return this.currentDt;
    }

    @Override
    public void notifyStepStartAndWaitStepEnd(int dt) {
        this.currentDt = dt;
        this.environmentPhaseCompleted.release(this.numberOfThreads);
        try {
            this.stepCompleted.acquire(this.numberOfThreads);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        this.sensePhaseCompleted.reset();
    }

    @Override
    public void notifyStepCompleted() {
        this.stepCompleted.release();
    }

    @Override
    public void waitSenseEnd() {
        try {
            this.sensePhaseCompleted.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void notifySenseCompleted() {
        this.sensePhaseCompleted.countDown();
    }
}
