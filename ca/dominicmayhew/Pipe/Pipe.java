package ca.dominicmayhew.Pipe;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A shared memory pipeline whose interface is the `put()` and `get()` methods.
 * The size of the buffer and the synchronization method can be set at instantiation.
 * 
 * @author Dominic Mayhew                      T00688238
 * @version February 18, 2022 // COMP 3411, Assignment 5
 */
public class Pipe<T> {
    T buf[];

    private Semaphore creatorAccess;
    private Semaphore receiverAccess;

    // Set during instantiation, provides functional interface for `put()`.
    private Putter<T> putter;
    // Set during instantiation, provides functional interface for `get()`.
    private Getter<T> getter;

    private AtomicInteger in;
    private AtomicInteger out;
    private AtomicInteger count;
    private int BUF_SIZE;

    /**
     * Instantiates the memory buffer and sets `put()` and `get()` methods according to the mode `Pipe.AccessMode` provided.
     * `Pipe.AccessMode.mutex` uses two binary semaphores for distinct producer and consumer mutex locks, but allows up to one producer or one consumer to busy wait if the buffer is full/empty.
     * `Pipe.AccessMode.semaphore` uses two counting semaphores to hold distinct access permits for producers and consumers and involves no busy waiting.
     * @param bufSize size of the buffer.
     * @param mode type of synchronization method to use.
     */
    public Pipe(int bufSize, AccessMode mode) {
        buf = (T[]) new Object[bufSize];
        BUF_SIZE = bufSize;

        if (mode == AccessMode.mutex) {
            creatorAccess = new Semaphore(1, false);
            receiverAccess = new Semaphore(1, false);
            count = new AtomicInteger();
            putter = mutexPut;
            getter = mutexAndSpinGet;
        } else {
            creatorAccess = new Semaphore(BUF_SIZE, false);
            receiverAccess = new Semaphore(BUF_SIZE, false);
            receiverAccess.drainPermits();
            putter = semaphorePut;
            getter = semaphoreGet;
        }

        in = new AtomicInteger();
        out = new AtomicInteger();
    }

    /**
     * The interface used by producers.
     * @param item The item to put in the pipeline.
     * @throws InterruptedException if the thread is interrupted while waiting for access.
     */
    public void put(T item) throws InterruptedException {
        putter.put(item);
    }

    /**
     * The interface used by consumers.
     * @return the object received from the buffer.
     * @throws InterruptedException if the thread is interrupted while waiting for access.
     */
    public T get() throws InterruptedException {
        return getter.get();
    }

    /**
     * Putter used when `Pipe.AccessMode.mutex` is provided at instantiation.
     * This Putter gains access through a binary semaphore (mutex lock) and then busy waits until space is available in the buffer.
     */
    private Putter<T> mutexPut = (T item) -> {
        // Acquire mutex.
        try {
            creatorAccess.acquire();
        } catch (InterruptedException ie) {
            throw new InterruptedException("Interrupted trying to acquire access");
        }
        // Spinlock until there is space in the buffer.
        while (count.get() == BUF_SIZE) {
            if (Thread.interrupted()) {
                throw new InterruptedException("Interrupted while busy waiting for space in the buffer.");
            }
        }
        // Put item in buffer and release mutex.
        buf[in.getAndUpdate(x -> (x + 1) % BUF_SIZE)] = item;
        count.incrementAndGet();
        creatorAccess.release();
    };

    /**
     * Getter used when `Pipe.AccessMode.mutex` is provided at instantiation.
     * This Getter gains access through a binary semaphore (mutex lock) and then busy waits until an item is available in the buffer.
     */
    private Getter<T> mutexAndSpinGet = () -> {
        // Acquire access.
        try {
            receiverAccess.acquire();
        } catch (InterruptedException ie) {
            throw new InterruptedException("Interrupted trying to acquire access");
        }
        // Spinlock until there is an item to recieve.
        while (count.get() == 0) {
            if (Thread.interrupted()) {
                throw new InterruptedException("Interrupted while busy waiting for an item to receive");
            }
        }
        // Return item.
        T result = buf[out.getAndUpdate(x -> (x + 1) % BUF_SIZE)];
        count.decrementAndGet();
        receiverAccess.release();
        return result;
    };

    /**
     * Putter used when `Pipe.AccessMode.semaphore` is provided at instantiation.
     * This Putter acquires permits from a counting semaphore. No busy waiting involved.
     * This Putter releases a permit on the consumer semaphore when it has succesfully placed its item in the buffer.
     */
    private Putter<T> semaphorePut = (T item) -> {
        // Acquire access.
        try {
            creatorAccess.acquire();
        } catch (InterruptedException ie) {
            throw new InterruptedException("Interrupted trying to acquire access.");
        }
        // Put item in buffer and signal receiverAccess.
        buf[in.getAndUpdate(x -> (x + 1) % BUF_SIZE)] = item;
        receiverAccess.release();
        return;
    };

    /**
     * Getter used when `Pipe.AccessMode.semaphore` is provided at instantiation.
     * This Getter gains access permits through a counting semaphore. No busy waiting involved.
     * This Getter releases a permit on the producer semaphore when it has successfully returned an item from the buffer. 
     */
    private Getter<T> semaphoreGet = () -> {
        // Acquire access.
        try {
            receiverAccess.acquire();
        } catch (InterruptedException ie) {
            throw new InterruptedException("Interrupted trying to acquire access.");
        }
        // Return item.
        T output = buf[out.getAndUpdate(x -> (x + 1) % BUF_SIZE)];
        creatorAccess.release();
        return output;
    };

    @FunctionalInterface
    private interface Putter<T> {
        abstract void put(T item) throws InterruptedException;
    }
    @FunctionalInterface
    private interface Getter<T> {
        abstract T get() throws InterruptedException;
    }
    // Defines which get/set methods to use.
    public enum AccessMode { mutex, semaphore }
}
