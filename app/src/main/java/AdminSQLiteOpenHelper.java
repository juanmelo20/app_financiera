import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String NOMBRE_BD = "banco.db";
    private static final int VERSION_BD = 1;
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
        // Solo para desarrollo: borra todo y lo vuelve a crear
        db.execSQL("DROP TABLE IF EXISTS transacciones");
        db.execSQL("DROP TABLE IF EXISTS tarjetas");
        db.execSQL("DROP TABLE IF EXISTS usuarios");
        onCreate(db);
    }
}