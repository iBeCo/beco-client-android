package com.beco.demo;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2500; // 2.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configure status bar and navigation bar
        configureSystemUI();

        setContentView(R.layout.activity_splash);

        // Hide action bar for full screen splash
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize animations
        initializeAnimations();

        // Navigate to MainActivity after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

            // Add smooth transition
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, SPLASH_DURATION);
    }

    private void configureSystemUI() {
        Window window = getWindow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Make status bar and navigation bar transparent for full-screen background
            window.setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.setNavigationBarColor(ContextCompat.getColor(this, android.R.color.transparent));
            }

            // Configure system UI visibility for immersive experience
            View decorView = window.getDecorView();
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

            // Make status bar icons dark to match black content
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;

            // Make navigation bar icons dark (API 27+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }

            decorView.setSystemUiVisibility(flags);
        }

        // Add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
    }

    private void initializeAnimations() {
        // Get views
        ImageView logo = findViewById(R.id.logo);
        TextView appName = findViewById(R.id.app_name);
        TextView tagline = findViewById(R.id.tagline);
        LinearLayout loadingSection = findViewById(R.id.loading_section);

        // Load animations
        Animation logoAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale);
        Animation textAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_up);
        Animation loadingAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_delayed);

        // Apply animations
        logo.startAnimation(logoAnimation);
        appName.startAnimation(textAnimation);
        tagline.startAnimation(textAnimation);
        loadingSection.startAnimation(loadingAnimation);
    }
}
