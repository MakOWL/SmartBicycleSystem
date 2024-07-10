package com.example.smart_bicycle20;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.location.LocationCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    public static int SPLASH_SCREEN = 2000;
    Animation topAnim,botAnim;
    ImageView imageView;
    TextView logo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        // Animations
        topAnim = AnimationUtils.loadAnimation(this,R.anim.splash_animation);
        botAnim = AnimationUtils.loadAnimation(this,R.anim.splash_animation_bot);

        // hooks
        imageView = findViewById(R.id.imageView);
        logo = findViewById(R.id.textView);

        // set animations
        imageView.setAnimation(topAnim);
        logo.setAnimation(botAnim);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
                finish();
            }
        },SPLASH_SCREEN);
    }
}