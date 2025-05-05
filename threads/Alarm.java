package nachos.threads;

import nachos.machine.*;

import java.util.LinkedList;
import java.util.Iterator;

/**
 * Uses a hardware timer to provide preemption, and to allow threads to
 * sleep until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to
     * this alarm's callback.
     *
     * <p>
     * <b>Note</b>: Nachos will not function correctly with more than one alarm.
     */
    public Alarm() {
        Machine.timer().setInterruptHandler(new Runnable() {
            public void run() { timerInterrupt(); }
        });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the
     * current thread to yield, and checks whether any sleeping threads
     * need to be woken up.
     */
    public void timerInterrupt() {
        long currentTime = Machine.timer().getTime();
        boolean intStatus = Machine.interrupt().disable();

        Iterator<ThreadTimePair> it = sleepQueue.iterator();
        while (it.hasNext()) {
            ThreadTimePair pair = it.next();
            if (pair.wakeTime <= currentTime) {
                pair.thread.ready();
                it.remove();
            }
        }

        Machine.interrupt().restore(intStatus);
        KThread.yield();
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param  x  the minimum number of clock ticks to wait.
     *
     * @see nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
        long wakeTime = Machine.timer().getTime() + x;
        if (x <= 0) return;

        boolean intStatus = Machine.interrupt().disable();
        sleepQueue.add(new ThreadTimePair(KThread.currentThread(), wakeTime));
        KThread.sleep();
        Machine.interrupt().restore(intStatus);
    }

    /**
     * 내부에서 사용할 (스레드, 깨워야 할 시간) 쌍
     */
    private static class ThreadTimePair {
        KThread thread;
        long wakeTime;

        ThreadTimePair(KThread thread, long wakeTime) {
            this.thread = thread;
            this.wakeTime = wakeTime;
        }
    }
    public static void alarmTest1() {
        int durations[] = {1000, 10*1000, 100*1000};
        long t0, t1;

        for (int d : durations) {
            t0 = Machine.timer().getTime();
            ThreadedKernel.alarm.waitUntil (d);
            t1 = Machine.timer().getTime();
            System.out.println ("alarmTest1: waited for " + (t1 - t0) + " ticks");
        }
    }

    // Implement more test methods here ...

    // Invoke Alarm.selfTest() from ThreadedKernel.selfTest()
    public static void selfTest() {
        alarmTest1();

        // Invoke your other test methods here ...
    }


    private static LinkedList<ThreadTimePair> sleepQueue = new LinkedList<>();
}


