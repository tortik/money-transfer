package com.revolut.core.service.lock;

import com.google.common.base.Objects;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;
import java.util.function.BiFunction;


public class LockHolder {

    private final Map<Long, LockWrapper> locks = new ConcurrentHashMap<>();

    /**
     * Provides refCounter base on compareFunc
     *
     * @param compareFunc - get refCounter in specific order
     */
    public LockMetaData getMonitor(Long fromAcc, Long toAcc, BiFunction<Long, Long, Long> compareFunc) {
        Long acc = compareFunc.apply(fromAcc, toAcc);
        LockWrapper lock = new LockWrapper(new StampedLock(), new AtomicInteger(0));
        LockWrapper existLockWrapper = locks.putIfAbsent(acc, lock);
        lock = existLockWrapper == null ? lock : existLockWrapper;
        return new LockMetaData(lock, acc);
    }

    public LockMetaData getMonitor(Long acc) {
        LockWrapper lockWrapper = locks.putIfAbsent(acc, new LockWrapper(new StampedLock(), new AtomicInteger(0)));
        return new LockMetaData(lockWrapper, acc);
    }

    public void removeMonitor(LockMetaData metaData) {
        StampedLock lock = metaData.getLockWrapper().getStampedLock();
        locks.remove(metaData.getAccountNumber(), new LockWrapper(lock, new AtomicInteger(metaData.getRefCount())));
    }

    /**
     * Holds metadata for specific refCounter
     */
    class LockMetaData {
        private LockWrapper lockWrapper;
        private long accountNumber;
        private int refCount;

        public LockMetaData(LockWrapper lockWrapper, Long accountNumber) {
            this.lockWrapper = lockWrapper;
            this.accountNumber = accountNumber;
            this.refCount = lockWrapper.refCounter.incrementAndGet();
        }

        public long getAccountNumber() {
            return accountNumber;
        }

        public LockWrapper getLockWrapper() {
            return lockWrapper;
        }

        public int getRefCount() {
            return refCount;
        }
    }

    /**
     * Wrapper under AtomicInteger.
     * Add equals/hashcode functions for removing refCounter from map
     *
     * @see AtomicInteger
     */
    class LockWrapper {
        private StampedLock stampedLock;
        private AtomicInteger refCounter;

        public LockWrapper(StampedLock stampedLock, AtomicInteger refCounter) {
            this.stampedLock = stampedLock;
            this.refCounter = refCounter;
        }

        public StampedLock getStampedLock() {
            return stampedLock;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LockWrapper lockWrapper = (LockWrapper) o;
            return Objects.equal(stampedLock, lockWrapper.stampedLock) &&
                    Objects.equal(refCounter.get(), lockWrapper.refCounter.get());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(refCounter);
        }
    }

}
