package pcd.lab05.monitors.ex_latch;

/*
 * Latch - to be implemented
 */
public class FakeLatch implements Latch {
	
	public FakeLatch() {}
	
	@Override
	public synchronized void await() throws InterruptedException {

	}

	@Override
	public void countDown() {}

	
}
