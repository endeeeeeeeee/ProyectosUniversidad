// Clase base para Proceso

package com.simkernel.process;

import com.simkernel.security.Usuario;

public class Proceso {
    public enum Estado {
        NUEVO, LISTO, EJECUTANDO, BLOQUEADO, TERMINADO
    }

    private final int pid;
    private final String nombre;
    private Estado estado;
    private int tiempoRestante;
    private int memoriaNecesaria;
    private int prioridad;
    private final Usuario usuario;
    private String archivoDestino;

    public Proceso(int pid, String nombre, int tiempoRestante, int memoriaNecesaria, int prioridad, Usuario usuario) {
        this.pid = pid;
        this.nombre = nombre;
        this.estado = Estado.NUEVO;
        this.tiempoRestante = tiempoRestante;
        this.memoriaNecesaria = memoriaNecesaria;
        this.prioridad = prioridad;
        this.usuario = usuario;
    }

    // Getters y Setters
    public int getPid() { return pid; }
    public String getNombre() { return nombre; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
    public int getTiempoRestante() { return tiempoRestante; }
    public void setTiempoRestante(int tiempoRestante) { this.tiempoRestante = tiempoRestante; }
    public int getMemoriaNecesaria() { return memoriaNecesaria; }
    public int getPrioridad() { return prioridad; }
    public Usuario getUsuario() { return usuario; }
    public String getArchivoDestino() { return archivoDestino; }
    public void setArchivoDestino(String archivoDestino) { this.archivoDestino = archivoDestino; }

    @Override
    public String toString() {
        return "[PID=" + pid + ", Estado=" + estado + ", Nombre=" + nombre + "]";
    }

    public int getTiempoCpu() {
        return tiempoRestante;
    }

}
