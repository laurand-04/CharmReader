package com.tfg.charmreader.data.repository.priv.tusObras;

import com.tfg.charmreader.data.model.Obras;
import com.tfg.charmreader.data.network.API.API;
import com.tfg.charmreader.data.network.interfacesAPI.I_ApiObras;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Callback;

public class ObrasRepository {

    private final I_ApiObras apiObras;

    public ObrasRepository() {
        this.apiObras = API.getInstancia().create(I_ApiObras.class);
    }

    public void guardarObra(Obras nuevaObra, Callback<Obras> callback) {
        apiObras.guardarObra(nuevaObra).enqueue(callback);
    }

    public void obtenerObrasDeUsuario(int idUsuario, Callback<List<Obras>> callback) {
        apiObras.obtenerObrasDeUsuario(idUsuario).enqueue(callback);
    }

    public void eliminarObra(int idObra, Callback<ResponseBody> callback) {
        apiObras.eliminarObra(idObra).enqueue(callback);
    }
}