package com.basedatos.aerouq.model;

public class Empleado {
    private int idEmpleado;
    private String documento;
    private String nombre;
    private String apellido;
    private int idCargo;

    public Empleado(int idEmpleado, String documento, String nombre, String apellido, int idCargo) {
        this.idEmpleado = idEmpleado;
        this.documento = documento;
        this.nombre = nombre;
        this.apellido = apellido;
        this.idCargo = idCargo;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public int getIdCargo() {
        return idCargo;
    }

    public void setIdCargo(int idCargo) {
        this.idCargo = idCargo;
    }
}