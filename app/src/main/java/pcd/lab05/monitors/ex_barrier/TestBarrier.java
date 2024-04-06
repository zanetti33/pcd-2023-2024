package pcd.lab05.monitors.ex_barrier;

import java.util.ArrayList;
import java.util.List;

public class TestBarrier {

	public static void main(String[] args) {
		
		int nWorkers = 1000;

		/* this barrier is not working */
		Barrier barrier = new SingleUseBarrier(nWorkers);
		
		List<Worker> workers = new ArrayList<Worker>();
		for (int i = 0; i < nWorkers; i++) {
			workers.add(new Worker("Worker-"+i, barrier));
		}

		for (Worker w: workers) {
			w.start();
		}
		
	}
}
