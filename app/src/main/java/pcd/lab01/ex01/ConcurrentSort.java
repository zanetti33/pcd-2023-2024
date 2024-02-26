package pcd.lab01.ex01;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.LongStream;

public class ConcurrentSort {


    static final int VECTOR_SIZE = 256; //200000000;
    static final int N_THREADS = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) throws InterruptedException {

        log("Generating array...");
        long[] v = genArray(VECTOR_SIZE);

        log("Array generated.");
        log(String.format("Sorting (" + VECTOR_SIZE + " elements)... with %d threads", N_THREADS));

        long t0 = System.nanoTime();

        int x=0;
        int elementsForThread = VECTOR_SIZE / N_THREADS;
        List<Thread> runningThreads = new ArrayList<>();
        for(int i=0; i<N_THREADS; i++) {
            int finalI = i;
            int start = x;
            int end = x+elementsForThread;
            Thread thread = new Thread(() -> {
                System.out.printf("[Thread %d]: I'm sorting the array from pos %d to pos %d!\n", finalI, start, end);
                Arrays.sort(v, start, end);
            });
            thread.start();
            runningThreads.add(thread);
            x+=elementsForThread;
        }

        log("waiting for other threads to end");
        for(int i=0; i<N_THREADS; i++) {
            runningThreads.get(i).join();
        }
        log("sorting finished");

        int nSortedArrays = N_THREADS;
        while (nSortedArrays > 1) {
            int currentStart=0;
            int nThreads = nSortedArrays / 2;
            elementsForThread = VECTOR_SIZE / nThreads;
            runningThreads = new ArrayList<>();
            log(String.format("Starting merge of %d arrays with %d threads", nSortedArrays, nThreads));
            for(int i=0; i<nThreads; i++) {
                int posStart = currentStart;
                int posEnd = posStart + elementsForThread;
                int posHalf = posStart + elementsForThread / 2;
                int finalI = i;
                Thread thread = new Thread(() -> {
                    System.out.printf("[Thread %d]: I'm merging the array from pos %d to pos %d and the array from pos %d to pos %d!\n", finalI, posStart, posHalf, posHalf, posEnd);
                    merge(v, posStart, posEnd);
                });
                thread.start();
                runningThreads.add(thread);
                currentStart += VECTOR_SIZE/(nThreads);
            }
            log("waiting for other threads to end");
            for(int i=0; i<nThreads; i++) {
                runningThreads.get(i).join();
            }
            log(String.format("completed merge of %d arrays", nSortedArrays));
            nSortedArrays /= 2;
        }

        long t1 = System.nanoTime();
        log("Done. Time elapsed: " + ((t1 - t0) / 1000000) + " ms");

        dumpArray(v);
    }

    private static void merge(long[] v, int posStart, int posEnd) {
        final int totalValues = posEnd - posStart;
        long[] ordered = new long[totalValues];
        int i = 0;
        final int half = (totalValues) / 2;
        int pos1 = posStart;
        int pos2 = half;
        // fino a che non esaurisco uno dei due vettori cerco il minore tra i due
        while (pos2 < posEnd &&
            pos1 < half) {
            if (v[pos1] < v[pos2]) {
                ordered[i] = v[pos1];
                pos1++;
            } else {
                ordered[i] = v[pos2];
                pos2++;
            }
            i++;
        }
        // finisco di copiare il secondo vettore
        if (pos1 == half) {
            while (i < totalValues) {
                ordered[i] = v[pos2];
                pos2++;
                i++;
            }
        } else {
            // o il primo
            while (i < totalValues) {
                ordered[i] = v[pos1];
                pos1++;
                i++;
            }
        }
        //ricopio tutti i valori ordinati nel vettore originale
        i = posStart;
        for (int j=0; j<totalValues; j++) {
            v[i] = ordered[j];
            i++;
        }
    }


    private static long[] genArray(int n) {
        Random gen = new Random(System.currentTimeMillis());
        long v[] = new long[n];
        for (int i = 0; i < v.length; i++) {
            v[i] = gen.nextLong();
        }
        return v;
    }

    private static void dumpArray(long[] v) {
        for (long l:  v) {
            System.out.print(l + " ");
        }
    }

    private static void log(String msg) {
        System.out.println("[Main]: " + msg);
    }
}
