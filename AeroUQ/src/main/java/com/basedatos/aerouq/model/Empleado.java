package com.basedatos.aerouq.model;

public class Empleado {
    private int idEmpleado;
    private String documento;
    private String nombre;
    private String apellido;
    private int idCargo;
    private String nombreCargo; // Solo para mostrar

    public Empleado(int idEmpleado, String documento, String nombre, String apellido, int idCargo, String nombreCargo) {
        this.idEmpleado = idEmpleado;
        this.documento = documento;
        this.nombre = nombre;
        this.apellido = apellido;
        this.idCargo = idCargo;
        this.nombreCargo = nombreCargo;
    }

    // Constructor sin nombreCargo
    public Empleado(int idEmpleado, String documento, String nombre, String apellido, int idCargo) {
        this(idEmpleado, documento, nombre, apellido, idCargo, null);
    }

    public int getIdEmpleado() { return idEmpleado; }
    public String getDocumento() { return documento; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public int getIdCargo() { return idCargo; }
    public String getNombreCargo() { return nombreCargo; }

    public void setIdEmpleado(int idEmpleado) { this.idEmpleado = idEmpleado; }
    public void setDocumento(String documento) { this.documento = documento; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public void setIdCargo(int idCargo) { this.idCargo = idCargo; }
    public void setNombreCargo(String nombreCargo) { this.nombreCargo = nombreCargo; }
}