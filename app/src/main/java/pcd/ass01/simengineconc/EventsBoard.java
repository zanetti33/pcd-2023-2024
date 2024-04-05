package pcd.ass01.simengineconc;

public interface EventsBoard {
    void notifyStepStart(int dt);
    void notifySenseCompleted();
    int waitStepStart();
    void waitSenseEnd();
    void waitStepEnd();
}
