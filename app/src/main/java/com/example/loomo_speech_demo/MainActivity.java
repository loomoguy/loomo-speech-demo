package com.example.loomo_speech_demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.segway.robot.algo.Pose2D;
import com.segway.robot.algo.minicontroller.CheckPoint;
import com.segway.robot.algo.minicontroller.CheckPointStateListener;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.locomotion.sbv.Base;
import com.segway.robot.sdk.voice.Recognizer;
import com.segway.robot.sdk.voice.VoiceException;
import com.segway.robot.sdk.voice.grammar.GrammarConstraint;
import com.segway.robot.sdk.voice.grammar.Slot;
import com.segway.robot.sdk.voice.recognition.RecognitionListener;
import com.segway.robot.sdk.voice.recognition.RecognitionResult;
import com.segway.robot.sdk.voice.recognition.WakeupListener;
import com.segway.robot.sdk.voice.recognition.WakeupResult;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Recognizer mRecognizer;
    Base mBase;
    ServiceBinder.BindStateListener mRecognizerBindStateListener;
    ServiceBinder.BindStateListener mBaseBindStateListener;
    GrammarConstraint movementGrammar;
    WakeupListener mWakeupListener;
    RecognitionListener mRecognitionListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBase = Base.getInstance();
        mRecognizer = Recognizer.getInstance();
        initListeners();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mRecognizer.bindService(getApplicationContext(), mRecognizerBindStateListener);
        mBase.bindService(getApplicationContext(), mBaseBindStateListener);


    }

    @Override
    protected void onPause() {
        super.onPause();
        mRecognizer.unbindService();
        mBase.unbindService();
    }


    private void initListeners() {

        mRecognizerBindStateListener = new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                try {
                    initGrammar();
                    // Wake-up and Recognition Mode
                    mRecognizer.startWakeupAndRecognition(mWakeupListener, mRecognitionListener);
                    // Recognition only mode
//                    mRecognizer.startRecognitionMode(mRecognitionListener);
                } catch (VoiceException e) {}


            }

            @Override
            public void onUnbind(String reason) {

            }
        };

        mWakeupListener = new WakeupListener() {
            @Override
            public void onStandby() {

            }

            @Override
            public void onWakeupResult(WakeupResult wakeupResult) {

            }

            @Override
            public void onWakeupError(String error) {

            }
        };

        mRecognitionListener = new RecognitionListener() {
            @Override
            public void onRecognitionStart() {

            }

            @Override
            public boolean onRecognitionResult(RecognitionResult recognitionResult) {
                String result = recognitionResult.getRecognitionResult();
                baseOriginReset();
                if (result.contains("rotate")) {
                    if (result.contains("left")) {
                        // rotate left
                        mBase.addCheckPoint(0f,0f, (float) Math.PI/2);
                    } else if (result.contains("right")) {
                        // rotate right
                        mBase.addCheckPoint(0f, 0f, (float) (-1*Math.PI)/2);
                    }

                } else if (result.contains("move") || result.contains("go") || result.contains("turn")) {
                    if (result.contains("forward")) {
                        mBase.addCheckPoint(1f, 0f);
                    } else if (result.contains("backward")) {
                        mBase.addCheckPoint(-1f, 0f);
                    } else if ( result.contains("left")) {
                        mBase.addCheckPoint(0f, 1f);
                    } else if (result.contains("right")) {
                        mBase.addCheckPoint(0f, -1f);
                    }

                }
                return true;
            }

            @Override
            public boolean onRecognitionError(String error) {
                return true;
            }
        };



        mBaseBindStateListener = new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                mBase.setControlMode(Base.CONTROL_MODE_NAVIGATION);
                mBase.setOnCheckPointArrivedListener(new CheckPointStateListener() {
                    @Override
                    public void onCheckPointArrived(CheckPoint checkPoint, Pose2D realPose, boolean isLast) {
                        baseOriginReset();

                    }

                    @Override
                    public void onCheckPointMiss(CheckPoint checkPoint, Pose2D realPose, boolean isLast, int reason) {

                    }
                });

            }

            @Override
            public void onUnbind(String reason) {

            }
        };

    }

    private void initGrammar() throws VoiceException {
        Slot firstSlot = new Slot("movement");
        firstSlot.addWord("move");
        firstSlot.addWord("go");
        firstSlot.addWord("turn");
        firstSlot.addWord("rotate");

        Slot secondSlot = new Slot("direction");
        secondSlot.addWord("forward");
        secondSlot.addWord("backward");
        secondSlot.addWord("left");
        secondSlot.addWord("right");

        List<Slot> movementSlotList = new LinkedList<>();
        movementSlotList.add(firstSlot);
        movementSlotList.add(secondSlot);

        movementGrammar = new GrammarConstraint("movements", movementSlotList);
        mRecognizer.addGrammarConstraint(movementGrammar);


    }

    private void baseOriginReset() {
        mBase.setControlMode(Base.CONTROL_MODE_NAVIGATION);
        //mBase.clearCheckPointsAndStop();
        mBase.cleanOriginalPoint();
        Pose2D newOriginPoint = mBase.getOdometryPose(-1);
        mBase.setOriginalPoint(newOriginPoint);
    }
}
