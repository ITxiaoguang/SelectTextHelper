package com.xiaoguang.selecttextview

import org.greenrobot.eventbus.EventBus

/**
 * 选择文本的事件总线
 * 这里的方法比较low，用户自行实现
 * hxg 2021/9/2 14:26 qq:929842234
 */
class SelectTextEventBus {
    private val typesBySubscriber: MutableMap<Any, MutableList<Class<*>>>

    init {
        typesBySubscriber = HashMap()
    }

    companion object {
        @Volatile
        private var defaultInstance: SelectTextEventBus? = null

        @JvmStatic
        val default: SelectTextEventBus
            get() {
                if (defaultInstance == null) {
                    synchronized(SelectTextEventBus::class.java) {
                        if (defaultInstance == null) {
                            defaultInstance = SelectTextEventBus()
                        }
                    }
                }
                return defaultInstance!!
            }
    }

    fun register(subscriber: Any, eventClass: Class<*>) {
        EventBus.getDefault().register(subscriber)
        var subscribedEvents = typesBySubscriber[subscriber]
        if (subscribedEvents == null) {
            subscribedEvents = ArrayList()
            typesBySubscriber[subscriber] = subscribedEvents
        }
        subscribedEvents.add(eventClass)
    }

    @Synchronized
    fun isRegistered(subscriber: Any): Boolean {
        return if (EventBus.getDefault().isRegistered(subscriber)) {
            true
        } else typesBySubscriber.containsKey(subscriber)
    }

    /**
     * 这里主要实现了注销功能
     */
    @Synchronized
    fun unregister() {
        for (key in typesBySubscriber.keys) {
            EventBus.getDefault().unregister(key)
        }
        typesBySubscriber.clear()
    }

    /**
     * 注销
     */
    @Synchronized
    fun unregister(subscriber: Any) {
        if (typesBySubscriber.containsKey(subscriber)) {
            EventBus.getDefault().unregister(subscriber)
            typesBySubscriber.remove(subscriber)
        }
    }

    /**
     * 分发事件
     *
     * @param event
     */
    fun dispatch(event: Any?) {
        EventBus.getDefault().post(event)
    }


}