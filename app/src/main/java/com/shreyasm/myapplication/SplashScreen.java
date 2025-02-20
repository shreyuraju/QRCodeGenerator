package com.shreyasm.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the app bar (action bar)
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Ensure Edge-to-Edge works only on Android 14 or higher
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            EdgeToEdge.enable(this);
        }

        setContentView(R.layout.activity_splash_screen);

        // Handling system bars padding for the splash screen layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Delayed navigation to MainActivity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(i);  // Start MainActivity
                finish();  // Optional: Call finish() to remove splash screen from back stack
            }
        }, 2000);  // Delay of 2 seconds
    }
}
