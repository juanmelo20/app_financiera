package com.example.app_financiera;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class EnviarDinero extends AppCompatActivity {

    private TextInputLayout tilOrigen, tilDestino, tilMonto;
    private AutoCompleteTextView actOrigen, actDestino;
    private TextInputEditText etMonto;

    private AdminSQLiteOpenHelper admin;
    private List<Tarjeta> listaOrigen;
    private List<Tarjeta> listaDestino;

    private int posicionOrigenSeleccionada = -1;
    private int posicionDestinoSeleccionada = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT));
        setContentView(R.layout.activity_enviar_dinero);

        admin = new AdminSQLiteOpenHelper(this);
        long usuarioId = Sesion.usuarioId(this);

        tilOrigen = findViewById(R.id.til_origen);
        tilDestino = findViewById(R.id.til_destino);
        tilMonto = findViewById(R.id.til_monto);

        actOrigen = findViewById(R.id.act_origen);
        actDestino = findViewById(R.id.act_destino);
        etMonto = findViewById(R.id.et_monto);

        MaterialButton btnEnviar = findViewById(R.id.btn_enviar_dinero);
        MaterialToolbar toolbar = findViewById(R.id.toolbar_enviar_dinero);

        toolbar.setNavigationOnClickListener(v -> finish());

        ajustarBarrasDelSistema();

        cargarDesplegables(usuarioId);

        btnEnviar.setOnClickListener(v -> realizarTransferencia());
    }

    private void cargarDesplegables(long usuarioId) {
        // 1. Tarjetas de Origen (mis tarjetas)
        listaOrigen = admin.obtenerTarjetas(usuarioId);

        if (listaOrigen.isEmpty()) {
            Toast.makeText(this, "No tienes tarjetas registradas", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        List<String> nombresOrigen = new ArrayList<>();
        for (Tarjeta t : listaOrigen) {
            nombresOrigen.add(t.getNombre() + " (" + Formato.dinero(t.getSaldo()) + ")");
        }

        ArrayAdapter<String> adapterOrigen = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, nombresOrigen);
        actOrigen.setAdapter(adapterOrigen);

        posicionOrigenSeleccionada = 0;
        actOrigen.setText(nombresOrigen.get(0), false);
        actOrigen.setOnItemClickListener((parent, view, position, id) ->
                posicionOrigenSeleccionada = position);

        // 2. Tarjetas de Destino (todas las tarjetas registradas)
        listaDestino = admin.obtenerTodasLasTarjetas();

        List<String> nombresDestino = new ArrayList<>();
        for (Tarjeta t : listaDestino) {
            nombresDestino.add(t.getNombre() + " • " + t.getPanFormateado());
        }

        ArrayAdapter<String> adapterDestino = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, nombresDestino);
        actDestino.setAdapter(adapterDestino);

        if (!listaDestino.isEmpty()) {
            posicionDestinoSeleccionada = 0;
            actDestino.setText(nombresDestino.get(0), false);
        }

        actDestino.setOnItemClickListener((parent, view, position, id) ->
                posicionDestinoSeleccionada = position);
    }

    private void realizarTransferencia() {
        limpiarErrores();

        if (posicionOrigenSeleccionada < 0 || posicionOrigenSeleccionada >= listaOrigen.size()) {
            tilOrigen.setError(getString(R.string.enviar_error_origen_vacio));
            return;
        }

        if (posicionDestinoSeleccionada < 0 || posicionDestinoSeleccionada >= listaDestino.size()) {
            tilDestino.setError(getString(R.string.enviar_error_destino_invalido));
            return;
        }

        String montoTexto = texto(etMonto);
        long monto = 0;
        try {
            monto = Long.parseLong(montoTexto);
            if (monto <= 0) {
                tilMonto.setError(getString(R.string.enviar_error_monto_invalido));
                return;
            }
        } catch (NumberFormatException e) {
            tilMonto.setError(getString(R.string.enviar_error_monto_invalido));
            return;
        }

        Tarjeta tarjetaOrigen = listaOrigen.get(posicionOrigenSeleccionada);
        Tarjeta tarjetaDestino = listaDestino.get(posicionDestinoSeleccionada);

        int resultado = admin.transferirDineroPorIds(tarjetaOrigen.getId(), tarjetaDestino.getId(), monto);

        switch (resultado) {
            case AdminSQLiteOpenHelper.RESULTADO_EXITO:
                Toast.makeText(this, R.string.enviar_exito, Toast.LENGTH_SHORT).show();
                finish();
                break;
            case AdminSQLiteOpenHelper.RESULTADO_SALDO_INSUFICIENTE:
                tilMonto.setError(getString(R.string.enviar_error_saldo_insuficiente));
                break;
            case AdminSQLiteOpenHelper.RESULTADO_MISMA_TARJETA:
                tilDestino.setError(getString(R.string.enviar_error_misma_tarjeta));
                break;
            default:
                Toast.makeText(this, R.string.enviar_error_general, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private String texto(TextInputEditText campo) {
        return (campo != null && campo.getText() != null) ? campo.getText().toString().trim() : "";
    }

    private void limpiarErrores() {
        tilOrigen.setError(null);
        tilDestino.setError(null);
        tilMonto.setError(null);
    }

    private void ajustarBarrasDelSistema() {
        View encabezado = findViewById(R.id.encabezado_enviar_dinero);
        int paddingSuperior = encabezado.getPaddingTop();

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.raiz_enviar_dinero), (v, insets) -> {
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
