package pcd.lab05.monitors.ex_latch;

import java.util.ArrayList;
import java.util.List;

public class TestLatch {

	public static void main(String[] args) {
		
		int nThreadA = 100;
		int nThreadB = 100;

		Latch latch = new CyclicCountDownLatch(nThreadB);
		
		List<ThreadA> threadsAlist = new ArrayList<>();
		for (int i = 0; i < nThreadA; i++) {
			threadsAlist.add(new ThreadA("ThreadA-" + i, latch));
		}

		List<ThreadB> threadsBlist = new ArrayList<>();
		for (int i = 0; i < nThreadB; i++) {
			threadsBlist.add(new ThreadB("ThreadB-" + i, latch));
		}

		for (ThreadA t: threadsAlist) {
			t.start();
		}
		
		for (ThreadB t: threadsBlist) {
			t.start();
		}
		
	}
}
