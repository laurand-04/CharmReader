package com.tfg.charmreader;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tfg.charmreader.interfacesAPI.I_ApiLibro;
import com.tfg.charmreader.interfacesAPI.I_ApiLibrosDeUsuario;
import com.tfg.charmreader.interfacesAPI.I_ApiUsuario;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.Usuario;

import java.io.IOException;

import retrofit2.Response;

public class Utilidades {

    public static I_ApiLibro apiLibro =
            API.getInstancia().create(I_ApiLibro.class);
    public static I_ApiLibrosDeUsuario apiLibrosDeUsuario =
            API.getInstancia().create(I_ApiLibrosDeUsuario.class);
    public static I_ApiUsuario apiUsuario =
            API.getInstancia().create(I_ApiUsuario.class);

    // 🔹 CALLBACK
    public interface IdUsuarioCallback {
        void onIdCargado(int idUsuario);
    }
    public static int obtenerIdUsuarioDesdeAPI() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null || firebaseUser.getEmail() == null) {
            Log.e("AUTH", "Usuario Firebase nulo");
            return -1;
        }
        return getIdUsuarioPorCorreo(firebaseUser.getEmail());
    }
    private static int getIdUsuarioPorCorreo(String correo){
        try {
            Response<Usuario> response = apiUsuario.getIdUsuarioPorCorreo(correo).execute();
            if (response.isSuccessful() && response.body() != null) {
                Log.d("PROGRESO", "ID obtenido getIdUsuarioPorCorreo: " + response.body().getId());
                return response.body().getId();
            } else {
                Log.e("API_ERROR", "Respuesta inválida en getIdUsuarioPorCorreo");
            }
        } catch (IOException e) {
            Log.d("Error clase utilidad", "problema en getIdUsuarioPorCorreo");
            throw new RuntimeException(e);
        }
        return -1;
    }
}
