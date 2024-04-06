package pcd.lab05.monitors.ex_latch;

public class CyclicCountDownLatch implements Latch {
    private final int tokenToBeConsumed;
    private int tokenConsumed;

    public CyclicCountDownLatch(int tokenToBeConsumed) {
        this.tokenToBeConsumed = Math.max(tokenToBeConsumed, 1);
    }

    @Override
    public synchronized void countDown() {
        tokenConsumed++;
        notifyAll();
    }

    @Override
    public synchronized void await() throws InterruptedException {
        while (tokenConsumed < tokenToBeConsumed) {
            wait();
        }
    }

    public void reset() {
        tokenConsumed = 0;
    }
}
