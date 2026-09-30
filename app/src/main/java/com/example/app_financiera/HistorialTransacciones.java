package com.example.app_financiera;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class HistorialTransacciones extends AppCompatActivity {

    private AdminSQLiteOpenHelper admin;
    private TransaccionAdapter adapter;
    private TextView tvVacio;
    private long usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT));
        setContentView(R.layout.activity_historial_transacciones);

        admin = new AdminSQLiteOpenHelper(this);
        usuarioId = Sesion.usuarioId(this);

        tvVacio = findViewById(R.id.tv_historial_vacio);
        MaterialToolbar toolbar = findViewById(R.id.toolbar_historial);

        toolbar.setNavigationOnClickListener(v -> finish());

        ajustarBarrasDelSistema();

        RecyclerView rvTransacciones = findViewById(R.id.rv_transacciones);
        rvTransacciones.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TransaccionAdapter(new ArrayList<>());
        rvTransacciones.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarHistorial();
    }

    private void cargarHistorial() {
        List<Transaccion> lista = admin.obtenerTransacciones(usuarioId);
        adapter.actualizar(lista);

        if (lista.isEmpty()) {
            tvVacio.setVisibility(View.VISIBLE);
        } else {
            tvVacio.setVisibility(View.GONE);
        }
    }

    private void ajustarBarrasDelSistema() {
        View encabezado = findViewById(R.id.encabezado_historial);
        int paddingSuperior = encabezado.getPaddingTop();

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.raiz_historial), (v, insets) -> {
                    Insets barras = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());

                    encabezado.setPadding(
                            encabezado.getPaddingLeft(),
                            paddingSuperior + barras.top,
                            encabezado.getPaddingRight(),
                            encabezado.getPaddingBottom());

                    v.setPadding(0, 0, 0, barras.bottom);
                    return insets;
                });
    }

    @Override
    protected void onDestroy() {
        admin.close();
        super.onDestroy();
    }
}
