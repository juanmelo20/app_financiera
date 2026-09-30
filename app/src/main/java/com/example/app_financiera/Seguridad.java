package com.example.app_financiera;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Seguridad {

    public static String hash(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256"); //SHA-256 es para contraseñas, utiliza para encriptar
            byte[] bytes = md.digest(texto.getBytes(StandardCharsets.UTF_8)); //Convierte el texto a bytes
            StringBuilder sb = new StringBuilder(); //Crea un StringBuilder para construir la cadena de caracteres
            for (byte b : bytes) {
                sb.append(String.format("%02x", b)); //Agrega el byte en formato hexadecimal
            }
            return sb.toString(); //Devuelve la cadena de caracteres en formato hexadecimal
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e); //Si no se encuentra el algoritmo, lanza una excepción
        }
    }
}