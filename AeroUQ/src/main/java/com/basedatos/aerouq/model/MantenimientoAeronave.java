package com.basedatos.aerouq.model;

public class MantenimientoAeronave {
    private int idMantenimiento;
    private int idAeronave;
    private String descripcion;
    private String fechaMantenimiento;
    private String estado;

    public MantenimientoAeronave(int idMantenimiento, int idAeronave, String descripcion, String fechaMantenimiento, String estado) {
        this.idMantenimiento = idMantenimiento;
        this.idAeronave = idAeronave;
        this.descripcion = descripcion;
        this.fechaMantenimiento = fechaMantenimiento;
        this.estado = estado;
    }

    public int getIdMantenimiento() { return idMantenimiento; }
    public int getIdAeronave() { return idAeronave; }
    public String getDescripcion() { return descripcion; }
    public String getFechaMantenimiento() { return fechaMantenimiento; }
    public String getEstado() { return estado; }

    public void setIdAeronave(int idAeronave) { this.idAeronave = idAeronave; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setFechaMantenimiento(String fechaMantenimiento) { this.fechaMantenimiento = fechaMantenimiento; }
    public void setEstado(String estado) { this.estado = estado; }
}