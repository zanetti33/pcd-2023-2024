package pcd.lab01.ex01;

import java.util.*;

public class SequentialSortInt {

	static final int VECTOR_SIZE = 400_000_000;
	
	public static void main(String[] args) {
	
		log("Generating array...");
		int[] v = genArray(VECTOR_SIZE);
		
		log("Array generated.");
		log("Sorting (" + VECTOR_SIZE + " elements)...");
	
		long t0 = System.nanoTime();		
		Arrays.sort(v, 0, v.length);
		long t1 = System.nanoTime();
		log("Done. Time elapsed: " + ((t1 - t0) / 1000000) + " ms");
		
		// dumpArray(v);
	}


	private static int[] genArray(int n) {
		Random gen = new Random(System.currentTimeMillis());
		int[] v = new int[n];
		for (int i = 0; i < v.length; i++) {
			v[i] = gen.nextInt();
		}
		return v;
	}

	private static void dumpArray(int[] v) {
		for (int l:  v) {
			System.out.print(l + " ");
		}
		System.out.println();
	}

	private static void log(String msg) {
		System.out.println(msg);
	}
}
