package com.simkernel.core;

import com.simkernel.process.Proceso;
import java.util.Queue;

public interface RecursoCompartido {
    boolean solicitar(Proceso p);
    void liberar();
    Proceso getProcesoActual();
    Queue<Proceso> getColaEspera();
    String getNombre();
}

