package pcd.lab05.monitors.ex_barrier;

/*
 * Barrier - to be implemented
 */
public class SingleUseBarrier implements Barrier {

	private final int partecipants;
	private int partecipantsThatReachedTheBarrier;
	
	public SingleUseBarrier(int partecipants) {
		this.partecipants = partecipants;
	}
	
	@Override
	public synchronized void hitAndWaitAll() throws InterruptedException {
		partecipantsThatReachedTheBarrier++;
		while (partecipantsThatReachedTheBarrier < partecipants) {
            wait();
        }
		notifyAll();
    }

	
}
