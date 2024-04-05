package pcd.ass01.simengineconc;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

public class EventsBoardImpl implements EventsBoard {
    /* CountDownLatch for initialization */
    private final CountDownLatch initializeComplete;
    /* CountDownLatch for each sense step */
    private final List<CountDownLatch> sensePhaseToComplete;
    /* CountDownLatch for each act step */
    private final List<CountDownLatch> actPhaseToComplete;
    /* Barrier that signals when the next step can take place */
    private final List<CountDownLatch> environmentStepCompleted;
    private int currentStep;
    private int currentDt;

    public EventsBoardImpl(int numSteps, int numberOfThreads) {
        // this latch check for all the agents initialization
        this.initializeComplete = new CountDownLatch(numberOfThreads);
        // each latch counts each thread sense/act phase,
        // and we have one latch for each step
        this.sensePhaseToComplete = Stream.generate(() -> new CountDownLatch(numberOfThreads)).limit(numSteps + 1).toList();
        this.actPhaseToComplete = Stream.generate(() -> new CountDownLatch(numberOfThreads)).limit(numSteps).toList();
        // each latch waits for the environment step,
        // and we have one latch for each step
        this.environmentStepCompleted = Stream.generate(() -> new CountDownLatch(1)).limit(numSteps + 1).toList();
    }

    @Override
    public void notifyStepStart(int dt) {
        //System.out.println(" EventsBoard notifyStepStart " + this.currentStep);
        this.currentDt = dt;
        this.environmentStepCompleted.get(this.currentStep).countDown();
    }

    @Override
    public void notifySenseCompleted() {
        //System.out.println(" EventsBoard notifySenseCompleted " + this.currentStep);
        this.sensePhaseToComplete.get(this.currentStep).countDown();
    }

    @Override
    public void notifyActCompleted() {
        this.actPhaseToComplete.get(this.currentStep).countDown();
    }

    @Override
    public int waitStepStart() {
        try {
            this.environmentStepCompleted.get(this.currentStep).await();
        } catch (InterruptedException ignored) {}
        return this.currentDt;
    }

    @Override
    public void waitSenseEnd() {
        //System.out.println(" EventsBoard waitSenseEnd " + this.currentStep);
        try {
            this.sensePhaseToComplete.get(this.currentStep).await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void waitStepEnd() {
        //System.out.println(" EventsBoard waitStepEnd " + this.currentStep);
        try {
            this.actPhaseToComplete.get(this.currentStep).await();
            this.currentStep++;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void waitInitEnd() {
        //System.out.println(" EventsBoard waitInitEnd " + this.currentStep);
        try {
            this.initializeComplete.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void notifyInitCompleted() {
        //System.out.println(" EventsBoard notifyInitCompleted " + this.currentStep);
        this.initializeComplete.countDown();
    }
}
