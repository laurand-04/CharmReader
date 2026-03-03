package com.tfg.charmreader.data.repository.priv.estanteria;

import com.tfg.charmreader.data.model.Estanteria;
import com.tfg.charmreader.data.network.API.API;
import com.tfg.charmreader.data.network.interfacesAPI.I_ApiEstanteria;
import com.tfg.charmreader.data.network.interfacesAPI.I_ApiLibrosDeUsuario;

import java.util.List;
import retrofit2.Callback;

public class EstanteriaRepository {
    private final I_ApiEstanteria api;

    public EstanteriaRepository() {
        this.api = API.getInstancia().create(I_ApiEstanteria.class);
    }

    public void obtenerEstanteriasDeUsuario(int idUsuario, Callback<List<Estanteria>> callback) {
        api.obtenerEstanteriasDeUsuario(idUsuario).enqueue(callback);
    }

    public void eliminarEstanteria(int idEstanteria, Callback<Boolean> callback) {
        api.eliminarEstanteria(idEstanteria).enqueue(callback);
    }

    public void guardarEstanteria(Estanteria estanteria, Callback<Estanteria> callback) {
        api.anadirEstanteria(estanteria).enqueue(callback);
    }
}