package com.poo.alquileres.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.poo.alquileres.model.Alquiler;
import com.poo.alquileres.model.AlquilerComun;
import com.poo.alquileres.model.AlquilerCorporativo;
import com.poo.alquileres.model.AlquilerMasivo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Construye instancias de Gson configuradas para el dominio: adaptadores de fecha y
 * polimorfismo de la jerarquía Alquiler.
 */
public final class GsonFactory {

    private GsonFactory() {
    }

    /** Gson base con soporte para java.time y formato legible. */
    public static Gson base() {
        return baseBuilder().create();
    }

    /** Gson que además resuelve los subtipos de Alquiler mediante el campo "tipo". */
    public static Gson conAlquileres() {
        RuntimeTypeAdapterFactory<Alquiler> alquilerFactory =
                RuntimeTypeAdapterFactory.of(Alquiler.class, "tipo")
                        .registerSubtype(AlquilerComun.class, "COMUN")
                        .registerSubtype(AlquilerMasivo.class, "MASIVO")
                        .registerSubtype(AlquilerCorporativo.class, "CORPORATIVO");

        return baseBuilder()
                .registerTypeAdapterFactory(alquilerFactory)
                .create();
    }

    private static GsonBuilder baseBuilder() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
    }
}
