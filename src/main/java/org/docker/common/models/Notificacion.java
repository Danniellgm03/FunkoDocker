package org.docker.common.models;

/**
 * Clase Notificacion generica
 */
public class Notificacion<T> {
    private Tipo tipo;
    private T contenido;

    /**
     * Constructor
     * @param tipo
     * @param contenido
     */
    public Notificacion(Tipo tipo, T contenido) {
        this.tipo = tipo;
        this.contenido = contenido;
    }

    /**
     * Obtenemos el tipo de notificacion
     * @return Tipo
     */
    public Tipo getTipo() {
        return tipo;
    }

    /**
     * Establecemos el tipo de notificacion
     * @param tipo
     */
    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    /**
     * Obtenemos el contenido de la notificacion
     * @return T
     */
    public T getContenido() {
        return contenido;
    }

    /**
     * Establecemos el contenido de la notificacion
     * @param contenido
     */
    public void setContenido(T contenido) {
        this.contenido = contenido;
    }

    @Override
    public String toString() {
        return "Notificacion{" +
                "tipo=" + tipo +
                ", contenido=" + contenido +
                '}';
    }

    /**
     * Enumeracion de tipos de notificacion
     */
    public enum Tipo {
        NEW, UPDATED, DELETED
    }
}
