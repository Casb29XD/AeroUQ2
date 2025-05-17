package com.basedatos.aerouq.model;

public class MantenimientoAeronave {
    private int idMantenimiento;
    private int idAeronave;
    private String matricula;
    private String descripcion;
    private String fechaMantenimiento;
    private String estado;

    public MantenimientoAeronave(int idMantenimiento, int idAeronave, String matricula, String descripcion, String fechaMantenimiento, String estado) {
        this.idMantenimiento = idMantenimiento;
        this.idAeronave = idAeronave;
        this.matricula = matricula;
        this.descripcion = descripcion;
        this.fechaMantenimiento = fechaMantenimiento;
        this.estado = estado;
    }

    public int getIdMantenimiento() { return idMantenimiento; }
    public int getIdAeronave() { return idAeronave; }
    public String getMatricula() { return matricula; }
    public String getDescripcion() { return descripcion; }
    public String getFechaMantenimiento() { return fechaMantenimiento; }
    public String getEstado() { return estado; }

    public void setIdMantenimiento(int idMantenimiento) { this.idMantenimiento = idMantenimiento; }
    public void setIdAeronave(int idAeronave) { this.idAeronave = idAeronave; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setFechaMantenimiento(String fechaMantenimiento) { this.fechaMantenimiento = fechaMantenimiento; }
    public void setEstado(String estado) { this.estado = estado; }
}