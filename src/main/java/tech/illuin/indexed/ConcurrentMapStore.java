package tech.illuin.indexed;

import tech.illuin.indexed.key.Key;
import tech.illuin.indexed.lock.RRWLock;
import tech.illuin.indexed.query.Query;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class ConcurrentMapStore<T> extends MapStore<T>
{
    private final RRWLock lock;

    /**
     * @param index
     */
    public ConcurrentMapStore(Index<T> index)
    {
        super(index);
        this.lock = new RRWLock();
    }

    @Override
    protected <V> Map<Key<T>, V> createMap()
    {
        return new ConcurrentHashMap<>();
    }

    @Override
    public MapStore<T> push(T value)
    {
        try {
            this.lock.write.lock();
            return super.push(value);
        }
        finally {
            this.lock.write.unlock();
        }
    }

    @Override
    public boolean containsMatch(T match)
    {
        try {
            this.lock.read.lock();
            return super.containsMatch(match);
        }
        finally {
            this.lock.read.unlock();
        }
    }

    @Override
    public List<T> get(List<Query<T>> queries)
    {
        try {
            this.lock.read.lock();
            return super.get(queries);
        }
        finally {
            this.lock.read.unlock();
        }
    }

    @Override
    public List<T> getAll(Key<T> key)
    {
        try {
            this.lock.read.lock();
            return super.getAll(key);
        }
        finally {
            this.lock.read.unlock();
        }
    }

    @Override
    public int count(Key<T> key)
    {
        try {
            this.lock.read.lock();
            return super.count(key);
        }
        finally {
            this.lock.read.unlock();
        }
    }

    @Override
    public List<T> remove(Query<T> query)
    {
        try {
            this.lock.write.lock();
            return super.remove(query);
        }
        finally {
            this.lock.write.unlock();
        }
    }

    @Override
    public boolean isEmpty()
    {
        try {
            this.lock.read.lock();
            return super.isEmpty();
        }
        finally {
            this.lock.read.unlock();
        }
    }
}
