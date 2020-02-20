package com.xiaxl.demo.statemachine;

import android.os.Message;


/**
 * 代码来自：
 * android/4.4w_r1/xref/frameworks/base/core/java/com/android/internal/util/State.java
 */
public interface IState {

    /**
     * Returned by processMessage to indicate the the message was processed.
     */
    static final boolean HANDLED = true;

    /**
     * Returned by processMessage to indicate the the message was NOT processed.
     */
    static final boolean NOT_HANDLED = false;

    /**
     * Called when a state is entered.
     */
    void enter();

    /**
     * Called when a state is exited.
     */
    void exit();

    /**
     * Called when a message is to be processed by the
     * state machine.
     * <p>
     * This routine is never reentered thus no synchronization
     * is needed as only one processMessage method will ever be
     * executing within a state machine at any given time. This
     * does mean that processing by this routine must be completed
     * as expeditiously as possible as no subsequent messages will
     * be processed until this routine returns.
     *
     * @param msg to process
     * @return HANDLED if processing has completed and NOT_HANDLED
     * if the message wasn't processed.
     */
    boolean processMessage(Message msg);

    /**
     * Name of State for debugging purposes.
     *
     * @return name of state.
     */
    String getName();
}
