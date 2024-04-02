package pcd.ass01.simtrafficconc;

public class CarAgentInfo {

	private CarAgent car;
	private double pos;
	private Road road;
	
	public CarAgentInfo(CarAgent car, Road road, double pos) {
		this.car = car;
		this.road = road;
		this.pos = pos;
	}
	
	public synchronized double getPos() {
		return pos;
	}
	
	public void updatePos(double pos) {
		this.pos = pos;
	}
	
	public CarAgent getCar() {
		return car;
	}	
	
	public synchronized Road getRoad() {
		return road;
	}
}
