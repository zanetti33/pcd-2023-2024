package pcd.ass01.simengineconc;

import java.util.Iterator;
import java.util.List;

public class EngineThread extends Thread {

    private final Iterable<AbstractAgent> agents;
    private int dt;

    public EngineThread(Iterable<AbstractAgent> agents) {
        this.agents = agents;
    }

    public void setDt(int dt) {
        this.dt = dt;
    }

    @Override
    public void run() {
        for (AbstractAgent a : agents) {
            a.step(dt);
        }
    }
}
