package pcd.ass01.simengineconc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class EngineThread extends Thread {

    private final Iterable<AbstractAgent> agents;
    private final int totalSteps;
    private final EventsBoard eventsBoard;

    public EngineThread(Iterable<AbstractAgent> agents,
                        int totalSteps,
                        EventsBoard eventsBoard) {
        this.agents = agents;
        this.totalSteps = totalSteps;
        this.eventsBoard = eventsBoard;
    }

    @Override
    public void run() {
        for (int dt = 0; dt < this.totalSteps; dt++) {
            // sense for each agent and communicate it to the eventsBoard
            List<Percept> agentsPercepts = new ArrayList<>();
            for (AbstractAgent a : this.agents) {
                agentsPercepts.add(a.sense());
                this.eventsBoard.senseDone();
            }
            // decide for each agent
            List<Optional<Action>> agentsActions = new ArrayList<>();
            int i = 0;
            for (AbstractAgent a : this.agents) {
                agentsActions.add(a.decide(dt, agentsPercepts.get(i++)));
            }
            // act for each agent only after the eventsBoard says so
            this.eventsBoard.waitForAct(dt);
            i = 0;
            for (AbstractAgent a : this.agents) {
                a.act(agentsActions.get(i++));
            }
            this.eventsBoard.waitForNextStep(dt);
        }
    }
}
