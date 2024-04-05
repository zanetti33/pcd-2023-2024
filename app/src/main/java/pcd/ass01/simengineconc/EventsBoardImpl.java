package pcd.ass01.simengineconc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.stream.Stream;

public class EventsBoardImpl implements EventsBoard {

    /* CountDownLatch for each sense step */
    private final List<CountDownLatch> sensePhaseToComplete;
    /* CountDownLatch for each act step */
    private final List<CountDownLatch> actPhaseToComplete;
    /* Barrier that signals when the next step can take place */
    private final List<CountDownLatch> environmentStepCompleted;
    private int currentStep;
    private int currentDt;

    public EventsBoardImpl(int numSteps, int numberOfThreads) {
        // each latch counts each thread sense/act phase,
        // and we have one latch for each step
        this.sensePhaseToComplete = Stream.of(new CountDownLatch(numberOfThreads)).limit(numSteps).toList();
        this.actPhaseToComplete = Stream.of(new CountDownLatch(numberOfThreads)).limit(numSteps).toList();
        // each latch waits for the environment step,
        // and we have one latch for each step
        this.environmentStepCompleted = Stream.of(new CountDownLatch(1)).limit(numSteps).toList();
    }

    @Override
    public void notifyStepStart(int dt) {
        this.currentDt = dt;
        this.environmentStepCompleted.get(this.currentStep).countDown();
    }

    @Override
    public void notifySenseCompleted() {
        this.sensePhaseToComplete.get(this.currentStep).countDown();
    }

    @Override
    public int waitStepStart() {
        /* Each listener reads the current step number before communicating
        * the end of his act phase, because the announcer will
        * update the shared variable immediately after. */
        int currentStep = this.currentStep;
        this.actPhaseToComplete.get(currentStep).countDown();
        try {
            this.environmentStepCompleted.get(currentStep + 1).await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return this.currentDt;
    }

    @Override
    public void waitSenseEnd() {
        try {
            this.sensePhaseToComplete.get(this.currentStep).await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void waitStepEnd() {
        try {
            this.actPhaseToComplete.get(this.currentStep).await();
            this.currentStep++;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
