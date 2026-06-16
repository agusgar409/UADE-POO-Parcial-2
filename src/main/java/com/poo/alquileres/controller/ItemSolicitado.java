package com.poo.alquileres.controller;

/**
 * DTO simple: un equipo (por código) y la cantidad solicitada en un alquiler.
 */
public record ItemSolicitado(String codigoEquipo, int cantidad) {
}
