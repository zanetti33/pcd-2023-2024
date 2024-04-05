package pcd.ass01.simtrafficconc;

import java.util.Optional;

import pcd.ass01.simengineconc.AbstractAgent;
import pcd.ass01.simengineconc.AbstractEnvironment;
import pcd.ass01.simengineconc.Action;
import pcd.ass01.simengineconc.Percept;

/**
 * 
 * Base class modeling the skeleton of an agent modeling a car in the traffic environment
 * 
 */
public abstract class CarAgent extends AbstractAgent {
	
	/* car model */
	protected double maxSpeed;		
	protected double currentSpeed;  
	protected double acceleration;
	protected double deceleration;

	/* percept and action retrieved and submitted at each step */
	protected CarPercept currentPercept;
	protected Optional<Action> selectedAction;
	
	
	public CarAgent(String id, RoadsEnv env, Road road, 
			double initialPos, 
			double acc, 
			double dec,
			double vmax) {
		super(id);
		this.acceleration = acc;
		this.deceleration = dec;
		this.maxSpeed = vmax;
		env.registerNewCar(this, road, initialPos);
	}

	/**
	 * 
	 * Basic behaviour of a car agent structured into a sense/decide/act structure 
	 * 
	 */
	/*
	public void step(int dt) {

		AbstractEnvironment env = this.getEnv();
		currentPercept = (CarPercept) env.getCurrentPercepts(getId());
		
		selectedAction = Optional.empty();
		
		decide(dt);
		
		if (selectedAction.isPresent()) {
			env.doAction(getId(), selectedAction.get());
		}
	}
	*/

	public Percept sense() {
		return this.getEnv().getCurrentPercepts(getId());
	}

	public void act(Optional<Action> action) {
		action.ifPresent(value -> this.getEnv().doAction(getId(), value));
	}
	
	/**
	 * 
	 * Base method to define the behaviour strategy of the car
	 * 
	 * @param dt
	 */
	
	public double getCurrentSpeed() {
		return currentSpeed;
	}
	
	protected void log(String msg) {
		System.out.println("[CAR " + this.getId() + "] " + msg);
	}

	
}
