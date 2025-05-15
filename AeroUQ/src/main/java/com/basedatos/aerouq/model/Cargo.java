package com.basedatos.aerouq.model;

public class Cargo {
    private int idCargo;
    private String nombre;
    private String descripcion;
    private String fechaCreacion;

    public Cargo(int idCargo, String nombre, String descripcion, String fechaCreacion) {
        this.idCargo = idCargo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
    }

    public int getIdCargo() { return idCargo; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getFechaCreacion() { return fechaCreacion; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
