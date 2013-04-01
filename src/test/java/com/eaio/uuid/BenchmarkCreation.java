package com.eaio.uuid;


import com.google.caliper.Param;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;
import com.google.common.collect.ObjectArrays;
import org.HdrHistogram.Histogram;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: dominictootell
 * Date: 01/04/2013
 * To change this template use File | Settings | File Templates.
 */

/**
 * Caliper run:
 * --captureVmLog --warmupMillis 30000  -Jmemory="-Xms512m -Xmx1024m" -DnoThreads=1,4,8,32,64,128,256
 *
 * noThreads    benchmark      ns linear runtime
 *  1  UUIDThreads    63.8 =
 *  1 UUID2Threads    51.8 =
 *  4  UUIDThreads   338.6 =
 *  4 UUID2Threads   251.7 =
 *  8  UUIDThreads   679.9 =
 *  8 UUID2Threads   510.3 =
 *  32  UUIDThreads  2639.1 ===
 *  32 UUID2Threads  2250.7 ===
 *  64  UUIDThreads  5102.1 =======
 *  64 UUID2Threads  4814.9 ======
 *  128  UUIDThreads 10197.3 ==============
 *  128 UUID2Threads  8064.7 ===========
 *  256  UUIDThreads 20715.1 ==============================
 *  256 UUID2Threads 14577.6 =====================
 *
 * or in debug:
 *
 * --debug --debug-reps 1000000 --captureVmLog --warmupMillis 1000  -Jmemory="-Xms512m -Xmx1024m" -DnoThreads=1,4,8,32,64,128,256
 */


public class BenchmarkCreation extends SimpleBenchmark {

    @Param int noThreads;

    static final long highestTrackableValue = 3600L * 1000 * 1000; // e.g. for 1 hr in usec units
    static final int numberOfSignificantValueDigits = 3;

    private static Histogram uuidHistogram = new Histogram(highestTrackableValue,numberOfSignificantValueDigits);
    private static Histogram uuid2Histogram = new Histogram(highestTrackableValue,numberOfSignificantValueDigits);

    public BenchmarkCreation() {
    }

    public void timeUUIDThreads(int reps) throws InterruptedException {
        long start = System.nanoTime();

        Thread[] threads = new Thread[noThreads];

        for (int i = 0; i < threads.length; i++)
        {
            threads[i] = new Thread(new UUIDRunnable(reps));
        }

        for (Thread t : threads)
        {
            t.start();
        }

        for (Thread t : threads)
        {
            t.join();
        }

        long stop = System.nanoTime();

        uuidHistogram.recordValue((stop-start)/1000000);

    }

    public void timeUUID2Threads(int reps) throws InterruptedException {
        long start = System.nanoTime();

        Thread[] threads = new Thread[noThreads];

        for (int i = 0; i < threads.length; i++)
        {
            threads[i] = new Thread(new UUID2Runnable(reps));
        }

        for (Thread t : threads)
        {
            t.start();
        }

        for (Thread t : threads)
        {
            t.join();
        }

        long stop = System.nanoTime();

        // for --debug
        uuid2Histogram.recordValue((stop-start)/1000000);

    }


    public static void main(String[] args) throws Exception {
//        Runner.main(BenchmarkCreation.class, args);
        new Runner().run(ObjectArrays.concat(args, BenchmarkCreation.class.getName()));

        // for --debug
        System.out.println("==========");
        uuidHistogram.getHistogramData().outputPercentileDistribution(System.out,5,new Double(0.1));
        uuid2Histogram.getHistogramData().outputPercentileDistribution(System.out,5,new Double(0.1));
        System.out.println("==========");
        System.out.flush();


    }


    public class UUIDRunnable implements  Runnable {
        private final long its;

        public UUIDRunnable(long iterations) {
            its = iterations;
        }

        public void run() {
            long i = its + 1;
            UUID id =null;
            while (0 != --i)
            {
                id=new UUID();
            }
            System.out.println(id.toString().length());
        }
    }

    public class UUID2Runnable implements  Runnable {
        private final long its;

        public UUID2Runnable(long iterations) {
            its = iterations;
        }

        public void run() {
            long i = its + 1;
            UUID2 id = null;
            while (0 != --i)
            {
                id=new UUID2();
            }
            System.out.println(id.toString().length());
        }
    }
}
