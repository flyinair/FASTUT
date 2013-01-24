package fastut.coverage.data.countermaps;

import java.util.Map;

public interface CounterMap<T> {

    public void incrementValue(T key);

    public void incrementValue(T key, int value);

    public int getValue(T key);

    public Map<T, Integer> getFinalStateAndCleanIt();

    public void clear();
}
