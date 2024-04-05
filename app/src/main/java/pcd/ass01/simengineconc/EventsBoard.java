package pcd.ass01.simengineconc;

public interface EventsBoard {
    void notifyStepStart(int dt);
    void notifySenseCompleted();
    void notifyActCompleted();
    int waitStepStart();
    void waitSenseEnd();
    void waitStepEnd();
    void waitInitEnd();
    void notifyInitCompleted();
}
