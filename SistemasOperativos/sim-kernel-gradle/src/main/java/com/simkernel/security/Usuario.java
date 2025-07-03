package com.simkernel.security;

import java.util.HashSet;
import java.util.Set;

public class Usuario {
    private final String nombre;
    private final Set<String> permisos;

    public Usuario(String nombre) {
        this.nombre = nombre;
        this.permisos = new HashSet<>();
    }

    public String getNombre() {
        return nombre;
    }

    public void agregarPermiso(String recurso) {
        permisos.add(recurso);
    }

    public boolean tienePermiso(String recurso) {
        return permisos.contains(recurso);
    }
}

