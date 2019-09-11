/*
 * Copyright (C) 2015 Willi Ye
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jai.pong;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 * Created by willi on 08.05.15.
 */
public class UIParent extends View {

    public enum PLAYER {
        ONE, TWO
    }

    private final Point pointOne;
    private final Point pointTwo;
    private final Ball ball;
    private final Paddle paddleOne;
    private final Paddle paddleTwo;
    private final CheatButton cheatButton;
    private long previousTime;
    private long elapsedTime;
    private boolean isCheat;
    private int magic;

    private ArrayList<Float> cheat_data;
    private ArrayList<Float> data;
    private long timeCounter;
    private int marker = 0;

    // Message IPC
    Messenger mService = null;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    public static final int MSG_INFERENCE = 1;
    boolean mIsBound;

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INFERENCE:
                    Log.d("qace-message", "MSG_INFERENCE: " + msg.arg1);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            Log.d("qace-message", "Connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
            Log.d("qace-message", "Disconnected");
        }
    };

    void doBindService() {
        Intent i = new Intent();
        i.setComponent(new ComponentName("com.example.qace", "com.example.qace.QACEService"));
        getContext().bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            Log.d("qace-message", "doUnbindService");
            getContext().unbindService(mConnection);
            mIsBound = false;
        }
    }

    void doSendMsg(Bundle b) {
        if (mIsBound) {
            Log.i("qace-message", "doSendMsg");
            try {
                Message msg = Message.obtain(null, MSG_INFERENCE);
                msg.replyTo = mMessenger;
                msg.obj = b;
                mService.send(msg);
            } catch (RemoteException e) {
            }
        }
    }

    public UIParent(Context context) {
        this(context, null);
    }

    public UIParent(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UIParent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setBackgroundColor(context.getColor(android.R.color.black));

        cheatButton = new CheatButton(context);
        pointOne = new Point(context, PLAYER.ONE);
        pointTwo = new Point(context, PLAYER.TWO);
        paddleOne = new Paddle(context, PLAYER.ONE);
        paddleTwo = new Paddle(context, PLAYER.TWO);
        isCheat = true;

        magic = 0xdeadbeef;
        timeCounter = 0;
        data = new ArrayList<>();
        cheat_data = new ArrayList<>();

        ball = new Ball(context, paddleOne, paddleTwo, isCheat, new Ball.OnPointListener() {
            @Override
            public void playerOne() {
                pointOne.point++;
                reset();
            }

            @Override
            public void playerTwo() {
                pointTwo.point++;
                reset();
            }

            private void reset() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                            ball.reset();
                            paddleOne.reset();
                            paddleTwo.reset();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    private void writeData(ArrayList<Float> data, String filePath) {
        try {
            FileOutputStream f = new FileOutputStream(filePath);
            ByteBuffer byteBuffer = ByteBuffer.allocate(4);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            for (float i : data) {
                byteBuffer.clear();
                byteBuffer.putFloat(i);
                f.write(byteBuffer.array());
            }
            f.flush();
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private float[] makeFloat(ArrayList<Float> data) {
        float[] f = new float[data.size()];
        int i = 0;
        for (Float d : data) {
            f[i++] = d;
        }
        return f;
    }

    @Override
    public void draw(Canvas canvas) {
        elapsedTime = System.currentTimeMillis() - previousTime;
        timeCounter += elapsedTime;
        if (timeCounter >= 200) {
//            Log.i("pong-file", "Adding data: " + String.valueOf(data.size()));
            if (paddleOne.height != null && paddleTwo.height != null) {
                data.add(paddleOne.getHeight());
                cheat_data.add(paddleTwo.getHeight());
                timeCounter = 0;

                if (data.size() == 50) {
                    Log.d("qace-message", "Sending data");
                    Bundle b = new Bundle();
//                    b.putFloatArray("pong-data", makeFloat(data));
                    b.putFloatArray("pong-data", makeFloat(cheat_data));
                    doSendMsg(b);
                    marker += 1;
                    data.clear();
                    cheat_data.clear();
                }
            }
        }

        super.draw(canvas);

        cheatButton.draw(canvas);
        pointOne.draw(canvas);
        pointTwo.draw(canvas);
        paddleOne.draw(canvas);
        paddleTwo.draw(canvas);
        ball.draw(canvas);
        previousTime = System.currentTimeMillis();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            int num = event.getPointerCount();
            for (int a = 0; a < num; a++) {
                int x = (int) event.getX(event.getPointerId(a));
                int y = (int) event.getY(event.getPointerId(a));
                if (x < getMeasuredWidth() - getMeasuredWidth() / 4) paddleOne.move(y);
                else {
                    if (a == 0 && event.getAction() == MotionEvent.ACTION_DOWN) {
                        ball.toggleCheat();
                        isCheat = !isCheat;
                        if (isCheat)
                            cheatButton.textPaint.setColor(Color.GREEN);
                        else
                            cheatButton.textPaint.setColor(Color.RED);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int desiredWidth = getResources().getDisplayMetrics().widthPixels;
        int desiredHeight = getResources().getDisplayMetrics().heightPixels;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) width = widthSize;
        else if (widthMode == MeasureSpec.AT_MOST) width = Math.min(desiredWidth, widthSize);
        else width = desiredWidth;

        if (heightMode == MeasureSpec.EXACTLY) height = heightSize;
        else if (heightMode == MeasureSpec.AT_MOST) height = Math.min(desiredHeight, heightSize);
        else height = desiredHeight;

        setMeasuredDimension(width, height);
    }
}
