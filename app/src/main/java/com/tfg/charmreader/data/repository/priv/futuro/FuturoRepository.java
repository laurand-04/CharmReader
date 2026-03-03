package com.tfg.charmreader.data.repository.priv.futuro;

import com.tfg.charmreader.data.model.CCLibrosSinEstrenar;
import com.tfg.charmreader.data.model.LibrosSinEstrenar;
import com.tfg.charmreader.data.network.API.API;
import com.tfg.charmreader.data.network.interfacesAPI.I_ApiLibrosSinEstrenar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Callback;

public class FuturoRepository {
    private final I_ApiLibrosSinEstrenar api;

    public FuturoRepository() {
        this.api = API.getInstancia().create(I_ApiLibrosSinEstrenar.class);
    }

    public void actualizarLibro(LibrosSinEstrenar libro, Callback<LibrosSinEstrenar> callback) {
        api.guardarLibrosSinEstrenar(libro).enqueue(callback);
    }

    public void obtenerLibrosPorUsuario(int idUsuario, Callback<ArrayList<LibrosSinEstrenar>> cb) {
        api.getLibrosSinEstrenarPorUsuario(idUsuario).enqueue(cb);
    }

    public void eliminarLibro(CCLibrosSinEstrenar idLanzamiento, Callback<String> cb) {
        api.eliminarLibrosSinEstrenar(idLanzamiento).enqueue(cb);
    }
}