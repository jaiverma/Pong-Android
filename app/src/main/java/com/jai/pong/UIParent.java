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

import android.content.Context;
import android.graphics.Color;
import android.graphics.Canvas;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
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
    public final Paddle paddleOne;
    public final Paddle paddleTwo;
    private final CheatButton cheatButton;
    private long previousTime;
    private long elapsedTime;
    private boolean isCheat;
    private int magic;

    private ArrayList<Float> cheat_data;
    private ArrayList<Float> data;
    private long timeCounter;
    private int marker = 0;


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

    @Override
    public void draw(Canvas canvas) {
//        elapsedTime = System.currentTimeMillis() - previousTime;
//        timeCounter += elapsedTime;
//        if (timeCounter >= 200) {
//            if (paddleOne.height != null && paddleTwo.height != null) {
//                data.add(paddleOne.getHeight());
//                cheat_data.add(paddleTwo.getHeight());
//                timeCounter = 0;
//
//                if (data.size() == 50) {
//                    try {
//                        FileWriter w1 = new FileWriter(getContext().getFilesDir() + "/data_" + marker + ".json");
//                        FileWriter w2 = new FileWriter(getContext().getFilesDir() + "/cheat_data_" + marker + ".json");
//                        marker += 1;
//                        String data_s = new Gson().toJson(data);
//                        String cheat_data_s = new Gson().toJson(cheat_data);
//                        w1.write(data_s);
//                        w2.write(cheat_data_s);
//                        w1.flush();
//                        w2.flush();
//                        w1.close();
//                        w2.close();
//                        data.clear();
//                        cheat_data.clear();
//                        // System.exit(0);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }

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
                if (x < getMeasuredWidth() / 2) paddleOne.move(y);
                else {
//                    if (a == 0 && event.getAction() == MotionEvent.ACTION_DOWN) {
//                        ball.toggleCheat();
//                        isCheat = !isCheat;
//                        if (isCheat)
//                            cheatButton.textPaint.setColor(Color.GREEN);
//                        else
//                            cheatButton.textPaint.setColor(Color.RED);
//                    }
                    paddleTwo.move(y);
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
