package pcd.ass01.simtrafficconc;

public class TrafficLightInfo {

    private final TrafficLight sem;
    private final Road road;
    private final double roadPos;

    public TrafficLightInfo(TrafficLight sem1, Road road1, double roadPos1) {
        this.sem = sem1;
        this.road = road1;
        this.roadPos = roadPos1;
    }

    public TrafficLight getSem() {
        return sem;
    }

    public synchronized Road getRoad() {
        return road;
    }

    public synchronized double getRoadPos() {
        return roadPos;
    }
}
