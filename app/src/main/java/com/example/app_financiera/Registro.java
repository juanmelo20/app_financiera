package com.example.app_financiera;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import android.text.TextWatcher;



import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class Registro extends AppCompatActivity {

    private TextInputLayout tilNombre, tilEmail, tilCelular, tilCedula, tilContrasena, tilConfirmar;
    private AdminSQLiteOpenHelper admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT));
        setContentView(R.layout.activity_registro);

        admin = new AdminSQLiteOpenHelper(this);
        tilNombre = findViewById(R.id.tilNombre);
        tilEmail = findViewById(R.id.tilEmail);
        tilCelular = findViewById(R.id.tilCelular);
        tilCedula = findViewById(R.id.tilCedula);
        tilContrasena = findViewById(R.id.tilContrasena);
        tilConfirmar = findViewById(R.id.tilConfirmar);

        validarAlEscribir(tilNombre);
        validarAlEscribir(tilCelular);
        validarAlEscribir(tilCedula);
        validarAlEscribir(tilEmail);
        validarAlEscribir(tilContrasena);
        validarAlEscribir(tilConfirmar);


        // Encabezado detrás de la barra de estado; el teclado nunca tapa el formulario
        View encabezado = findViewById(R.id.encabezado);
        int paddingTopInicial = encabezado.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets teclado = insets.getInsets(WindowInsetsCompat.Type.ime());
            encabezado.setPadding(encabezado.getPaddingLeft(), paddingTopInicial + barras.top,
                    encabezado.getPaddingRight(), encabezado.getPaddingBottom());
            v.setPadding(barras.left, 0, barras.right, Math.max(barras.bottom, teclado.bottom));
            return insets;
        });


        findViewById(R.id.btnCrearCuenta).setOnClickListener(v -> registrar());
    }

    private void registrar() {
        String nombre = texto(tilNombre);
        String email = texto(tilEmail).toLowerCase(Locale.ROOT);
        String celular = texto(tilCelular);
        String cedula = texto(tilCedula);
        String contrasena = tilContrasena.getEditText().getText().toString();
        String confirmar = tilConfirmar.getEditText().getText().toString();

        limpiarErrores();
        boolean valido = true;

        if (!validarNombre(nombre)) valido = false;
        if (!validarEmail(email)) valido = false;
        if (!validarCelular(celular)) valido=false;
        if (!validarCedula(cedula)) valido=false;
        if (!validarContrasena(contrasena)) valido = false;
        if (!validarConfirmacion()) valido = false;
        if (!valido) return;
        if (admin.existeEmail(email)) {
            tilEmail.setError(getString(R.string.error_email_existe));
            return;
        }
        if (admin.existeCedula(cedula)) {
            tilCedula.setError(getString(R.string.error_cedula_existe));
            return;
        }
        long id = admin.registrarUsuario(nombre, email, celular, cedula, Seguridad.hash(contrasena));
        if (id == -1) {
            Toast.makeText(this, R.string.msg_error_registro, Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, R.string.msg_cuenta_creada, Toast.LENGTH_LONG).show();
        Sesion.iniciar(this, id);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);


    }

    private boolean validarNombre(String nombre){
        if (nombre.isEmpty()) {
            tilNombre.setError(getString(R.string.error_obligatorio));
            return false;
        }
        if (nombre.length() < 3) {
            tilNombre.setError(getString(R.string.error_nombre_longitud_min));
            return false;
        }
        if (nombre.length() > 20) {
            tilNombre.setError(getString(R.string.error_nombre_longitud_max));
            return false;
        }
        if (nombre.matches(".*\\d.*")) {
            tilNombre.setError(getString(R.string.error_nombre_numeros));
            return false;
        }
        if (!nombre.matches("[\\p{L} ]+")) {
            tilNombre.setError(getString(R.string.error_nombre_caracteres_especiales));
            return false;
        }
        tilNombre.setError(null);
        return true;
    }
    private boolean validarCelular(String celular) {
        if (!celular.matches("\\d{10}")) {
            tilCelular.setError(getString(R.string.error_celular));
            return false;
        }
        tilCelular.setError(null);
        return true;
    }

    private boolean validarCedula(String cedula) {
        if (!cedula.matches("\\d{7,10}")) {
            tilCedula.setError(getString(R.string.error_cedula));
            return false;
        }
        tilCedula.setError(null);
        return true;
    }
    private boolean validarEmail(String email) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_email));
            return false;
        }
        tilEmail.setError(null);
        return true;
    }
    private boolean validarContrasena(String contrasena) {
        if (contrasena.length() < 8) {
            tilContrasena.setError(getString(R.string.error_contrasena_Longitud));
            return false;
        }
        if (!contrasena.matches(".*[A-Z].*")) {
            tilContrasena.setError(getString(R.string.error_contrasena_mayuscula));
            return false;
        }
        if (!contrasena.matches(".*[a-z].*")) {
            tilContrasena.setError(getString(R.string.error_contrasena_minuscula));
            return false;
        }
        if (!contrasena.matches(".*\\d.*")) {
            tilContrasena.setError(getString(R.string.error_contrasena_numero));
            return false;
        }
        if (!contrasena.matches(".*[^a-zA-Z0-9].*")) {
            tilContrasena.setError(getString(R.string.error_contrasena_caracteres_especiales));
            return false;
        }
        tilContrasena.setError(null);
        return true;
    }

    private boolean validarConfirmacion() {
        String contrasena = tilContrasena.getEditText().getText().toString();
        String confirmar = tilConfirmar.getEditText().getText().toString();

        if (!confirmar.equals(contrasena)) {
            tilConfirmar.setError(getString(R.string.error_contrasena_confirmar));
            return false;
        }
        tilConfirmar.setError(null);
        return true;
    }



    private String texto(TextInputLayout til) {
        return til.getEditText().getText().toString().trim(); //.trim() elimina espacios en blanco
    }

    private void limpiarErrores() {
        TextInputLayout[] campos = {tilNombre, tilEmail, tilCelular, tilCedula, tilContrasena, tilConfirmar};
        for (TextInputLayout til : campos) til.setError(null);
    }

    private void validarAlEscribir(TextInputLayout til){
        til.getEditText().addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String texto = s.toString();

                // Campo vacío: no se muestra "obligatorio" mientras escribe o borra
                if (texto.trim().isEmpty()) {
                    til.setError(null);
                    return;
                }

                if (til == tilNombre) {
                    validarNombre(texto.trim());
                } else if (til == tilCelular) {
                    validarCelular(texto.trim());
                } else if (til == tilCedula) {
                    validarCedula(texto.trim());
                } else if (til == tilContrasena) {
                    validarContrasena(texto);// Si ya escribió la confirmación, se vuelve a comparar
                    if (!tilConfirmar.getEditText().getText().toString().isEmpty()) {
                        validarConfirmacion();
                    }
                } else if (til == tilConfirmar) {
                    validarConfirmacion();
                } else if (til == tilEmail) {
                validarEmail(texto.trim());
                }
            }
        });
    }
}