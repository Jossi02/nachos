package nachos.threads;

import nachos.machine.*;

import java.util.LinkedList;

public class Condition2 {
    private Lock conditionLock;
    private LinkedList<KThread> waitQueue;

    public Condition2(Lock conditionLock) {
        this.conditionLock = conditionLock;
        this.waitQueue = new LinkedList<KThread>();
    }

    public void sleep() {
        Lib.assertTrue(conditionLock.isHeldByCurrentThread());

        boolean intStatus = Machine.interrupt().disable();

        waitQueue.add(KThread.currentThread());
        conditionLock.release();
        KThread.sleep();
        conditionLock.acquire();

        Machine.interrupt().restore(intStatus);
    }

    public void wake() {
        Lib.assertTrue(conditionLock.isHeldByCurrentThread());

        boolean intStatus = Machine.interrupt().disable();

        if (!waitQueue.isEmpty()) {
            KThread thread = waitQueue.removeFirst();
            thread.ready();
        }

        Machine.interrupt().restore(intStatus);
    }

    public void wakeAll() {
        Lib.assertTrue(conditionLock.isHeldByCurrentThread());

        boolean intStatus = Machine.interrupt().disable();

        while (!waitQueue.isEmpty()) {
            wake();
        }

        Machine.interrupt().restore(intStatus);
    }
}
