package com.simkernel.core;

import com.simkernel.process.Proceso;
import java.util.LinkedList;
import java.util.Queue;

public class Archivo implements RecursoCompartido {
    private Proceso procesoActual = null;
    private final Queue<Proceso> colaEspera = new LinkedList<>();
    private final Semaphore semaforo = new Semaphore(1);

    @Override
    public boolean solicitar(Proceso p) {
        if (semaforo.acquire()) {
            procesoActual = p;
            return true;
        } else {
            colaEspera.add(p);
            return false;
        }
    }

    @Override
    public void liberar() {
        if (!colaEspera.isEmpty()) {
            procesoActual = colaEspera.poll();
            // El semáforo sigue ocupado porque el recurso pasa al siguiente proceso
        } else {
            procesoActual = null;
            semaforo.release(); // Solo se libera el semáforo si no hay más procesos esperando
        }
    }

    @Override
    public Proceso getProcesoActual() {
        return procesoActual;
    }

    @Override
    public Queue<Proceso> getColaEspera() {
        return colaEspera;
    }

    @Override
    public String getNombre() {
        return "Archivo";
    }

    public Semaphore getSemaforo() {
        return semaforo;
    }
}
