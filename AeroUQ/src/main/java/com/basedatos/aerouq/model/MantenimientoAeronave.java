package com.basedatos.aerouq.model;

import java.util.Date;

public class MantenimientoAeronave {
    private int idMantenimiento;
    private int idAeronave;
    private String descripcion;
    private Date fechaMantenimiento;
    private String estado;

    // Constructor
    public MantenimientoAeronave(int idMantenimiento, int idAeronave, String descripcion, Date fechaMantenimiento, String estado) {
        this.idMantenimiento = idMantenimiento;
        this.idAeronave = idAeronave;
        this.descripcion = descripcion;
        this.fechaMantenimiento = fechaMantenimiento;
        this.estado = estado;
    }

    // Getters y setters
    public int getIdMantenimiento() {
        return idMantenimiento;
    }

    public void setIdMantenimiento(int idMantenimiento) {
        this.idMantenimiento = idMantenimiento;
    }

    public int getIdAeronave() {
        return idAeronave;
    }

    public void setIdAeronave(int idAeronave) {
        this.idAeronave = idAeronave;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Date getFechaMantenimiento() {
        return fechaMantenimiento;
    }

    public void setFechaMantenimiento(Date fechaMantenimiento) {
        this.fechaMantenimiento = fechaMantenimiento;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}