package com.xiaxl.demo.statemachine;

import android.os.Message;


/**
 * 代码来自：
 * android/4.4w_r1/xref/frameworks/base/core/java/com/android/internal/util/State.java
 */
public class State implements IState {

    /**
     * Constructor
     */
    protected State() {
    }

    /* (non-Javadoc)
     * @see com.android.internal.util.IState#enter()
     */
    @Override
    public void enter() {
    }

    /* (non-Javadoc)
     * @see com.android.internal.util.IState#exit()
     */
    @Override
    public void exit() {
    }

    /* (non-Javadoc)
     * @see com.android.internal.util.IState#processMessage(android.os.Message)
     */
    @Override
    public boolean processMessage(Message msg) {
        return false;
    }

    /**
     * Name of State for debugging purposes.
     *
     * This default implementation returns the class name, returning
     * the instance name would better in cases where a State class
     * is used for multiple states. But normally there is one class per
     * state and the class name is sufficient and easy to get. You may
     * want to provide a setName or some other mechanism for setting
     * another name if the class name is not appropriate.
     *
     * @see com.android.internal.util.IState#processMessage(android.os.Message)
     */
    @Override
    public String getName() {
        String name = getClass().getName();
        int lastDollar = name.lastIndexOf('$');
        return name.substring(lastDollar + 1);
    }
}
