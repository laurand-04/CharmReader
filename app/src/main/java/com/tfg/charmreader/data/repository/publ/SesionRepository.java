package com.tfg.charmreader.data.repository.publ;

import com.tfg.charmreader.data.model.Sesion;
import com.tfg.charmreader.data.network.API.API;
import com.tfg.charmreader.data.network.interfacesAPI.I_ApiSesion;

import java.util.List;
import retrofit2.Callback;

public class SesionRepository {
    private final I_ApiSesion api;

    public SesionRepository() {
        this.api = API.getInstancia().create(I_ApiSesion.class);
    }

    public void obtenerSesiones(int idGrupo, Callback<List<Sesion>> cb) {
        api.verSesiones(idGrupo).enqueue(cb);
    }

    public void eliminarSesion(int idGrupo, String fecha, String hora, Callback<Void> cb) {
        api.eliminarSesion(idGrupo, fecha, hora).enqueue(cb);
    }


    public void nuevaSesion(Sesion nueva, Callback<Sesion> callback) {
        api.nuevaSesion(nueva).enqueue(callback);
    }

    public void obtenerProximaSesion(int idGrupo, Callback<Sesion> callback) {
        api.obtenerProximaSesion(idGrupo).enqueue(callback);
    }
}