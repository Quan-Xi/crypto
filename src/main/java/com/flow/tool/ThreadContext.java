package com.flow.tool;

import java.util.Objects;

/**
 * 简单的线程内参数传递方式
 */
public class ThreadContext {
    private static final ThreadLocal<Object> THREAD_LOCAL = new ThreadLocal();
    public static void set(Object o){
        THREAD_LOCAL.set(o);
    }

    public static Object get(){
        return THREAD_LOCAL.get();
    }

    public static Object getOrDefault(Object cache){
        Object localCache = THREAD_LOCAL.get();
        if (Objects.isNull(localCache)) {
            THREAD_LOCAL.set(cache);
            return cache;
        }
        return localCache;
    }

    public static void remove(){
        THREAD_LOCAL.remove();
    }
}
