package com.xiaxl.demo;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        setContentView(R.layout.activity_main);


        PersonStateMachine personStateMachine = PersonStateMachine.makePerson();
        personStateMachine.sendMessage(PersonStateMachine.MSG_WAKEUP);
    }
}