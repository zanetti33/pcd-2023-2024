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
	private final List<AbstractAgent> agents;
	
	/* simulation listeners */
	private final List<SimulationListener> listeners;

    /* Running monitor */
	private final SimulationState simulationState = new SimulationState();

	/* Number of steps monitor */
	private final SynchCell totalNumberOfSteps = new SynchCell();

	/* number of steps executed */
	private int nSteps;

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
		this.totalNumberOfSteps.set(numSteps);
		startWallTime = System.currentTimeMillis();

		/* initialize the env and the agents inside */
		int t = t0;

		this.notifyReset(t, agents, env);

        /* simulation threads */
        List<EngineThreads> agentsThreads = new ArrayList<>();
		int numberOfAgents = agents.size();
		int numberOfProcessors = Runtime.getRuntime().availableProcessors();
		int numberOfThreads;
		// with less agents than thread we start less thread
		// (same number as agents) with one agent per thread
        /* event board */
        EventsBoard eventsBoard;
        if (numberOfAgents < numberOfProcessors) {
			numberOfThreads = numberOfAgents;
			/* events board */
			eventsBoard = new EventsBoardImpl(numberOfThreads);
			for (int i=0; i<numberOfAgents; i++) {
				agentsThreads.add(
						new EngineThreads(
								this.env,
								agents.subList(i, i+1),
                                eventsBoard
						)
				);
			}
		} else {
			// with number of agents >= number of thread we split the number of
			// agents evenly between the enginge threads
			int assignedAgents = 0;
			numberOfThreads = numberOfProcessors;
			/* events board */
			eventsBoard = new EventsBoardImpl(numberOfThreads);
			int agentsForThread = numberOfAgents / numberOfThreads;
			int reminder = numberOfAgents % numberOfThreads;
			for (int i=0; i<numberOfThreads; i++) {
				int startIndex = assignedAgents;
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
                                eventsBoard
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
		eventsBoard.waitInitEnd();

		long totalStepTime = 0;
		while (nSteps < this.totalNumberOfSteps.get()) {

			currentWallTime = System.currentTimeMillis();

			env.step(dt);
			log(this.getName(), "Env step done");

			// when env step is over we notify agents to start and wait their completion
			eventsBoard.notifyStepStartAndWaitStepEnd(dt);

			t += dt;
			updateView(t, agents, env);

			nSteps++;
			totalStepTime += System.currentTimeMillis() - currentWallTime;

			if (toBeInSyncWithWallTime) {
				syncWithWallTime();
			}
			this.simulationState.waitResume();
		}
		eventsBoard.notifyEnd();

		this.endWallTime = System.currentTimeMillis();
		this.averageTimePerStep = totalStepTime / this.totalNumberOfSteps.get();
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

	public void stop() {
		this.simulationState.stop();
	}

	public void resume(int stepsToAdd) {
		int newTotalSteps = nSteps + stepsToAdd;
		this.totalNumberOfSteps.set(newTotalSteps);
		resume();
	}

	public void resume() {
		this.simulationState.resume();
	}
}
