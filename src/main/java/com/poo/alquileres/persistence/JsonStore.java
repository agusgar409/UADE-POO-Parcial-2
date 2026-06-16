package com.poo.alquileres.persistence;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Almacén genérico que lee/escribe una List&lt;T&gt; en un archivo JSON bajo la carpeta data/.
 */
public class JsonStore<T> {

    private static final String DATA_DIR = "data";

    private final Path filePath;
    private final Type listType;
    private final Gson gson;

    /**
     * @param fileName nombre del archivo (ej. "clientes.json")
     * @param listType tipo de la lista (new TypeToken&lt;List&lt;Cliente&gt;&gt;(){}.getType())
     * @param gson     instancia de Gson configurada
     */
    public JsonStore(String fileName, Type listType, Gson gson) {
        this.filePath = Paths.get(DATA_DIR, fileName);
        this.listType = listType;
        this.gson = gson;
    }

    public synchronized List<T> readAll() {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }
        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            List<T> list = gson.fromJson(reader, listType);
            return list != null ? list : new ArrayList<>();
        } catch (IOException e) {
            throw new PersistenceException("Error leyendo " + filePath, e);
        }
    }

    public synchronized void writeAll(List<T> items) {
        try {
            Files.createDirectories(filePath.getParent());
            try (Writer writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                gson.toJson(items, listType, writer);
            }
        } catch (IOException e) {
            throw new PersistenceException("Error escribiendo " + filePath, e);
        }
    }

    /** Excepción no chequeada para fallos de persistencia. */
    public static class PersistenceException extends RuntimeException {
        public PersistenceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
