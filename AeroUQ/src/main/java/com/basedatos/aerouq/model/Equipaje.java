package com.basedatos.aerouq.model;

public class Equipaje {
    private int idMaleta;
    private String codigoDeBarras;
    private double peso;
    private int idVuelo;
    private String estado;
    private int idPasajero;

    // Constructor
    public Equipaje(int idMaleta, String codigoDeBarras, double peso, int idVuelo, String estado, int idPasajero) {
        this.idMaleta = idMaleta;
        this.codigoDeBarras = codigoDeBarras;
        this.peso = peso;
        this.idVuelo = idVuelo;
        this.estado = estado;
        this.idPasajero = idPasajero;
    }

    // Getters y setters
    public int getIdMaleta() {
        return idMaleta;
    }

    public void setIdMaleta(int idMaleta) {
        this.idMaleta = idMaleta;
    }

    public String getCodigoDeBarras() {
        return codigoDeBarras;
    }

    public void setCodigoDeBarras(String codigoDeBarras) {
        this.codigoDeBarras = codigoDeBarras;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public int getIdVuelo() {
        return idVuelo;
    }

    public void setIdVuelo(int idVuelo) {
        this.idVuelo = idVuelo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getIdPasajero() {
        return idPasajero;
    }

    public void setIdPasajero(int idPasajero) {
        this.idPasajero = idPasajero;
    }
}
