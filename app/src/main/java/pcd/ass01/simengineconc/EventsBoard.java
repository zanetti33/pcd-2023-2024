package pcd.ass01.simengineconc;

public interface EventsBoard {

    void senseDone();
    void waitForNextStep(int currentDt);
    void waitForAct(int currentDt);

}
