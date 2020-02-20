package com.xiaxl.demo;

import android.os.Message;
import android.util.Log;

import com.xiaxl.demo.statemachine.State;
import com.xiaxl.demo.statemachine.StateMachine;

public class PersonStateMachine extends StateMachine {


    private static final String TAG = "MachineTest";


    //设置状态改变标志常量
    public static final int MSG_WAKEUP = 1; // 醒
    public static final int MSG_TIRED = 2; // 困
    public static final int MSG_HUNGRY = 3; // 饿
    private static final int MSG_HALTING = 4; //停

    //创建状态
    private State mBoringState = new BoringState();// 默认状态
    private State mWorkState = new WorkState(); // 工作
    private State mEatState = new EatState(); // 吃
    private State mSleepState = new SleepState(); // 睡


    /**
     * 构造方法
     *
     * @param name
     */
    PersonStateMachine(String name) {
        super(name);

        Log.e(TAG, "PersonStateMachine");
        //加入状态，初始化状态
        addState(mBoringState, null);
        addState(mSleepState, mBoringState);
        addState(mWorkState, mBoringState);
        addState(mEatState, mBoringState);

        // sleep状态为初始状态
        setInitialState(mSleepState);
    }

    /**
     * @return 创建启动person 状态机
     */
    public static PersonStateMachine makePerson() {
        Log.e(TAG, "PersonStateMachine  makePerson");
        PersonStateMachine person = new PersonStateMachine("Person");
        person.start();
        return person;
    }


    @Override
    protected void onHalting() {
        Log.e(TAG, "PersonStateMachine  halting");
        synchronized (this) {
            this.notifyAll();
        }
    }


    /**
     * 定义状态:无聊
     */
    class BoringState extends State {
        @Override
        public void enter() {
            Log.e(TAG, "BoringState  enter Boring");
        }

        @Override
        public void exit() {
            Log.e(TAG, "BoringState  exit Boring");
        }

        @Override
        public boolean processMessage(Message msg) {
            Log.e(TAG, "BoringState  processMessage.....");
            return true;
        }
    }

    /**
     * 定义状态:睡觉
     */
    class SleepState extends State {
        @Override
        public void enter() {
            Log.e(TAG, "SleepState  enter Sleep");
        }

        @Override
        public void exit() {
            Log.e(TAG, "SleepState  exit Sleep");
        }

        @Override
        public boolean processMessage(Message msg) {
            Log.e(TAG, "SleepState  processMessage.....");
            switch (msg.what) {
                // 收到清醒信号
                case MSG_WAKEUP:
                    Log.e(TAG, "SleepState  MSG_WAKEUP");
                    // 进入工作状态
                    deferMessage(msg);
                    transitionTo(mWorkState);
                    //
                    //发送饿了信号...
                    sendMessage(obtainMessage(MSG_HUNGRY));

                    break;
                case MSG_HALTING:
                    Log.e(TAG, "SleepState  MSG_HALTING");

                    // 停止
                    transitionToHaltingState();
                    break;
                default:
                    return false;
            }
            return true;
        }


    }


    /**
     * 定义状态:工作
     */
    class WorkState extends State {
        @Override
        public void enter() {
            Log.e(TAG, "WorkState  enter Work");
        }

        @Override
        public void exit() {
            Log.e(TAG, "WorkState  exit Work");
        }

        @Override
        public boolean processMessage(Message msg) {
            Log.e(TAG, "WorkState  processMessage.....");
            switch (msg.what) {
                // 收到 饿了 信号
                case MSG_HUNGRY:
                    Log.e(TAG, "WorkState  MSG_HUNGRY");
                    // 吃饭状态
                    deferMessage(msg);
                    transitionTo(mEatState);

                    // 发送累了信号...
                    sendMessage(obtainMessage(MSG_TIRED));
                    break;
                default:
                    return false;
            }
            return true;
        }


    }

    /**
     * 定义状态:吃
     */
    class EatState extends State {
        @Override
        public void enter() {
            Log.e(TAG, "EatState  enter Eat");
        }

        @Override
        public void exit() {
            Log.e(TAG, "EatState  exit Eat");
        }

        @Override
        public boolean processMessage(Message msg) {
            Log.e(TAG, "EatState  processMessage.....");
            switch (msg.what) {
                // 收到 困了 信号
                case MSG_TIRED:
                    Log.e(TAG, "EatState  MSG_TIRED");
                    // 睡觉
                    deferMessage(msg);
                    transitionTo(mSleepState);

                    // 发出结束信号...
                    sendMessage(obtainMessage(MSG_HALTING));
                    break;
                default:
                    return false;
            }
            return true;
        }

    }
}