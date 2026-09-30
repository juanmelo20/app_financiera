package com.example.app_financiera;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;

public class SplashCreen extends AppCompatActivity {

    private static final long DURACION_MS = 2000;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable irAlSiguiente = () -> {
        Class<?> destino = Sesion.activa(SplashCreen.this) ? MainActivity.class : Login.class;
        startActivity(new Intent(SplashCreen.this, destino));
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.dark(Color.TRANSPARENT));
        setContentView(R.layout.activity_splash_creen);
        handler.postDelayed(irAlSiguiente, DURACION_MS);
    }
    @Override
    protected void onDestroy() {
        handler.removeCallbacks(irAlSiguiente);
        super.onDestroy();
    }
}