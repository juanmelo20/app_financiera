package com.example.app_financiera;

// Clase que representa la tabla tarjetas.
public class Tarjeta {

    private final long id;
    private final String nombre;
    private final String pan;
    private final String expiracion;
    private final String cvv;
    private final long saldo;
    private final boolean principal;

    public Tarjeta(long id, String nombre, String pan, String expiracion,
                   String cvv, long saldo, boolean principal) {
        this.id = id;
        this.nombre = nombre;
        this.pan = pan;
        this.expiracion = expiracion;
        this.cvv = cvv;
        this.saldo = saldo;
        this.principal = principal;
    }

    public long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getPan() { return pan; }
    public String getExpiracion() { return expiracion; }
    public String getCvv() { return cvv; }
    public long getSaldo() { return saldo; }
    public boolean isPrincipal() { return principal; }

    // Muestra los últimos 4 dígitos •••• •••• •••• 1234
    public String getPanOculto() {
        if (pan != null && pan.length() >= 4) {
            return "•••• •••• •••• " + pan.substring(pan.length() - 4);
        }
        return pan;
    }

    // Formato de 16 dígitos agrupados de a 4: 1234 5678 1234 5678
    public String getPanFormateado() {
        if (pan != null && pan.length() == 16) {
            return pan.replaceAll("(.{4})", "$1 ").trim();
        }
        return pan;
    }
}
