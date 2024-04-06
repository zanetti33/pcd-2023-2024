package pcd.ass01.simengineconc;

public class SimulationState {
    private boolean running;

    public SimulationState() {
        this.running = true;
    }

    public synchronized void resume(){
        this.running = true;
        notify();
    }

    public synchronized void stop(){
        this.running = false;
        notify();
    }

    public synchronized void waitResume() {
        while(!running) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
