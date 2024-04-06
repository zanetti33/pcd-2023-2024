package pcd.ass01.simengineconc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EngineThreads extends Thread {

    private final AbstractEnvironment environment;
    private final Iterable<AbstractAgent> agents;
    private final EventsBoard eventsBoard;

    public EngineThreads(AbstractEnvironment env,
                         Iterable<AbstractAgent> agents,
                         EventsBoard eventsBoard) {
        this.environment = env;
        this.agents = agents;
        this.eventsBoard = eventsBoard;
    }

    @Override
    public void run() {
        for (AbstractAgent a : this.agents) {
            a.init(this.environment);
        }
        this.eventsBoard.notifyInitCompleted();
        while (this.eventsBoard.waitStepStart()) {
            int dt = this.eventsBoard.getDt();
            // sense for each agent and communicate it to the eventsBoard
            List<Percept> agentsPercepts = new ArrayList<>();
            for (AbstractAgent a : this.agents) {
                agentsPercepts.add(a.sense());
            }
            this.eventsBoard.notifySenseCompleted();
            // decide for each agent
            List<Optional<Action>> agentsActions = new ArrayList<>();
            int i = 0;
            for (AbstractAgent a : this.agents) {
                agentsActions.add(a.decide(dt, agentsPercepts.get(i++)));
            }
            // act for each agent only after the eventsBoard says so
            this.eventsBoard.waitSenseEnd();
            i = 0;
            for (AbstractAgent a : this.agents) {
                a.act(agentsActions.get(i++));
            }
            this.eventsBoard.notifyStepCompleted();
        }
    }
}
