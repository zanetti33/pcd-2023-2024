package pcd.ass01.simengine_conc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MasterAgent extends Thread {
	
	private boolean toBeInSyncWithWallTime;
	private int nStepsPerSec;
	private int numSteps;

	private long currentWallTime;
	
	private AbstractSimulation sim;
	private Flag stopFlag;
	private Semaphore done;
	private int nWorkers;
	
	public MasterAgent(AbstractSimulation sim, int nWorkers, int numSteps, Flag stopFlag, Semaphore done, boolean syncWithTime) {
		toBeInSyncWithWallTime = false;
		this.sim = sim;
		this.stopFlag = stopFlag;
		this.numSteps = numSteps;
		this.done = done;
		this.nWorkers = nWorkers;
		
		if (syncWithTime) {
			this.syncWithTime(25);
		}
	}

	public void run() {
		
		log("booted");
		
		var simEnv = sim.getEnvironment();
		var simAgents = sim.getAgents();
		
		simEnv.init();
		for (var a: simAgents) {
			a.init(simEnv);
		}

		int t = sim.getInitialTime();
		int dt = sim.getTimeStep();
		
		sim.notifyReset(t, simAgents, simEnv);
		
		Trigger canDoStep = new Trigger(nWorkers);
		CyclicBarrier jobDone = new CyclicBarrier(nWorkers + 1);
		
		log("creating workers...");
		/*
		int nAssignedAgentsPerWorker = simAgents.size()/nWorkers;

		int index = 0;
		List<WorkerAgent> workers = new ArrayList<>();
		for (int i = 0; i < nWorkers - 1; i++) {
			List<AbstractAgent> assignedSimAgents = new ArrayList<>();
			for (int j = 0; j < nAssignedAgentsPerWorker; j++) {
				assignedSimAgents.add(simAgents.get(index));
				index++;
			}
			
			WorkerAgent worker = new WorkerAgent("worker-"+i, assignedSimAgents, dt, canDoStep, jobDone, stopFlag);
			worker.start();
			workers.add(worker);
		}
		
		List<AbstractAgent> assignedSimAgents = new ArrayList<>();
		while (index < simAgents.size()) {
			assignedSimAgents.add(simAgents.get(index));
			index++;
		}

		WorkerAgent worker = new WorkerAgent("worker-"+(nWorkers-1), assignedSimAgents, dt, canDoStep, jobDone, stopFlag);
		worker.start();
		workers.add(worker);
		 */

		int nAssignedAgentsPerWorker = simAgents.size()/nWorkers;

		int index = 0;
		List<List<AbstractAgent>> agentGroups = new ArrayList<>();
		for (int i = 0; i < nWorkers - 1; i++) {
			List<AbstractAgent> assignedSimAgents = new ArrayList<>();
			for (int j = 0; j < nAssignedAgentsPerWorker; j++) {
				assignedSimAgents.add(simAgents.get(index));
				index++;
			}
			agentGroups.add(assignedSimAgents);
		}

		List<AbstractAgent> assignedSimAgents = new ArrayList<>();
		while (index < simAgents.size()) {
			assignedSimAgents.add(simAgents.get(index));
			index++;
		}
		agentGroups.add(assignedSimAgents);

		log("starting the simulation loop.");

		int step = 0;
		currentWallTime = System.currentTimeMillis();

		try (ExecutorService executorService = Executors.newFixedThreadPool(nWorkers)) {
			while (!stopFlag.isSet() &&  step < numSteps) {
				
				simEnv.step(dt);
				simEnv.cleanActions();

				/* trigger workers to do their work in this step */	
				//canDoStep.trig();
				
				/* wait for workers to complete */
				//jobDone.await();
				List<Future<?>> tasks = new ArrayList<>();
				index = 0;
				for (List<AbstractAgent> group : agentGroups) {
					String id = index + " in step " + t;
					tasks.add(executorService.submit(() -> executeStepForAgents(id, group, dt)));
					index++;
				}
				for (Future<?> task : tasks) {
					task.get();
				}

				/* executed actions */
				simEnv.processActions();
								
				sim.notifyNewStep(t, simAgents, simEnv);
	
				if (toBeInSyncWithWallTime) {
					syncWithWallTime();
				}
				
				/* updating logic time */
				
				t += dt;
				step++;
			}	
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		log("done");
		stopFlag.set();
		canDoStep.trig();

		done.release();
	}

	private void executeStepForAgents(String id, List<AbstractAgent> assignedSimAgents, int dt) {
		/* moving on agents */
		System.out.println(" [Task " + id + "] step of " + assignedSimAgents.size() + " agents");
		for (var ag: assignedSimAgents) {
			ag.step(dt);
		}
	}

	private void syncWithTime(int nStepsPerSec) {
		this.toBeInSyncWithWallTime = true;
		this.nStepsPerSec = nStepsPerSec;
	}

	private void syncWithWallTime() {
		try {
			long newWallTime = System.currentTimeMillis();
			long delay = 1000 / this.nStepsPerSec;
			long wallTimeDT = newWallTime - currentWallTime;
			currentWallTime = System.currentTimeMillis();
			if (wallTimeDT < delay) {
				Thread.sleep(delay - wallTimeDT);
			}
		} catch (Exception ex) {}
		
	}
	
	private void log(String msg) {
		System.out.println("[MASTER] " + msg);
	}
	
	
}
