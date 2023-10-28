package org.docker.common.models;

import org.docker.common.enums.Modelo;
import org.docker.common.locale.MyLocale;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Clase Funko
 * @see Modelo
 * @see MyLocale
 */
public class Funko {
    private Integer id;
    private UUID COD;
    private Long myId;
    private String nombre;
    private Modelo modelo;
    private double precio;
    private LocalDate fecha;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    /**
     * Constructor
     * @param id
     * @param COD
     * @param myId
     * @param nombre
     * @param modelo
     * @param precio
     * @param fecha
     * @param created_at
     * @param updated_at
     */
    public Funko(Integer id, UUID COD, Long myId, String nombre, Modelo modelo, double precio, LocalDate fecha, LocalDateTime created_at, LocalDateTime updated_at) {
        this.id = id;
        this.COD = COD;
        this.myId = myId;
        this.nombre = nombre;
        this.modelo = modelo;
        this.precio = precio;
        this.fecha = fecha;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public Integer getId() {
        return id;
    }

    public UUID getCOD() {
        return COD;
    }

    public Long getMyId() {
        return myId;
    }

    public String getNombre() {
        return nombre;
    }

    public Modelo getModelo() {
        return modelo;
    }

    public double getPrecio() {
        return precio;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setCOD(UUID COD) {
        this.COD = COD;
    }

    public void setMyId(Long myId) {
        this.myId = myId;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setModelo(Modelo modelo) {
        this.modelo = modelo;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    @Override
    public String toString() {
        return "Funko{" +
                "id=" + id +
                ", COD=" + COD +
                ", myId=" + myId +
                ", nombre='" + nombre + '\'' +
                ", modelo=" + modelo +
                ", precio=" + MyLocale.toLocalMoney(precio) +
                ", fecha=" + MyLocale.toLocalDate(fecha) +
                ", created_at=" + MyLocale.toLocalDateTime(created_at) +
                ", updated_at=" + MyLocale.toLocalDateTime(updated_at) +
                '}';
    }
}
