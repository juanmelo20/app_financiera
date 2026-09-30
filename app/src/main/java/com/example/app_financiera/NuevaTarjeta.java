package com.example.app_financiera;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

public class NuevaTarjeta extends AppCompatActivity {

    private TextInputLayout tilNombreTarjeta;
    private AdminSQLiteOpenHelper admin;
    private long usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT));
        setContentView(R.layout.activity_nueva_tarjeta);

        admin = new AdminSQLiteOpenHelper(this);
        usuarioId = Sesion.usuarioId(this);

        tilNombreTarjeta = findViewById(R.id.til_nombre_tarjeta);
        MaterialButton btnCrear = findViewById(R.id.btn_crear_tarjeta);
        MaterialToolbar toolbar = findViewById(R.id.toolbar_nueva_tarjeta);

        toolbar.setNavigationOnClickListener(v -> finish());

        ajustarBarrasDelSistema();

        btnCrear.setOnClickListener(v -> crearTarjeta());
    }

    private void crearTarjeta() {
        tilNombreTarjeta.setError(null);

        String nombre = texto(tilNombreTarjeta);

        if (nombre.isEmpty()) {
            tilNombreTarjeta.setError(getString(R.string.nueva_tarjeta_nombre_vacio));
            return;
        }

        boolean exito = admin.crearTarjeta(usuarioId, nombre);

        if (exito) {
            Toast.makeText(this, R.string.nueva_tarjeta_exito, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, R.string.nueva_tarjeta_error, Toast.LENGTH_SHORT).show();
        }
    }

    private String texto(TextInputLayout til) {
        return (til != null && til.getEditText() != null) ? til.getEditText().getText().toString().trim() : "";
    }

    private void ajustarBarrasDelSistema() {
        View encabezado = findViewById(R.id.encabezado_nueva_tarjeta);
        int paddingSuperior = encabezado.getPaddingTop();

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.raiz_nueva_tarjeta), (v, insets) -> {
                    Insets barras = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                                    | WindowInsetsCompat.Type.ime());

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
    protected void onDestroy() { //onDestroy es un metodo que se ejecuta cuando se destruye la actividad.
        admin.close();
        super.onDestroy();
    }
}
