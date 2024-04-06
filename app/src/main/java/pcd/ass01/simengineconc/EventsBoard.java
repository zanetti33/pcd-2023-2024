package pcd.ass01.simengineconc;

public interface EventsBoard {
    /* The simulation waits for all the engine threads to initialize */
    void waitInitEnd();
    /* The engine threads notify their initialization completion */
    void notifyInitCompleted();
    /* The engine threads wait for the env step to start */
    boolean waitStepStart();
    /* The simulation notify the completion of env step, so that engine threads can start the step and waits
    * for the step completion */
    void notifyStepStartAndWaitStepEnd(int dt);
    /* The engine threads notify their step completion */
    void notifyStepCompleted();
    /* The engine threads wait the completion of the sense phase to proceed in the act phase */
    void waitSenseEnd();
    /* The engine threads notify the completion of their sense phase */
    void notifySenseCompleted();
    /* Returns dt for this step execution */
    int getDt();
    /* Notify execution end */
    void notifyEnd();
}
