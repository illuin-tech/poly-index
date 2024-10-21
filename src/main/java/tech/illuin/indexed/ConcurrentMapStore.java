package tech.illuin.indexed;

import tech.illuin.indexed.key.Key;
import tech.illuin.indexed.query.Query;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class ConcurrentMapStore<T> extends MapStore<T>
{
    private final ReadWriteLock lock;

    /**
     * @param index
     */
    public ConcurrentMapStore(Index<T> index)
    {
        super(index);
        this.lock = new ReentrantReadWriteLock();
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
            this.lock.writeLock().lock();
            return super.push(value);
        }
        finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public boolean containsMatch(T match)
    {
        try {
            this.lock.readLock().lock();
            return super.containsMatch(match);
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public List<T> get(List<Query<T>> queries)
    {
        try {
            this.lock.readLock().lock();
            return super.get(queries);
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public List<T> getAll(Key<T> key)
    {
        try {
            this.lock.readLock().lock();
            return super.getAll(key);
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public int count(Key<T> key)
    {
        try {
            this.lock.readLock().lock();
            return super.count(key);
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public List<T> remove(Query<T> query)
    {
        try {
            this.lock.writeLock().lock();
            return super.remove(query);
        }
        finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public boolean isEmpty()
    {
        try {
            this.lock.readLock().lock();
            return super.isEmpty();
        }
        finally {
            this.lock.readLock().unlock();
        }
    }
}
