package com.example.app_financiera;

public class Transaccion {

    private final long id;
    private final String origenTarjeta;
    private final String origenUsuario;
    private final String destinoTarjeta;
    private final String destinoUsuario;
    private final long monto;
    private final String fecha;
    private final boolean esSalida;

    public Transaccion(long id, String origenTarjeta, String origenUsuario,
                       String destinoTarjeta, String destinoUsuario,
                       long monto, String fecha, boolean esSalida) {
        this.id = id;
        this.origenTarjeta = origenTarjeta;
        this.origenUsuario = origenUsuario;
        this.destinoTarjeta = destinoTarjeta;
        this.destinoUsuario = destinoUsuario;
        this.monto = monto;
        this.fecha = fecha;
        this.esSalida = esSalida;
    }

    public long getId() { return id; }
    public String getOrigenTarjeta() { return origenTarjeta; }
    public String getOrigenUsuario() { return origenUsuario; }
    public String getDestinoTarjeta() { return destinoTarjeta; }
    public String getDestinoUsuario() { return destinoUsuario; }
    public long getMonto() { return monto; }
    public String getFecha() { return fecha; }
    public boolean isSalida() { return esSalida; }

    public String getNombreOrigenCompleto() {
        return origenUsuario + " (" + origenTarjeta + ")";
    }

    public String getNombreDestinoCompleto() {
        return destinoUsuario + " (" + destinoTarjeta + ")";
    }
}
