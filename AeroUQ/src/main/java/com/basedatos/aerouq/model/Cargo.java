package com.basedatos.aerouq.model;

public class Cargo {
    private int idCargo;
    private String nombreCargo;
    private String descripcion;
    private String fechaCreacion;

    public Cargo(int idCargo, String nombreCargo, String descripcion, String fechaCreacion) {
        this.idCargo = idCargo;
        this.nombreCargo = nombreCargo;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
    }

    public int getIdCargo() { return idCargo; }
    public String getNombreCargo() { return nombreCargo; }
    public String getDescripcion() { return descripcion; }
    public String getFechaCreacion() { return fechaCreacion; }

    public void setNombreCargo(String nombreCargo) { this.nombreCargo = nombreCargo; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}