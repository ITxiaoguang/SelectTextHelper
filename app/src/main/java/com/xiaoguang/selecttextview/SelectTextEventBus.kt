package com.xiaoguang.selecttextview;


import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 选择文本的事件总线
 * 这里的方法比较low，用户自行实现
 * hxg 2021/9/2 14:26 qq:929842234
 */
public class SelectTextEventBus {
    private static volatile SelectTextEventBus defaultInstance;

    private final Map<Object, List<Class<?>>> typesBySubscriber;

    public SelectTextEventBus() {
        typesBySubscriber = new HashMap<>();
    }

    public static SelectTextEventBus getDefault() {
        if (defaultInstance == null) {
            synchronized (SelectTextEventBus.class) {
                if (defaultInstance == null) {
                    defaultInstance = new SelectTextEventBus();
                }
            }
        }
        return defaultInstance;
    }

    public void register(Object subscriber, Class eventClass) {
        EventBus.getDefault().register(subscriber);

        List<Class<?>> subscribedEvents = typesBySubscriber.get(subscriber);
        if (subscribedEvents == null) {
            subscribedEvents = new ArrayList<>();
            typesBySubscriber.put(subscriber, subscribedEvents);
        }

        subscribedEvents.add(eventClass);
    }

    public synchronized boolean isRegistered(Object subscriber) {
        if (EventBus.getDefault().isRegistered(subscriber)) {
            return true;
        }
        return typesBySubscriber.containsKey(subscriber);
    }

    /**
     * 这里主要实现了注销功能
     */
    public synchronized void unregister() {
        for (Object key : typesBySubscriber.keySet()) {
            EventBus.getDefault().unregister(key);
        }

        typesBySubscriber.clear();
    }

    /**
     * 注销
     */
    public synchronized void unregister(Object subscriber) {
        if (typesBySubscriber.containsKey(subscriber)) {
            EventBus.getDefault().unregister(subscriber);
            typesBySubscriber.remove(subscriber);
        }
    }

    /**
     * 分发事件
     *
     * @param event
     */
    public void dispatch(Object event) {
        EventBus.getDefault().post(event);
    }

}