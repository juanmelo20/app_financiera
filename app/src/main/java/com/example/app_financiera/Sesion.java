package com.example.app_financiera;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Maneja la sesión con SharedPreferences.
 * Solo se guarda el id del usuario, nunca la contraseña.
 * Si no hay id guardado (-1), no hay sesión.
 */
public class Sesion {

    private static final String ARCHIVO = "sesion";
    private static final String CLAVE_USUARIO_ID = "usuario_id";

    private Sesion() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE);
    }

    public static void iniciar(Context context, long usuarioId) {
        prefs(context).edit()
                .putLong(CLAVE_USUARIO_ID, usuarioId)
                .apply();
    }

    public static boolean activa(Context context) {
        return usuarioId(context) != -1;
    }

    public static long usuarioId(Context context) {
        return prefs(context).getLong(CLAVE_USUARIO_ID, -1);
    }

    public static void cerrar(Context context) {
        prefs(context).edit().clear().apply();
    }
}