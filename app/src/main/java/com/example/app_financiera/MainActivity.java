package com.example.app_financiera;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;



import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Objects;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private AdminSQLiteOpenHelper admin;
    private TextView tvSaldoTotal;
    private TarjetaAdapter adapter;

    private long usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT));
        setContentView(R.layout.activity_main);

        admin = new AdminSQLiteOpenHelper(this);
        usuarioId = Sesion.usuarioId(this);

        String nombre = admin.obtenerNombreUsuario(usuarioId);

        //Validar usuario si no existe
        if (nombre == null) {
            cerrarSesion();
            return;
        }

        // El tema es NoActionBar, así que la Toolbar hace de Action Bar
        MaterialToolbar toolbar = findViewById(R.id.toolbar_home);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.home_saludo, nombre));

        tvSaldoTotal = findViewById(R.id.tv_saldo_total);


        ajustarBarrasDelSistema();

        RecyclerView rvTarjetas = findViewById(R.id.rv_tarjetas);
        rvTarjetas.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TarjetaAdapter(new ArrayList<>(), tarjeta ->
                // FASE 7: aquí se abrirá DetalleTarjeta
                Toast.makeText(this, tarjeta.getNombre(), Toast.LENGTH_SHORT).show());

        rvTarjetas.setAdapter(adapter);

        findViewById(R.id.btn_nueva_tarjeta).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, NuevaTarjeta.class)));

        findViewById(R.id.btn_enviar).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, EnviarDinero.class)));

        findViewById(R.id.btn_historial).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, HistorialTransacciones.class)));



    }

    // onResume y no onCreate: al volver de una transferencia o de crear
    // una tarjeta, el saldo se actualiza solo
    @Override
    protected void onResume() {
        super.onResume();
        tvSaldoTotal.setText(Formato.dinero(admin.obtenerSaldoTotal(usuarioId)));
        adapter.actualizar(admin.obtenerTarjetas(usuarioId));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.accion_cerrar_sesion) {
            cerrarSesion();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cerrarSesion() {
        Sesion.cerrar(this);

        Intent intent = new Intent(this, Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Mismo manejo de barras que en Login: el encabezado carbón
    // se extiende debajo de la barra de estado
    private void ajustarBarrasDelSistema() {
        View encabezado = findViewById(R.id.encabezado_home);
        int paddingSuperior = encabezado.getPaddingTop();

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.raiz_home), (v, insets) -> {
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