package pcd.ass01.simengineconc;

import java.util.Optional;

/**
 * 
 * Base  class for defining types of agents taking part to the simulation
 * 
 */
public abstract class AbstractAgent {
	
	private String myId;
	private AbstractEnvironment env;
	
	/**
	 * Each agent has an identifier
	 * 
	 * @param id
	 */
	protected AbstractAgent(String id) {
		this.myId = id;
	}
	
	/**
	 * This method is called at the beginning of the simulation
	 * 
	 * @param env
	 */
	public void init(AbstractEnvironment env) {
		this.env = env;
	}

	abstract public Percept sense();

	abstract public Optional<Action> decide(int dt, Percept percept);

	abstract public void act(Optional<Action> action);


	public String getId() {
		return myId;
	}
	
	protected AbstractEnvironment getEnv() {
		return this.env;
	}
}
