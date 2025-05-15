package com.basedatos.aerouq.model;

import java.util.Date;

public class Vuelo {
    private int idVuelo;
    private String numeroVuelo;
    private int idAerolinea;
    private String origen;
    private String destino;
    private Date fechaHoraSalida;
    private Date fechaHoraLlegada;
    private String estadoVuelo;
    private int idPuerta;

    // Constructor
    public Vuelo(int idVuelo, String numeroVuelo, int idAerolinea, String origen, String destino,
                 Date fechaHoraSalida, Date fechaHoraLlegada, String estadoVuelo, int idPuerta) {
        this.idVuelo = idVuelo;
        this.numeroVuelo = numeroVuelo;
        this.idAerolinea = idAerolinea;
        this.origen = origen;
        this.destino = destino;
        this.fechaHoraSalida = fechaHoraSalida;
        this.fechaHoraLlegada = fechaHoraLlegada;
        this.estadoVuelo = estadoVuelo;
        this.idPuerta = idPuerta;
    }

    // Getters y setters
    public int getIdVuelo() {
        return idVuelo;
    }

    public void setIdVuelo(int idVuelo) {
        this.idVuelo = idVuelo;
    }

    public String getNumeroVuelo() {
        return numeroVuelo;
    }

    public void setNumeroVuelo(String numeroVuelo) {
        this.numeroVuelo = numeroVuelo;
    }

    public int getIdAerolinea() {
        return idAerolinea;
    }

    public void setIdAerolinea(int idAerolinea) {
        this.idAerolinea = idAerolinea;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public Date getFechaHoraSalida() {
        return fechaHoraSalida;
    }

    public void setFechaHoraSalida(Date fechaHoraSalida) {
        this.fechaHoraSalida = fechaHoraSalida;
    }

    public Date getFechaHoraLlegada() {
        return fechaHoraLlegada;
    }

    public void setFechaHoraLlegada(Date fechaHoraLlegada) {
        this.fechaHoraLlegada = fechaHoraLlegada;
    }

    public String getEstadoVuelo() {
        return estadoVuelo;
    }

    public void setEstadoVuelo(String estadoVuelo) {
        this.estadoVuelo = estadoVuelo;
    }

    public int getIdPuerta() {
        return idPuerta;
    }

    public void setIdPuerta(int idPuerta) {
        this.idPuerta = idPuerta;
    }
}
