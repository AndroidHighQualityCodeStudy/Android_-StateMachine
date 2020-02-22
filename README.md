## Android frameworks源码StateMachine使用举例及源码解析

工作中有一同事说到Android状态机`StateMachine`。作为一名Android资深工程师，我居然没有听说过`StateMachine`，因此抓紧时间学习一下。
`StateMachine`不是`Android SDK`中的相关API，其存在于`frameworks`层源码中的一个Java类。可能因为如此，许多应用层的开发人员并未使用过。
因此这里我们先说一下`StateMachine`的使用方式，然后再对源码进行相关介绍。

+ StateMachine使用举例
+ StateMachine原理学习


### 一、StateMachine使用举例

StateMachine 处于Android `frameworks`层源码`frameworks/base/core/java/com/android/internal/util`路径下。应用层若要使用`StateMachine`需将对应路径下的三个类拷贝到自己的工程目录下。
这三个类分别为：`StateMachine.java`、`State`、`IState`

下边是使用的代码举例，这个例子我也是网络上找的（**读懂StateMachine源码后，我对这个例子进行了一些简单更改，以下为更改后的案例**）：

主要分以下几个部分来说明：

+ PersonStateMachine.java案例代码
+ PersonStateMachine 使用
+ 案例的简单说明
+ 案例源码下载


#### 1.1、PersonStateMachine.java

创建`PersonStateMachine`继承`StateMachine`类。
创建四种状态，四种状态均继承自`State`：

+ 默认状态 BoringState
+ 工作状态 WorkState
+ 吃饭状态 EatState
+ 睡觉状态 SleepState

定义了状态转换的四种消息类型：

+ 唤醒消息 MSG_WAKEUP
+ 困乏消息 MSG_TIRED
+ 饿了消息 MSG_HUNGRY
+ 状态机停止消息 MSG_HALTING 

下面来看完整的案例代码：


```java
public class PersonStateMachine extends StateMachine {

    private static final String TAG = "MachineTest";

    //设置状态改变标志常量
    public static final int MSG_WAKEUP = 1; // 消息：醒
    public static final int MSG_TIRED = 2; // 消息：困
    public static final int MSG_HUNGRY = 3; // 消息：饿
    private static final int MSG_HALTING = 4; // 状态机暂停消息

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
        PersonStateMachine person = new PersonStateMachine("Person");
        person.start();
        return person;
    }


    @Override
    protected void onHalting() {
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
            Log.e(TAG, "############ enter Boring ############");
        }

        @Override
        public void exit() {
            Log.e(TAG, "############ exit Boring ############");
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
            Log.e(TAG, "############ enter Sleep ############");
        }

        @Override
        public void exit() {
            Log.e(TAG, "############ exit Sleep ############");
        }

        @Override
        public boolean processMessage(Message msg) {
            Log.e(TAG, "SleepState  processMessage.....");
            switch (msg.what) {
                // 收到清醒信号
                case MSG_WAKEUP:
                    Log.e(TAG, "SleepState  MSG_WAKEUP");
                    // 进入工作状态
                    transitionTo(mWorkState);
                    //...
                    //...
                    //发送饿了信号...
                    sendMessage(obtainMessage(MSG_HUNGRY));
                    break;
                case MSG_HALTING:
                    Log.e(TAG, "SleepState  MSG_HALTING");

                    // 转化到暂停状态
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
            Log.e(TAG, "############ enter Work ############");
        }

        @Override
        public void exit() {
            Log.e(TAG, "############ exit Work ############");
        }

        @Override
        public boolean processMessage(Message msg) {
            Log.e(TAG, "WorkState  processMessage.....");
            switch (msg.what) {
                // 收到 饿了 信号
                case MSG_HUNGRY:
                    Log.e(TAG, "WorkState  MSG_HUNGRY");
                    // 吃饭状态
                    transitionTo(mEatState);
                    //...
                    //...
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
            Log.e(TAG, "############ enter Eat ############");
        }

        @Override
        public void exit() {
            Log.e(TAG, "############ exit Eat ############");
        }

        @Override
        public boolean processMessage(Message msg) {
            Log.e(TAG, "EatState  processMessage.....");
            switch (msg.what) {
                // 收到 困了 信号
                case MSG_TIRED:
                    Log.e(TAG, "EatState  MSG_TIRED");
                    // 睡觉
                    transitionTo(mSleepState);
                    //...
                    //...
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
```
#### 1.2、PersonStateMachine 使用

```java
// 获取 状态机引用
PersonStateMachine personStateMachine = PersonStateMachine.makePerson();
// 初始状态为SleepState，发送消息MSG_WAKEUP
personStateMachine.sendMessage(PersonStateMachine.MSG_WAKEUP);
```
+ `SleepState`状态收到`MSG_WAKEUP`消息后，会执行对应状态的`processMessage`方法
+ `SleepState`类中`processMessage`方法收到`MSG_WAKEUP`消息后，执行`transitionTo(mWorkState)`方法，完成状态转换。转换到`WorkState`状态。

#### 1.3、案例的简单说明

几种状态的依赖关系如下：
![在这里插入图片描述](https://upload-images.jianshu.io/upload_images/5969042-09b072f7bc91cd5c?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

构造方法中，添加所有状态，并设置初始状态：

```java
PersonStateMachine(String name) {
    super(name);
    //加入状态，初始化状态
    addState(mBoringState, null);
    addState(mSleepState, mBoringState);
    addState(mWorkState, mBoringState);
    addState(mEatState, mBoringState);
    
    // sleep状态为初始状态
    setInitialState(mSleepState);
}
```

通过以下方法，创建并启动状态机：

```java
public static PersonStateMachine makePerson() {
    PersonStateMachine person = new PersonStateMachine("Person");
    person.start();
    return person;
}
```

#### 1.4、案例源码下载

[Android_StateMachine案例地址](https://github.com/AndroidHighQualityCodeStudy/Android_-StateMachine)


### 二、实现原理学习

在 `StateMachine`中，开启了一个线程`HandlerThread`，其对应的Handler为`SmHandler`。因此上文案例中对应状态的 `processMessage(Message msg)`方法，均在`HandlerThread`线程中执行。

#### 2.1、首先从`StateMachine`的构造方法说起，对应的代码如下：

```java
protected StateMachine(String name) {
    // 创建 HandlerThread
    mSmThread = new HandlerThread(name);
    mSmThread.start();
    // 获取HandlerThread对应的Looper
    Looper looper = mSmThread.getLooper();
    // 初始化 StateMachine
    initStateMachine(name, looper);
}
```
+ `StateMachine`的构造方法中，创建并启动了一个线程`HandlerThread`；
+ `initStateMachine`方法中，创建了`HandlerThread`线程对应的Handler `SmHandler`

```java
private void initStateMachine(String name, Looper looper) {
    mName = name;
    mSmHandler = new SmHandler(looper, this);
}
```

+ `SmHandler`构造方法中，向状态机中添加了两个状态：一个状态为状态机的`暂停状态mHaltingState`、一个状态为状态机的`退出状态mQuittingState` 

```java
private SmHandler(Looper looper, StateMachine sm) {
    super(looper);
    mSm = sm;

    // 添加状态：暂停 和 退出
    // 这两个状态 无父状态
    addState(mHaltingState, null);
    addState(mQuittingState, null);
}
```

+ `mHaltingState`状态，顾名思义让状态机暂停，其对应的`processMessage(Message msg)`方法，返回值为true，将消息消费掉，但不处理消息。从而使状态机状态停顿到`mHaltingState`状态
+ `mQuittingState`状态，若进入该状态， 状态机将退出。`HandlerThread`线程对应的Looper将退出，`HandlerThread`线程会被销毁，所有加入到状态机的状态被清空。


#### 2.2、状态机的start() 方法

状态机的初始化说完，下边来说状态机的启动方法`start()`

```java
public void start() {
    // mSmHandler can be null if the state machine has quit.
    SmHandler smh = mSmHandler;
    // StateMachine 未进行初始化，为什么不抛出一个异常
    if (smh == null) {
        return;
    }
    // 完成状态机建设
   smh.completeConstruction();
}
```

+ 从以上代码可以看到，其中只有一个方法`completeConstruction()`，用于完成状态机的建设。

```java
private final void completeConstruction() {
    int maxDepth = 0;
    // 循环判断所有状态，看看哪一个链最长，得出深度
    for (StateInfo si : mStateInfoHashMap.values()) {
        int depth = 0;
        for (StateInfo i = si; i != null; depth++) {
            i = i.parentStateInfo;
        }
        if (maxDepth < depth) {
            maxDepth = depth;
        }
    }
    // 状态堆栈
    mStateStack = new StateInfo[maxDepth];
    // 临时状态堆栈
    mTempStateStack = new StateInfo[maxDepth];
    // 初始化堆栈
    setupInitialStateStack();

    // 发送初始化完成的消息（消息放入到队列的最前边）
    sendMessageAtFrontOfQueue(obtainMessage(SM_INIT_CMD, mSmHandlerObj));
}
```

+ `maxDepth`是状态机中，最长依赖链的长度。
+ `mStateStack`与`mTempStateStack`为两个用数组实现的堆栈。这两个堆栈的最大长度，即为`maxDepth`。其用来存储`当前活跃状态`与`当前活跃状态的父状态、父父状态、...等`
+ `setupInitialStateStack();`完成状态的初始化，将当前的活跃状态放入到`mStateStack`堆栈中。

下边来具体说`setupInitialStateStack();`方法中，如何完成栈的初始化。

```java
private final void setupInitialStateStack() {
    // 获取初始状态信息
    StateInfo curStateInfo = mStateInfoHashMap.get(mInitialState);
    //
    for (mTempStateStackCount = 0; curStateInfo != null; mTempStateStackCount++) {
        // 初始状态 放入临时堆栈
        mTempStateStack[mTempStateStackCount] = curStateInfo;
        // 当前状态的 所有父状态 一级级放入堆栈
        curStateInfo = curStateInfo.parentStateInfo;
    }

    // 清空 状态堆栈
    // Empty the StateStack
    mStateStackTopIndex = -1;
    // 临时堆栈 换到 状态堆栈
    moveTempStateStackToStateStack();
}
```

+ 拿案例中状态来举例，将`初始化状态`放入 `mTempStateStack`堆栈中
+ 将`初始化状态`的`父状态`、`父父状态`、`父父父状态`... 都一一放入到mTempStateStack堆栈中

![enter description here](https://upload-images.jianshu.io/upload_images/5969042-3ce735290078240f?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

+ 然后moveTempStateStackToStateStack()方法中，`mTempStateStack`出栈，`mStateStack`入栈，将所有状态信息导入到`mStateStack`堆栈，并清空`mTempStateStack`堆栈。

![enter description here](https://upload-images.jianshu.io/upload_images/5969042-4c0d1e7349a4c74c?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

到这里，初始化基本完成，但我们还落下一部分代码没有说：

```java
// 发送初始化完成的消息（消息放入到队列的最前边）
sendMessageAtFrontOfQueue(obtainMessage(SM_INIT_CMD, mSmHandlerObj));
```
+ 发送一个初始化完成的消息到`SmHandler`当中。

下边来看一下`SmHandler`的`handleMessage(Message msg)`方法：

```java
public final void handleMessage(Message msg) {

    // 处理消息
    if (!mHasQuit) {
        // 保存传入的消息
        mMsg = msg;
        State msgProcessedState = null;
        // 已完成初始化
        if (mIsConstructionCompleted) {
		// ..
        }
        // 接收到 初始化完成的消息
        else if (!mIsConstructionCompleted
                && (mMsg.what == SM_INIT_CMD) && (mMsg.obj == mSmHandlerObj)) {
            /** Initial one time path. */
            // 初始化完成
            mIsConstructionCompleted = true;
            // 调用堆栈中状态的enter方法，并将堆栈中的状态设置为活跃状态
            invokeEnterMethods(0);
        } else {
		// ..
        }
        // 执行Transition
        performTransitions(msgProcessedState, msg);
    }
}
```
+ 接收到初始化完成的消息后`mIsConstructionCompleted = true;`对应的标志位变过来
+ 执行 `invokeEnterMethods`方法将`mStateStack`堆栈中的所有状态设置为活跃状态，并由`父—>子`的顺序，执行堆栈中状态的`enter()`方法
+ `performTransitions(msgProcessedState, msg);`在start()时，其中的内容全部不执行，因此先不介绍。

`invokeEnterMethods`方法的方法体如下：
```java
private final void invokeEnterMethods(int stateStackEnteringIndex) {
    for (int i = stateStackEnteringIndex; i <= mStateStackTopIndex; i++) {
        if (mDbg) mSm.log("invokeEnterMethods: " + mStateStack[i].state.getName());
        mStateStack[i].state.enter();
        mStateStack[i].active = true;
    }
}
```
+ 可以看到，其将`mStateStack`堆栈中的所有状态设置为活跃状态，并由`父—>子`的顺序，执行堆栈中状态的`enter()`方法

到此start()完成，最终`mStateStack`堆栈状态，也如上图所示。

#### 2.3、状态转化

还是拿案例中的代码举例：

```java
// 获取 状态机引用
PersonStateMachine personStateMachine = PersonStateMachine.makePerson();
// 初始状态为SleepState，发送消息MSG_WAKEUP
personStateMachine.sendMessage(PersonStateMachine.MSG_WAKEUP);
```
+ 通过调用`sendMessage(PersonStateMachine.MSG_WAKEUP);`方法，向`SmHandler`中发送一个消息，来触发状态转化。
+ 可以说 `sendMessage(PersonStateMachine.MSG_WAKEUP);`消息，为状态转化的导火索。

下边，再次看一下`SmHandler`的`handleMessage(Message msg)`方法：

```java
public final void handleMessage(Message msg) {
    // 处理消息
    if (!mHasQuit) {
        // 保存传入的消息
        mMsg = msg;
        State msgProcessedState = null;
        // 已完成初始化
        if (mIsConstructionCompleted) {
            // 处理消息的状态
            msgProcessedState = processMsg(msg);
        }
        // 接收到 初始化完成的消息
        else if (!mIsConstructionCompleted
                && (mMsg.what == SM_INIT_CMD) && (mMsg.obj == mSmHandlerObj)) {
            // 初始化完成
            mIsConstructionCompleted = true;
            // 调用堆栈中状态的enter方法，并将堆栈中的状态设置为活跃状态
            invokeEnterMethods(0);
        } else {
            throw new RuntimeException("StateMachine.handleMessage: "
                    + "The start method not called, received msg: " + msg);
        }
        // 执行Transition
        performTransitions(msgProcessedState, msg);
    }
}
```

+ 因为初始化已经完成，代码会直接走到`processMsg(msg);`方法中。

我们来看`processMsg(msg);`方法：

```java
private final State processMsg(Message msg) {
    // 堆栈中找到当前状态
    StateInfo curStateInfo = mStateStack[mStateStackTopIndex];
    // 是否为退出消息
    if (isQuit(msg)) {
        // 转化为退出状态
        transitionTo(mQuittingState);
    } else {
        // 状态返回true 则是可处理此状态
        // 状态返回false 则不可以处理
        while (!curStateInfo.state.processMessage(msg)) {
            // 当前状态的父状态
            curStateInfo = curStateInfo.parentStateInfo;
            // 父状态未null
            if (curStateInfo == null) {
                // 回调到未处理消息方法中
                mSm.unhandledMessage(msg);
                break;
            }
        }
    }
    // 消息处理后，返回当前状态信息
    // 如果消息不处理，则返回其父状态处理，返回处理消息的父状态
    return (curStateInfo != null) ? curStateInfo.state : null;
}
```

+ 代码会直接走到`while (!curStateInfo.state.processMessage(msg))`
执行`mStateStack`堆栈中，最上层状态的 `processMessage(msg)`方法。案例中这个状态为`SleepState`
+ 这里
如果`mStateStack`堆栈中状态的processMessage(msg)方法返回true，则表示其消费掉了这个消息；
如果其返回false，则表示不消费此消息，那么该消息将继续向其`父状态`进行传递；
+ 最终将返回，消费掉该消息的状态。

这里，堆栈对上层的状态为`SleepState`。所以我们看一下其对应的`processMessage(msg)`方法。


```java
public boolean processMessage(Message msg) {
    switch (msg.what) {
        // 收到清醒信号
        case MSG_WAKEUP:
            // 进入工作状态
            transitionTo(mWorkState);
            //...
            //...
            //发送饿了信号...
            sendMessage(obtainMessage(MSG_HUNGRY));
            break;
        case MSG_HALTING:
		// ...
            break;
        default:
            return false;
    }
    return true;
}
```

+ 在SleepState状态的`processMessage(Message msg)`方法中，其收到`MSG_WAKEUP`消息后，会调用`transitionTo(mWorkState);`方法，将目标状态设置为`mWorkState`。

我们看一下`transitionTo(mWorkState);`方法：

```java
private final void transitionTo(IState destState) {
    mDestState = (State) destState;
}
```
+ 可以看到，`transitionTo(IState destState)`方法，只是一个简单的状态赋值。

下边我们回到`SmHandler`的`handleMessage(Message msg)`方法：

+ 代码会执行到`SmHandler.handleMessage(Message msg)`的`performTransitions(msgProcessedState, msg);`方法之中。
+ 而这里我们传入的参数`msgProcessedState`为`mSleepState`。

```java
private void performTransitions(State msgProcessedState, Message msg) {
    // 当前状态
    State orgState = mStateStack[mStateStackTopIndex].state;
	// ...
    // 目标状态
    State destState = mDestState;
    if (destState != null) {
        while (true) {
            // 目标状态 放入temp 堆栈
            // 目标状态的 父状态 作为参数 传入下一级
            StateInfo commonStateInfo = setupTempStateStackWithStatesToEnter(destState);
            // commonStateInfo 状态的子状态全部退栈
            invokeExitMethods(commonStateInfo);
            // 目标状态入栈
            int stateStackEnteringIndex = moveTempStateStackToStateStack();
            // 入栈状态 活跃
            invokeEnterMethods(stateStackEnteringIndex);
		    //...
            moveDeferredMessageAtFrontOfQueue();

            if (destState != mDestState) {
                // A new mDestState so continue looping
                destState = mDestState;
            } else {
                // No change in mDestState so we're done
                break;
            }
        }
        mDestState = null;
    }
	// ...
}
```
+ 以上方法中 传入的参数`msgProcessedState`为`mSleepState`
+ 方法中`destState`目标状态为 `mWorkState`

此时此刻`performTransitions(State msgProcessedState, Message msg)`方法中内容的执行示意图如下：

##### A、目标状态放入到mTempStateStack队列中

```java
// 目标状态 放入temp 堆栈
// 目标状态的 父状态 作为参数 传入下一级
StateInfo commonStateInfo = setupTempStateStackWithStatesToEnter(destState);
```

+ 1、将`WorkState`状态放入到`mTempStateStack`堆栈中
+ 2、将`WorkState`状态的`非活跃父状态`一一入`mTempStateStack`堆栈
+ 3、因为`WorkState`状态的父状态为`BoringState`，是活跃状态，因此只将`WorkState`放入到`mTempStateStack`堆栈中
+ 4、返回活跃的父状态`BoringState`

以上代码的执行示意图如下：
![enter description here](https://upload-images.jianshu.io/upload_images/5969042-000ede026b361c7e?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

##### B、`commonStateInfo`状态在`mStateStack`堆栈中的子状态退堆栈

`commonStateInfo`为`setupTempStateStackWithStatesToEnter(destState);`方法的返回参数。这里是`BoringState`

```java
// commonStateInfo 状态的子状态全部退栈
invokeExitMethods(commonStateInfo);
```
+ 1、`BoringState`作为参数传入到`invokeExitMethods(commonStateInfo);`方法中
+ 2、其方法内容为，将`BoringState`状态的`全部子状态退堆栈`

以上代码的执行示意图如下：
![在这里插入图片描述](https://upload-images.jianshu.io/upload_images/5969042-7d84cec802856710?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

##### C、`mTempStateStack`全部状态出堆栈，`mStateStack`入堆栈

```java
// 目标状态入栈
int stateStackEnteringIndex = moveTempStateStackToStateStack();
// 入栈状态 活跃
invokeEnterMethods(stateStackEnteringIndex);
```

+  `moveTempStateStackToStateStack`方法中：`mTempStateStack`全部状态出堆栈，`mStateStack`入堆栈
+  invokeEnterMethods(stateStackEnteringIndex);方法中，将新加入的状态设置为`活跃状态`；并调用其对应的`enter()`方法。

最终的堆栈状态为：

![在这里插入图片描述](https://upload-images.jianshu.io/upload_images/5969042-4ad436e272d77fdf?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

到此StateMachine的源码讲解完成。
感兴趣的同学，还是自己读一遍源码吧，希望我的这篇文章可以为你的源码阅读提供一些帮助。

## ========== THE END ==========
![wx_gzh.jpg](https://upload-images.jianshu.io/upload_images/5969042-09fd25b1ec5b86fe.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
