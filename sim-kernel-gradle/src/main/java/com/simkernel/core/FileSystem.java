package com.simkernel.core;

import com.simkernel.process.Proceso;
import java.util.*;

public class FileSystem {
    public static class ArchivoSimulado {
        private final String nombre;
        private Proceso procesoActual;
        private final Queue<Proceso> colaEspera = new LinkedList<>();
        private final Semaphore semaforo = new Semaphore(1);

        public ArchivoSimulado(String nombre) {
            this.nombre = nombre;
        }

        public boolean solicitar(Proceso p) {
            if (semaforo.acquire()) {
                procesoActual = p;
                return true;
            } else {
                colaEspera.add(p);
                return false;
            }
        }

        public void liberar() {
            if (!colaEspera.isEmpty()) {
                procesoActual = colaEspera.poll();
            } else {
                procesoActual = null;
                semaforo.release();
            }
        }

        public String getNombre() { return nombre; }
        public Proceso getProcesoActual() { return procesoActual; }
        public Queue<Proceso> getColaEspera() { return colaEspera; }
        public Semaphore getSemaforo() { return semaforo; }
    }

    private final List<ArchivoSimulado> archivos = new ArrayList<>();

    public FileSystem(List<String> nombresArchivos) {
        for (String nombre : nombresArchivos) {
            archivos.add(new ArchivoSimulado(nombre));
        }
    }

    public ArchivoSimulado getArchivo(String nombre) {
        for (ArchivoSimulado a : archivos) {
            if (a.getNombre().equals(nombre)) return a;
        }
        return null;
    }

    public List<ArchivoSimulado> getArchivos() {
        return archivos;
    }
}

