package com.example.app_financiera;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class Login extends AppCompatActivity {

    private TextInputLayout tilEmail, tilContrasena;
    private TextInputEditText etEmail, etContrasena;
    private AdminSQLiteOpenHelper admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT));
        setContentView(R.layout.activity_login);

        admin = new AdminSQLiteOpenHelper(this);

        tilEmail = findViewById(R.id.til_email);
        tilContrasena = findViewById(R.id.til_contrasena);
        etEmail = findViewById(R.id.et_email);
        etContrasena = findViewById(R.id.et_contrasena);
        MaterialButton btnIniciar = findViewById(R.id.btn_iniciar_sesion);
        MaterialButton btnRegistro = findViewById(R.id.btn_ir_registro);

        ajustarBarrasDelSistema();

        btnIniciar.setOnClickListener(v -> iniciarSesion());

        btnRegistro.setOnClickListener(v ->
                startActivity(new Intent(Login.this, Registro.class)));
    }

    private void iniciarSesion() {
        tilEmail.setError(null);
        tilContrasena.setError(null);

        String email = texto(etEmail).trim();
        String contrasena = texto(etContrasena);

        boolean valido = true;

        if (email.isEmpty()) {
            tilEmail.setError(getString(R.string.login_error_email_vacio));
            valido = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.login_error_email_invalido));
            valido = false;
        }

        if (contrasena.isEmpty()) {
            tilContrasena.setError(getString(R.string.login_error_contrasena_vacia));
            valido = false;
        }

        if (!valido) {
            return;
        }

        // Mismo hash que se usó en Registro
        long usuarioId = admin.autenticar(email, Seguridad.hash(contrasena));

        if (usuarioId == -1) {
            Toast.makeText(this, R.string.login_error_credenciales,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Sesion.iniciar(this, usuarioId);

        Toast.makeText(this, R.string.login_exitoso, Toast.LENGTH_SHORT).show();

        // CLEAR_TASK: al pulsar atrás en Home no se vuelve al Login
        Intent intent = new Intent(Login.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private static String texto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString();
    }

    // Con EdgeToEdge el contenido se dibuja debajo de la barra de estado.
    // Esto empuja el encabezado hacia abajo y deja espacio para el teclado.
    private void ajustarBarrasDelSistema() {
        View encabezado = findViewById(R.id.encabezado_login);
        int paddingSuperior = encabezado.getPaddingTop();

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.raiz_login), (v, insets) -> {
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
    protected void onDestroy() {
        admin.close();
        super.onDestroy();
    }
}