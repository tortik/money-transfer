package com.revolut.core.service.sync;

import com.google.common.base.Objects;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;


public class SyncMonitorHolder {

    private final Map<Long, Monitor> locks = new ConcurrentHashMap<>();

    /**
     * Provides monitor base on compareFunc
     *
     * @param compareFunc - get monitor in specific order
     */
    public MonitorMetaData getMonitor(Long fromAcc, Long toAcc, BiFunction<Long, Long, Long> compareFunc) {
        Long acc = compareFunc.apply(fromAcc, toAcc);
        Monitor monitor = locks.putIfAbsent(acc, new Monitor(new AtomicInteger(0)));
        return new MonitorMetaData(monitor, acc);
    }


    public MonitorMetaData getMonitor(Long acc) {
        Monitor monitor = locks.putIfAbsent(acc, new Monitor(new AtomicInteger(0)));
        return new MonitorMetaData(monitor, acc);
    }

    public void removeMonitor(MonitorMetaData metaData) {
        locks.remove(metaData.getAccountNumber(), new Monitor(new AtomicInteger(metaData.getRefCount())));
    }

    /**
     * Holds metadata for specific monitor
     */
    class MonitorMetaData {
        private Monitor monitor;
        private long accountNumber;
        private int refCount;

        public MonitorMetaData(Monitor monitor, Long accountNumber) {
            this.monitor = monitor;
            this.accountNumber = accountNumber;
            this.refCount = monitor.monitor.incrementAndGet();
        }

        public long getAccountNumber() {
            return accountNumber;
        }

        public Monitor getMonitor() {
            return monitor;
        }

        public int getRefCount() {
            return refCount;
        }
    }

    /**
     * Wrapper under AtomicInteger.
     * Add equals/hashcode functions for removing monitor from map
     *
     * @see AtomicInteger
     */
    class Monitor {
        private AtomicInteger monitor;

        public Monitor(AtomicInteger monitor) {
            this.monitor = monitor;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Monitor monitorWrapper = (Monitor) o;
            return Objects.equal(monitor.get(), monitorWrapper.monitor.get());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(monitor);
        }
    }

}
