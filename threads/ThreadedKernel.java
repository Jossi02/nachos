package nachos.threads;

import nachos.machine.*;

/**
 * A multi-threaded OS kernel.
 */
public class ThreadedKernel extends Kernel {
    /**
     * Allocate a new multi-threaded kernel.
     */
    public ThreadedKernel() {
        super();
    }

    /**
     * Initialize this kernel. Creates a scheduler, the first thread, and an
     * alarm, and enables interrupts. Creates a file system if necessary.   
     */
    public void initialize(String[] args) {
        // set scheduler
        String schedulerName = Config.getString("ThreadedKernel.scheduler");
        scheduler = (Scheduler) Lib.constructObject(schedulerName);

        // set fileSystem
        String fileSystemName = Config.getString("ThreadedKernel.fileSystem");
        if (fileSystemName != null)
            fileSystem = (FileSystem) Lib.constructObject(fileSystemName);
        else if (Machine.stubFileSystem() != null)
            fileSystem = Machine.stubFileSystem();
        else
            fileSystem = null;

        // start threading
        new KThread(null);

        alarm  = new Alarm();

        Machine.interrupt().enable();
    }

    /**
     * Test this kernel. Test the <tt>KThread</tt>, <tt>Semaphore</tt>,
     * <tt>SynchList</tt>, and <tt>ElevatorBank</tt> classes. Note that the
     * autograder never calls this method, so it is safe to put additional
     * tests here.
     */
    private static class PingTest implements Runnable {
        PingTest(int which) {
            this.which = which;
        }

        public void run() {
            for (int i=0; i<10; i++) {
                System.out.println("*** thread " + which + " looped "
                        + i + " times");
                KThread.currentThread().yield();
            }
        }

        private int which;
    }


    public void selfTest() {
        System.out.println("=== Priority Scheduler Test ===");

        //스레드 생성
        KThread low = new KThread(new Runnable() {
            public void run() {
                System.out.println("Priority : 2 (Low) thread running at " + Machine.timer().getTime());
            }
        }).setName("Low");

        KThread mid = new KThread(new Runnable() {
            public void run() {
                System.out.println("Priority : 4 (Mid) thread running at " + Machine.timer().getTime());
            }
        }).setName("Mid");

        KThread mid2 = new KThread(new Runnable() {
            public void run() {
                System.out.println("Priority : 4 (Mid2) thread running at " + Machine.timer().getTime());
            }
        }).setName("Mid2");

        KThread high = new KThread(new Runnable() {
            public void run() {
                System.out.println("Priority : 7 (High) thread running at " + Machine.timer().getTime());
            }
        }).setName("High");

        // 우선순위 설정
        boolean intStatus = Machine.interrupt().disable();
        ThreadedKernel.scheduler.setPriority(low, 2);
        ThreadedKernel.scheduler.setPriority(mid, 4);
        ThreadedKernel.scheduler.setPriority(mid2, 4);
        ThreadedKernel.scheduler.setPriority(high, 7);
        Machine.interrupt().restore(intStatus);

        // 순서 확인을 위해 low → mid2 → mid → high 순으로 fork
        low.fork();
        mid2.fork();
        mid.fork();
        high.fork();

        // join으로 종료 대기
        low.join();
        mid.join();
        mid2.join();
        high.join();

        System.out.println("=== Test Finished ===");
    }


        /**
         * A threaded kernel does not run user programs, so this method does
         * nothing.
         */
    public void run() {
    }

    /**
     * Terminate this kernel. Never returns.
     */
    public void terminate() {
        Machine.halt();
    }

    /** Globally accessible reference to the scheduler. */
    public static Scheduler scheduler = null;
    /** Globally accessible reference to the alarm. */
    public static Alarm alarm = null;
    /** Globally accessible reference to the file system. */
    public static FileSystem fileSystem = null;

    // dummy variables to make javac smarter
    private static RoundRobinScheduler dummy1 = null;
    private static PriorityScheduler dummy2 = null;
    private static LotteryScheduler dummy3 = null;
    private static Condition2 dummy4 = null;
    private static Communicator dummy5 = null;
    private static Rider dummy6 = null;
    private static ElevatorController dummy7 = null;
}
