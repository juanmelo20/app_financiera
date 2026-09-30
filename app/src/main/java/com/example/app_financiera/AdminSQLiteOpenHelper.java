package com.example.app_financiera;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;


public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String NOMBRE_BD = "banco.db";
    private static final int VERSION_BD = 2;
    public AdminSQLiteOpenHelper(Context context) {
        super(context, NOMBRE_BD, null, VERSION_BD);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL, " +
                "email TEXT NOT NULL UNIQUE, " +
                "celular TEXT NOT NULL, " +
                "cedula TEXT NOT NULL UNIQUE, " +
                "contrasena TEXT NOT NULL)");

        db.execSQL("CREATE TABLE tarjetas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "usuario_id INTEGER NOT NULL REFERENCES usuarios(id), " +
                "nombre TEXT NOT NULL, " +
                "pan TEXT NOT NULL UNIQUE, " +
                "expiracion TEXT NOT NULL, " +
                "cvv TEXT NOT NULL, " +
                "saldo DOUBLE NOT NULL CHECK (saldo >= 0), " +
                "es_principal INTEGER NOT NULL DEFAULT 0, " +
                "ultima_revision TEXT)");

        db.execSQL("CREATE TABLE transacciones (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "tarjeta_origen_id INTEGER NOT NULL REFERENCES tarjetas(id), " +
                "tarjeta_destino_id INTEGER NOT NULL REFERENCES tarjetas(id), " +
                "monto INTEGER NOT NULL CHECK (monto > 0), " +
                "fecha TEXT NOT NULL)");

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS transacciones");
        db.execSQL("DROP TABLE IF EXISTS tarjetas");
        db.execSQL("DROP TABLE IF EXISTS usuarios");
        onCreate(db);
    }

    public static final long SALDO_PRINCIPAL = 5_000_000L;

    public boolean existeEmail(String email) {
        return existe("SELECT 1 FROM usuarios WHERE email = ?", email);
    }

    public boolean existeCedula(String cedula) {
        return existe("SELECT 1 FROM usuarios WHERE cedula = ?", cedula);
    }

    private boolean existe(String sql, String valor) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery(sql, new String[]{valor})) { //rawQuery es un metodo que nos permite ejecutar una consulta SQL.
            return c.moveToFirst();
        }
    }

    // Crea el usuario y su tarjeta Principal en una sola transacción.
    //  -1 si algo falló.
    public long registrarUsuario(String nombre, String email, String celular,
                                 String cedula, String contrasena) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues usuario = new ContentValues(); //ContentValues es una clase que nos permite almacenar datos en un mapa clave-valor.
            usuario.put("nombre", nombre); //put es un metodo que nos permite agregar datos al mapa.
            usuario.put("email", email);
            usuario.put("celular", celular);
            usuario.put("cedula", cedula);
            usuario.put("contrasena", contrasena);
            long usuarioId = db.insertOrThrow("usuarios", null, usuario);

            ContentValues tarjeta = new ContentValues();
            tarjeta.put("usuario_id", usuarioId);
            tarjeta.put("nombre", "Principal");
            tarjeta.put("pan", GenerarTarjetas.generarPan());
            tarjeta.put("expiracion", GenerarTarjetas.generarExpiracion());
            tarjeta.put("cvv", GenerarTarjetas.generarCvv());
            tarjeta.put("saldo", SALDO_PRINCIPAL);
            tarjeta.put("es_principal", 1);
            tarjeta.put("ultima_revision", hoy());
            db.insertOrThrow("tarjetas", null, tarjeta);

            db.setTransactionSuccessful();
            return usuarioId;
        } catch (SQLException e) {
            return -1;
        } finally {
            db.endTransaction();
        }
    }
    /**
     * Devuelve el ID del usuario si email y hash coinciden, o -1 si no.
     * COLLATE NOCASE: el correo se compara sin distinguir mayúsculas.
     */
    public long autenticar(String email, String contrasenaHash) {

        SQLiteDatabase db = getReadableDatabase();

        try (Cursor c = db.rawQuery(
                "SELECT id FROM usuarios " +
                        "WHERE email = ? COLLATE NOCASE AND contrasena = ?",
                new String[]{email, contrasenaHash}
        )) {
            return c.moveToFirst() ? c.getLong(0) : -1;
        }
    }
    //obtener usuario
    public String obtenerNombreUsuario(long usuarioId) {

        SQLiteDatabase db = getReadableDatabase();

        try (Cursor c = db.rawQuery(
                "SELECT nombre FROM usuarios WHERE id = ?",
                new String[]{String.valueOf(usuarioId)}
        )) {
            return c.moveToFirst() ? c.getString(0) : null;
        }
    }

    //Obtener saldo usuario
    public long obtenerSaldoTotal(long usuarioId) {

        SQLiteDatabase db = getReadableDatabase(); //getReadableDatabase es un metodo que nos permite obtener una base de datos de solo lectura.

        try (Cursor c = db.rawQuery( //Cursor es una clase que nos permite recorrer los resultados de una consulta SQL.
                                    //rawQuery es un metodo que nos permite ejecutar una consulta SQL.
                "SELECT COALESCE(SUM(saldo), 0) FROM tarjetas WHERE usuario_id = ?",
                new String[]{String.valueOf(usuarioId)}
        )) {
            return c.moveToFirst() ? c.getLong(0) : 0;
        }
    }
    // Tarjetas del usuario: la Principal primero, luego en orden de creación. Si no hay ninguna, devuelve una lista vacía.
    public List<Tarjeta> obtenerTarjetas(long usuarioId) {

        List<Tarjeta> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase(); //getReadableDatabase es un metodo que nos permite obtener una base de datos de solo lectura.

        try (Cursor c = db.rawQuery(
                "SELECT id, nombre, pan, expiracion, cvv, saldo, es_principal " +
                        "FROM tarjetas WHERE usuario_id = ? " +
                        "ORDER BY es_principal DESC, id ASC",
                new String[]{String.valueOf(usuarioId)}
        )) {
            while (c.moveToNext()) { //moveToNext es un metodo que nos permite recorrer los resultados de una consulta SQL.
                lista.add(new Tarjeta( //new Tarjeta es un metodo que nos permite crear un objeto de la clase Tarjeta.
                        c.getLong(0),
                        c.getString(1),
                        c.getString(2),
                        c.getString(3),
                        c.getString(4),
                        c.getLong(5),
                        c.getInt(6) == 1
                ));
            }
        }
        return lista;
    }

    // Devuelve todas las tarjetas registradas en el banco (para el desplegable de destino)
    public List<Tarjeta> obtenerTodasLasTarjetas() {
        List<Tarjeta> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT id, nombre, pan, expiracion, cvv, saldo, es_principal " +
                        "FROM tarjetas ORDER BY id ASC",
                null
        )) {
            while (c.moveToNext()) {
                lista.add(new Tarjeta(
                        c.getLong(0),
                        c.getString(1),
                        c.getString(2),
                        c.getString(3),
                        c.getString(4),
                        c.getLong(5),
                        c.getInt(6) == 1
                ));
            }
        }
        return lista;
    }

    // Crear una nueva tarjeta adicional para un usuario
    public boolean crearTarjeta(long usuarioId, String nombreTarjeta) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues tarjeta = new ContentValues();
        tarjeta.put("usuario_id", usuarioId);
        tarjeta.put("nombre", nombreTarjeta);
        tarjeta.put("pan", GenerarTarjetas.generarPan());
        tarjeta.put("expiracion", GenerarTarjetas.generarExpiracion());
        tarjeta.put("cvv", GenerarTarjetas.generarCvv());
        tarjeta.put("saldo", 1000000);
        tarjeta.put("es_principal", 0);
        tarjeta.put("ultima_revision", hoy());
        long id = db.insert("tarjetas", null, tarjeta);
        return id != -1;
    }

    public static final int RESULTADO_EXITO = 0;
    public static final int RESULTADO_SALDO_INSUFICIENTE = 1;
    public static final int RESULTADO_TARJETA_DESTINO_NO_EXISTE = 2;
    public static final int RESULTADO_MISMA_TARJETA = 3;
    public static final int RESULTADO_ERROR = 4;

    public int transferirDinero(long tarjetaOrigenId, String panDestino, long monto) {
        if (monto <= 0) return RESULTADO_ERROR;

        SQLiteDatabase db = getWritableDatabase();

        // 1. Verificar tarjeta destino
        long tarjetaDestinoId = -1;
        try (Cursor c = db.rawQuery("SELECT id FROM tarjetas WHERE pan = ?", new String[]{panDestino})) {
            if (c.moveToFirst()) {
                tarjetaDestinoId = c.getLong(0);
            }
        }

        if (tarjetaDestinoId == -1) {
            return RESULTADO_TARJETA_DESTINO_NO_EXISTE;
        }

        if (tarjetaOrigenId == tarjetaDestinoId) {
            return RESULTADO_MISMA_TARJETA;
        }

        // 2. Transacción atómica
        db.beginTransaction();
        try {
            // Verificar saldo
            long saldoOrigen = 0;
            try (Cursor c = db.rawQuery("SELECT saldo FROM tarjetas WHERE id = ?", new String[]{String.valueOf(tarjetaOrigenId)})) {
                if (c.moveToFirst()) {
                    saldoOrigen = c.getLong(0);
                } else {
                    return RESULTADO_ERROR;
                }
            }

            if (saldoOrigen < monto) {
                return RESULTADO_SALDO_INSUFICIENTE;
            }

            // Restar a origen
            db.execSQL("UPDATE tarjetas SET saldo = saldo - ? WHERE id = ?",
                    new Object[]{monto, tarjetaOrigenId});

            // Sumar a destino
            db.execSQL("UPDATE tarjetas SET saldo = saldo + ? WHERE id = ?",
                    new Object[]{monto, tarjetaDestinoId});

            // Registrar transacción
            ContentValues transaccion = new ContentValues();
            transaccion.put("tarjeta_origen_id", tarjetaOrigenId);
            transaccion.put("tarjeta_destino_id", tarjetaDestinoId);
            transaccion.put("monto", monto);
            transaccion.put("fecha", hoy());
            db.insertOrThrow("transacciones", null, transaccion);

            db.setTransactionSuccessful();
            return RESULTADO_EXITO;
        } catch (Exception e) {
            return RESULTADO_ERROR;
        } finally {
            db.endTransaction();
        }
    }

    public int transferirDineroPorIds(long tarjetaOrigenId, long tarjetaDestinoId, long monto) {
        if (monto <= 0) return RESULTADO_ERROR;
        if (tarjetaOrigenId == tarjetaDestinoId) return RESULTADO_MISMA_TARJETA;

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            long saldoOrigen = 0;
            try (Cursor c = db.rawQuery("SELECT saldo FROM tarjetas WHERE id = ?", new String[]{String.valueOf(tarjetaOrigenId)})) {
                if (c.moveToFirst()) {
                    saldoOrigen = c.getLong(0);
                } else {
                    return RESULTADO_ERROR;
                }
            }

            if (saldoOrigen < monto) {
                return RESULTADO_SALDO_INSUFICIENTE;
            }

            db.execSQL("UPDATE tarjetas SET saldo = saldo - ? WHERE id = ?",
                    new Object[]{monto, tarjetaOrigenId});

            db.execSQL("UPDATE tarjetas SET saldo = saldo + ? WHERE id = ?",
                    new Object[]{monto, tarjetaDestinoId});

            ContentValues transaccion = new ContentValues();
            transaccion.put("tarjeta_origen_id", tarjetaOrigenId);
            transaccion.put("tarjeta_destino_id", tarjetaDestinoId);
            transaccion.put("monto", monto);
            transaccion.put("fecha", hoy());
            db.insertOrThrow("transacciones", null, transaccion);

            db.setTransactionSuccessful();
            return RESULTADO_EXITO;
        } catch (Exception e) {
            return RESULTADO_ERROR;
        } finally {
            db.endTransaction();
        }
    }

    // Devuelve todas las transacciones donde el usuario sea origen o destino, incluyendo el nombre del dueño
    public List<Transaccion> obtenerTransacciones(long usuarioId) {
        List<Transaccion> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String sql = "SELECT t.id, " +
                "tor.nombre AS origen_tarjeta, " +
                "uor.nombre AS origen_usuario, " +
                "tdes.nombre AS destino_tarjeta, " +
                "udes.nombre AS destino_usuario, " +
                "t.monto, t.fecha, " +
                "tor.usuario_id AS origen_usuario_id " +
                "FROM transacciones t " +
                "INNER JOIN tarjetas tor ON t.tarjeta_origen_id = tor.id " +
                "INNER JOIN usuarios uor ON tor.usuario_id = uor.id " +
                "INNER JOIN tarjetas tdes ON t.tarjeta_destino_id = tdes.id " +
                "INNER JOIN usuarios udes ON tdes.usuario_id = udes.id " +
                "WHERE tor.usuario_id = ? OR tdes.usuario_id = ? " +
                "ORDER BY t.id DESC";

        String idStr = String.valueOf(usuarioId);
        try (Cursor c = db.rawQuery(sql, new String[]{idStr, idStr})) {
            while (c.moveToNext()) {
                boolean esSalida = (c.getLong(7) == usuarioId);
                lista.add(new Transaccion(
                        c.getLong(0),
                        c.getString(1),
                        c.getString(2),
                        c.getString(3),
                        c.getString(4),
                        c.getLong(5),
                        c.getString(6),
                        esSalida
                ));
            }
        }
        return lista;
    }

    //fecha actual
    private static String hoy() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }
}