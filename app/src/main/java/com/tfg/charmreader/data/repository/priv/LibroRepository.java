package com.tfg.charmreader.data.repository.priv;

import com.tfg.charmreader.data.model.Libro;
import com.tfg.charmreader.data.model.LibrosDeUsuario;
import com.tfg.charmreader.data.network.API.API;
import com.tfg.charmreader.data.network.interfacesAPI.I_ApiLibro;
import com.tfg.charmreader.data.network.interfacesAPI.I_ApiLibrosDeUsuario;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Callback;

public class LibroRepository {
    private final I_ApiLibrosDeUsuario apiLibrosDeUsuario;
    private final I_ApiLibro apiLibro;

    public LibroRepository() {
        this.apiLibrosDeUsuario = API.getInstancia().create(I_ApiLibrosDeUsuario.class);
        this.apiLibro = API.getInstancia().create(I_ApiLibro.class);
    }

    public void obtenerLibrosSinEstanteria(int idUsuario, Callback<List<LibrosDeUsuario>> callback) {
        // En tu lógica, estantería 0 significa "sin estantería"
        apiLibrosDeUsuario.obtenerLibrosDeEstanteria(0, idUsuario).enqueue(callback);
    }

    public void obtenerDetallesLibros(List<Integer> ids, Callback<List<Libro>> callback) {
        apiLibro.obtenerLibrosPorIds(ids).enqueue(callback);
    }

    public void asignarAEstanteria(int idU, int idL, int idEst, Callback<Boolean> callback) {
        apiLibrosDeUsuario.asignarLibroAEstanteria(idU, idL, idEst).enqueue(callback);
    }

    public void obtenerRelacionesPorEstanteria(int idEst, int idU, Callback<List<LibrosDeUsuario>> cb) {
        apiLibrosDeUsuario.obtenerLibrosDeEstanteria(idEst, idU).enqueue(cb);
    }

    public void desvincularLibro(int idU, int idL, Callback<Boolean> cb) {
        apiLibrosDeUsuario.desvincularLibroDeEstanteria(idU, idL).enqueue(cb);
    }

    public void actualizarProgreso(LibrosDeUsuario libro, Callback<LibrosDeUsuario> callback) {
        // Usamos el servicio de la API de relaciones (I_ApiLibrosDeUsuario)
        // El métod0 'guardarProgreso' en tu API recibe el objeto completo
        apiLibrosDeUsuario.guardarProgreso(libro).enqueue(callback);
    }

    public void anadirLibro(Libro libro, Callback<Libro> callback) {
        apiLibro.añadirLibro(libro).enqueue(callback);
    }

    public void obtenerProgreso(int idU, int idL, Callback<LibrosDeUsuario> callback) {
        apiLibrosDeUsuario.getLibrodeUsuario(idU, idL).enqueue(callback);

    }

    public void contarLibrosEnEstanteria(int idEstanteria, Callback<Integer> callback) {
        apiLibrosDeUsuario.contarLibrosEnEstanteria(idEstanteria).enqueue(callback);
    }

    public void obtenerHistorialLectura(int usuarioId, Callback<java.util.List<com.tfg.charmreader.data.model.LibrosDeUsuario>> callback) {
        apiLibrosDeUsuario.obtenerLibrosDeUsuario(usuarioId).enqueue(callback);
    }

    public void obtenerLibrosPorIds(List<Integer> ids, Callback<List<Libro>> callback) {
        apiLibro.obtenerLibrosPorIds(ids).enqueue(callback);
    }

    public void eliminarLibro(int idU, int idL, Callback<ResponseBody> callback) {
        apiLibrosDeUsuario.eliminarLibro(idU, idL).enqueue(callback);

    }
}