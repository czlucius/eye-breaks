package com.jlj.eyecare;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Timer;
import java.util.TimerTask;

public class BreaksService extends Service implements View.OnTouchListener {
    private static final String TAG = "BreaksService";
    public static int NOTIFICATION_ID = 183;

    private WindowManager windowManager;
    private View floatingView;
    private boolean isBreakTime = false;
    private boolean skipped = false;

    private long arbitaryStart;

    private SharedPreferences preferences;
    private Timer timer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = getSharedPreferences(BreaksFragment.PrefConstants.PREF_NAME, Context.MODE_PRIVATE);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, App.NOTIFICATION_CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_baseline_visibility_24)
                .setContentTitle("Eye Break")
                .setContentText("Service is running")
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        startForeground(NOTIFICATION_ID, builder.build());


        // UI
        final WindowManager.LayoutParams params;
        int layoutParamsType;

        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Log.i(TAG, "onCreate");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            layoutParamsType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParamsType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutParamsType,
                0,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER | Gravity.START;
        params.x = 0;
        params.y = 0;

        FrameLayout root = new FrameLayout(this) {
            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {


                // Only fire on the ACTION_DOWN event, or you'll get two events (one for _DOWN, one for _UP)
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    // Check if the HOME button is pressed
                    if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                        // As we've taken action, we'll return true to prevent other apps from consuming the event as well
                        return true;
                    }
                }
                // don't intercept if not back btn
                return super.dispatchKeyEvent(event);
            }
        };

        @NonNull LayoutInflater inflater = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        if (inflater != null) {
            floatingView = inflater.inflate(R.layout.remote_break_layout, root, true);
            windowManager.addView(floatingView, params);

        } else {
            throw new RuntimeException("No inflater from service.");
        }

        Chronometer chronometer = floatingView.findViewById(R.id.durationChronometer);
        //chronometer.setCountDown(true); Commented out for android 6 compatability

        floatingView.findViewById(R.id.skipBreakBtn).setOnClickListener(v -> {
            skipped = true;
        });


        root.setOnTouchListener(this);

        arbitaryStart = System.nanoTime();

        timer = new Timer();
        timer.schedule(new BreaksTimerTask(), 0, 1000);


    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return isBreakTime;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) {
            windowManager.removeView(floatingView);
            floatingView = null;
        }
        timer.cancel();
    }


    private class BreaksTimerTask extends TimerTask {
        Chronometer chronometer = floatingView.findViewById(R.id.durationChronometer);
        @Override
        public void run() {


            // If arbitraryStart = 20min, start break, and if arbitraryStart > 20min20s, reset it to System.nanoTime()
            long breakDuration = preferences.getLong(BreaksFragment.PrefConstants.BREAKS_KEY, 20_000L);
            long usageDurationNs = (long) (preferences.getLong(BreaksFragment.PrefConstants.EVERY_DURATION_KEY, 1_200_000L) * (Math.pow(10, 9)));
            long breakDurationNs = (long) (breakDuration * (Math.pow(10, 9)));
            if ((System.nanoTime() - arbitaryStart) > (usageDurationNs + breakDurationNs)) {
                arbitaryStart = System.nanoTime();
                // back to usage time
                floatingView.post(() -> {
                    floatingView.setVisibility(View.INVISIBLE);
                });

                skipped = false;

            } else if (System.nanoTime() - arbitaryStart > usageDurationNs) {
                // break time!

                if (!isBreakTime) {
                    chronometer.post(() -> {
                        chronometer.setBase(SystemClock.elapsedRealtime() + breakDuration * 1000);
                        chronometer.start();

                    });
                }
                isBreakTime = true;
            } else {
                // usage time
                isBreakTime = false;

            }

            if (!isBreakTime || skipped) {
                floatingView.post(() -> {
                    floatingView.setVisibility(View.INVISIBLE);
                });
            } else {
                floatingView.post(() -> {
                    floatingView.setVisibility(View.VISIBLE);
                });


            }
        }
    }


}
