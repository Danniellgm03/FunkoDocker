package org.docker.common.locale;

import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

/**
 * Esta clase se encarga de dar formato a las fechas, horas, monedas y números
 * @author daniel
 * @version 1.0
 */
public class MyLocale {
    /**
     * Locale para el idioma español
     * @see Locale
     */
    private final Locale localeEs = new Locale("es", "ES");

    /**
     * Formatea una fecha a un formato local
     * @param date
     * @return String - Fecha formateada
     */
    public static String toLocalDate(LocalDate date) {
        return date.format(
                DateTimeFormatter
                        .ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())
        );
    }

    /**
     * Formatea una fecha y hora a un formato local
     * @param dateTime
     * @return String - Fecha y hora formateada
     */
    public static String toLocalDateTime(LocalDateTime dateTime) {
        return dateTime.format(
                DateTimeFormatter
                        .ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(Locale.getDefault())
        );
    }

    /**
     * Formatea una moneda a un formato local
     * @param money
     * @return String - Moneda formateada
     */
    public static String toLocalMoney(double money) {
        return new String(NumberFormat.getCurrencyInstance(Locale.getDefault()).format(money).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Formatea un número a un formato local
     * @param number
     * @return String - Número formateado
     */
    public static String toLocalNumber(double number) {
        return NumberFormat.getNumberInstance(Locale.getDefault()).format(number);
    }
}
