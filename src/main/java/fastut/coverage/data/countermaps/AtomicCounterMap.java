package fastut.coverage.data.countermaps;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import fastut.coverage.data.HasBeenInstrumented;

/**
 * Thread-safe implementation of map that counts number of keys (like multi-set)
 *
 * @author ptab
 * @param <T>
 */
public class AtomicCounterMap<T> implements CounterMap<T>, HasBeenInstrumented {

    private final ConcurrentMap<T, AtomicInteger> counters = new ConcurrentHashMap<T, AtomicInteger>();

    public final void incrementValue(T key, int inc) {
        AtomicInteger v = counters.get(key);
        if (v != null) {
            v.addAndGet(inc);
        } else {
            v = counters.putIfAbsent(key, new AtomicInteger(inc));
            if (v != null) v.addAndGet(inc);
        }
    }

    public final void incrementValue(T key) {
        // AtomicInteger v=counters.putIfAbsent(key, new AtomicInteger(1));
        // return (v!=null)?v.incrementAndGet():1;
        AtomicInteger v = counters.get(key);
        if (v != null) {
            v.incrementAndGet();
        } else {
            v = counters.putIfAbsent(key, new AtomicInteger(1));
            if (v != null) v.incrementAndGet();
        }
    }

    public final int getValue(T key) {
        AtomicInteger v = counters.get(key);
        return v == null ? 0 : v.get();
    }

    public synchronized Map<T, Integer> getFinalStateAndCleanIt() {
        Map<T, Integer> res = new LinkedHashMap<T, Integer>();
        Iterator<Map.Entry<T, AtomicInteger>> iterator = counters.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<T, AtomicInteger> entry = iterator.next();
            T key = entry.getKey();
            int old = entry.getValue().get();
            iterator.remove();
            if (old > 0) {
                res.put(key, old);
            }
        }
        return res;
    }

    public int getSize() {
        return counters.size();
    }

    @Override
    public void clear() {
        counters.clear();
    }
}
