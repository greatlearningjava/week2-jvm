package com.greatlearning.solution.detection;

import java.util.Random;
import java.util.concurrent.locks.Lock;

public class Runnable2DeadlockDetection implements Runnable{

    private LockGraphFacade lockGraphFacade = null;
    private Lock lock1 = null;
    private Lock lock2 = null;

    public Runnable2DeadlockDetection(LockGraphFacade lockGraphFacade, Lock lock1, Lock lock2) {
        this.lockGraphFacade = lockGraphFacade;
        this.lock1 = lock1;
        this.lock2 = lock2;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        Random random = new Random();
        while(true) {
            int failureCount = 0;
            while(! tryLockBothLocks()) {
                failureCount++;
                int multiplier = random.nextInt(20);
                System.err.println(threadName + " failed to lock both Locks. " +
                        "Waiting a bit before retrying [" + failureCount + " tries]" + " multiplier = " + multiplier);
                sleep(100L * random.nextInt(20));
            }
            if(failureCount > 0) {
                System.out.println(threadName +
                        " succeeded in locking both locks - after " + failureCount + " failures.");
            }else{
                System.out.println(threadName + " Obtained both the locks");
            }

            //do the work - now that both locks have been acquired (locked by this thread)

            //unlock
             System.out.println(threadName + " runnable 2 , unlocking 2");
             this.lockGraphFacade.unlock(this.lock2);
             System.out.println(threadName + " runnable 2 , unlocking 1");
             this.lockGraphFacade.unlock(this.lock1);
            sleep(500L * random.nextInt(10));
        }
    }

    private void sleep(long timeMillis) {
        try {
            Thread.sleep(timeMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean tryLockBothLocks() {
        String threadName = Thread.currentThread().getName();

        System.out.println(threadName + " lock1: attempt lock");
        boolean lock1Succeeded = this.lockGraphFacade.tryLock(this.lock1);
        if(!lock1Succeeded) {
            return false;
        }
        System.out.println(threadName + " lock1: locked");

        sleep(1000);

        System.out.println(threadName + " lock2: attempt lock");
        boolean lock2Succeeded = this.lockGraphFacade.tryLock(this.lock2);
        if(!lock2Succeeded) {
            this.lockGraphFacade.unlock(lock1);
            return false;
        }
        System.out.println(threadName + " lock2: locked");

        return true;
    }
}
