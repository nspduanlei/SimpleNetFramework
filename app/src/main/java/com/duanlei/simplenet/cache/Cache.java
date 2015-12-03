package com.duanlei.simplenet.cache;

/**
 * Author: duanlei
 * <p/>
 * Date: 2015-12-03
 *
 * 请求缓存接口
 */
public interface Cache<K, V> {
    public V get(K key);

    public void put(K key, V value);

    public void remove(K key);
}
