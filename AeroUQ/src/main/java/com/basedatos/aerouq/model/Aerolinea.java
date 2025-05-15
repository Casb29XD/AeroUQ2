package com.basedatos.aerouq.model;

public class Aerolinea {
    private int idAerolinea;
    private String nombre;
    private int flota;
    private String contacto;

    // Constructor
    public Aerolinea(int idAerolinea, String nombre, int flota, String contacto) {
        this.idAerolinea = idAerolinea;
        this.nombre = nombre;
        this.flota = flota;
        this.contacto = contacto;
    }

    // Getters y setters
    public int getIdAerolinea() {
        return idAerolinea;
    }

    public void setIdAerolinea(int idAerolinea) {
        this.idAerolinea = idAerolinea;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getFlota() {
        return flota;
    }

    public void setFlota(int flota) {
        this.flota = flota;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }
}