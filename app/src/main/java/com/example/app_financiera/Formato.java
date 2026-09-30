
package com.example.app_financiera;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
    public class Formato {

        private Formato() {
        }

        public static String dinero(long valor) {
            DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.US);
            simbolos.setGroupingSeparator('.');
            simbolos.setDecimalSeparator(',');

            DecimalFormat formato = new DecimalFormat("$ #,##0", simbolos);
            return formato.format(valor);
        }
    }
