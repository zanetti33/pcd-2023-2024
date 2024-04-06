package pcd.ass01.simengineconc;

import java.util.ArrayList;
import java.util.List;

import static pcd.ass01.simengineconc.Logger.log;

/**
 * Base class for defining concrete simulations
 *  
 */
public abstract class AbstractSimulation {

	private static final String MAIN_THREAD_NAME = "Main Thread";
	/* environment of the simulation */
	private AbstractEnvironment env;
	
	/* list of the agents */
	private List<AbstractAgent> agents;
	
	/* simulation listeners */
	private List<SimulationListener> listeners;

	/* simulation threads */
	private List<EngineThreads> agentsThreads;

	/* event board */
	private EventsBoard eventsBoard;

    /* logical time step */
	private int dt;
	
	/* initial logical time */
	private int t0;

	/* in the case of sync with wall-time */
	private boolean toBeInSyncWithWallTime;
	private int nStepsPerSec;
	
	/* for time statistics*/
	private long currentWallTime;
	private long startWallTime;
	private long endWallTime;
	private long averageTimePerStep;

	protected AbstractSimulation() {
		agents = new ArrayList<AbstractAgent>();
		listeners = new ArrayList<SimulationListener>();
		toBeInSyncWithWallTime = false;
	}
	
	/**
	 * 
	 * Method used to configure the simulation, specifying env and agents
	 * 
	 */
	protected abstract void setup();
	
	/**
	 * Method running the simulation for a number of steps,
	 * using a concurrent approach
	 * 
	 * @param numSteps
	 */
	public void run(int numSteps) {		

		startWallTime = System.currentTimeMillis();

		/* initialize the env and the agents inside */
		int t = t0;

		this.notifyReset(t, agents, env);

		this.agentsThreads = new ArrayList<>();
		int numberOfAgents = agents.size();
		int numberOfProcessors = Runtime.getRuntime().availableProcessors();
		int numberOfThreads;
		// with less agents than thread we start less thread
		// (same number as agents) with one agent per thread
		if (numberOfAgents < numberOfProcessors) {
			numberOfThreads = numberOfAgents;
			/* events board */
			this.eventsBoard = new EventsBoardImpl(numberOfThreads);
			for (int i=0; i<numberOfAgents; i++) {
				agentsThreads.add(
						new EngineThreads(
								this.env,
								agents.subList(i, i+1),
								this.eventsBoard
						)
				);
			}
		} else {
			// with number of agents >= number of thread we split the number of
			// agents evenly between the enginge threads
			int assignedAgents = 0;
			numberOfThreads = numberOfProcessors;
			/* events board */
			this.eventsBoard = new EventsBoardImpl(numberOfThreads);
			for (int i=0; i<numberOfThreads; i++) {
				int startIndex = assignedAgents;
				int agentsForThread = numberOfAgents / numberOfThreads;
				int reminder = numberOfAgents % numberOfThreads;
				int endIndex = assignedAgents + agentsForThread;
				if (reminder > 0) {
					endIndex++;
					reminder--;
				}
				List<AbstractAgent> agents = this.agents.subList(startIndex, endIndex);
				agentsThreads.add(
						new EngineThreads(
								this.env,
								agents,
								this.eventsBoard
						)
				);
				assignedAgents += (endIndex - startIndex);
			}
		}

		env.init();
		log(this.getName(), "Init done");
		for (EngineThreads at : agentsThreads) {
			at.start();
		}
		this.eventsBoard.waitInitEnd();

		long timePerStep = 0;
		int nSteps = 0;
		while (nSteps < numSteps) {

			currentWallTime = System.currentTimeMillis();

			env.step(dt);
			log(this.getName(), "Env step done");

			// when env step is over we notify agents to start and wait their completion
			this.eventsBoard.notifyStepStartAndWaitStepEnd(dt);

			t += dt;
			updateView(t, agents, env);

			nSteps++;
			timePerStep += System.currentTimeMillis() - currentWallTime;

			if (toBeInSyncWithWallTime) {
				syncWithWallTime();
			}
		}

		for (EngineThreads at : agentsThreads) {
			at.interrupt();
		}

		this.endWallTime = System.currentTimeMillis();
		this.averageTimePerStep = timePerStep / numSteps;
	}

	private String getName() {
		return MAIN_THREAD_NAME;
	}

	public long getSimulationDuration() {
		return endWallTime - startWallTime;
	}
	
	public long getAverageTimePerCycle() {
		return averageTimePerStep;
	}
	
	/* methods for configuring the simulation */
	
	protected void setupTimings(int t0, int dt) {
		this.dt = dt;
		this.t0 = t0;
	}
	
	protected void syncWithTime(int nCyclesPerSec) {
		this.toBeInSyncWithWallTime = true;
		this.nStepsPerSec = nCyclesPerSec;
	}
		
	protected void setupEnvironment(AbstractEnvironment env) {
		this.env = env;
	}

	protected void addAgent(AbstractAgent agent) {
		agents.add(agent);
	}
	
	/* methods for listeners */
	
	public void addSimulationListener(SimulationListener l) {
		this.listeners.add(l);
	}
	
	private void notifyReset(int t0, List<AbstractAgent> agents, AbstractEnvironment env) {
		for (var l: listeners) {
			l.notifyInit(t0, agents, env);
		}
	}

	private void updateView(int t, List<AbstractAgent> agents, AbstractEnvironment env) {
		for (var l: listeners) {
			l.notifyStepDone(t, agents, env);
		}
	}

	/* method to sync with wall time at a specified step rate */

	private void syncWithWallTime() {
		try {
			long newWallTime = System.currentTimeMillis();
			long delay = 1000 / this.nStepsPerSec;
			long wallTimeDT = newWallTime - currentWallTime;
			if (wallTimeDT < delay) {
				Thread.sleep(delay - wallTimeDT);
			}
		} catch (Exception ex) {}
	}
}
