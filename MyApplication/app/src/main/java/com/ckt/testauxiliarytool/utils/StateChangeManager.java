package com.ckt.testauxiliarytool.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <br/>Author: pecuyu
 * <br/>Email: yu.qin@ck-telecom.com
 * <br/>Date: 2017/11/8
 * <br/>TODO: 通信接口，状态管理类
 */

public class StateChangeManager {
    private HashMap<String, OnPublishStateChangeListener> mPublishStateChangeListeners = new HashMap<>();

    private StateChangeManager() {
    }

    public static StateChangeManager getInstance() {
        return InstanceHolder.sManager;
    }

    private static class InstanceHolder {
        private static final StateChangeManager sManager = new StateChangeManager();
    }


    public interface OnPublishStateChangeListener {
        void onPublishStateChange();
    }

    /* 添加监听*/
    public void setPublishStateChangeListener(String key, StateChangeManager.OnPublishStateChangeListener publishStateChangeListener) {
        if (key == null) return;
        mPublishStateChangeListeners.put(key, publishStateChangeListener);
    }

    /* 发布所有的状态改变*/
    public void publishAllStateChange() {
        if (mPublishStateChangeListeners != null && !mPublishStateChangeListeners.isEmpty()) {
            Set<Map.Entry<String, OnPublishStateChangeListener>> entries = mPublishStateChangeListeners.entrySet();
            for (Map.Entry<String, OnPublishStateChangeListener> entry : entries) {
                OnPublishStateChangeListener listener = entry.getValue();
                if (listener != null) listener.onPublishStateChange();
            }
        }
    }

    public void clearAllStateListeners() {
        if (mPublishStateChangeListeners != null) {
            mPublishStateChangeListeners.clear();
        }
    }

    public void removeListenerAt(String key) {
        if (mPublishStateChangeListeners != null) {
            mPublishStateChangeListeners.remove(key);
        }
    }

    public void removeListener(OnPublishStateChangeListener listener) {
        if (mPublishStateChangeListeners != null) {
            mPublishStateChangeListeners.remove(getKeyByValue(listener));
        }
    }


    /* 发布状态改变 */
    public void publishStateChange(String key) {
        StateChangeManager.OnPublishStateChangeListener changeListener = mPublishStateChangeListeners.get(key);
        if (changeListener != null) {
            changeListener.onPublishStateChange();
        }
    }

    private String getKeyByValue(OnPublishStateChangeListener listener) {
        Set<Map.Entry<String, OnPublishStateChangeListener>> entries = mPublishStateChangeListeners.entrySet();
        for (Map.Entry<String, OnPublishStateChangeListener> entry : entries) {
            if (entry.getValue() == listener) {
                return entry.getKey();
            }
        }
        return null;
    }
}
