package com.tfg.charmreader.data.repository.publ;

import com.tfg.charmreader.data.model.Usuario;
import com.tfg.charmreader.data.network.API.API;
import com.tfg.charmreader.data.network.interfacesAPI.I_ApiUsuario;

import retrofit2.Callback;

public class UserRepository {
    private static final I_ApiUsuario apiUsuario = API.getInstancia().create(I_ApiUsuario.class);

    public void obtenerUsuarioPorId (int id, Callback<Usuario> callback){
        apiUsuario.obtenerUsuarioPorId(id).enqueue(callback);
    }
}
