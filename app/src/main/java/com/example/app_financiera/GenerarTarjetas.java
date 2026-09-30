package com.example.app_financiera;

import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Locale;

public class GenerarTarjetas {

    private static final SecureRandom random = new SecureRandom();

    // 16 dígitos
    public static String generarPan() {
        int[] d = new int[16];
        d[0] = 1 + random.nextInt(9);
        for (int i = 1; i < 15; i++) d[i] = random.nextInt(10);
        d[15] = digitoLuhn(d); // Dígito de Luhn, el digito de luhn es el último dígito, sirve para validar el número y que no se repita
        StringBuilder sb = new StringBuilder(); //
        for (int digito : d) sb.append(digito); // Convierte el array de enteros a un string
        return sb.toString();
    }

    private static int digitoLuhn(int[] d) {
        int suma = 0;
        for (int i = 14; i >= 0; i--) {
            int valor = d[i];
            if ((14 - i) % 2 == 0) {
                valor *= 2;
                if (valor > 9) valor -= 9;
            }
            suma += valor;
        }
        return (10 - (suma % 10)) % 10;
    }

    // MM/AA, entre 3 y 5 años desde hoy
    public static String generarExpiracion() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 3 + random.nextInt(3));
        int mes = 1 + random.nextInt(12);
        return String.format(Locale.US, "%02d/%02d", mes, cal.get(Calendar.YEAR) % 100);
    }

    public static String generarCvv() {
        return String.format(Locale.US, "%03d", random.nextInt(1000));
    }
}