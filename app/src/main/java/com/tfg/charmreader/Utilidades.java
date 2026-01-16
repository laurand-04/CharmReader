package com.tfg.charmreader;

import android.util.Log;

import com.tfg.charmreader.interfacesAPI.I_ApiLibro;
import com.tfg.charmreader.interfacesAPI.I_ApiLibrosDeUsuario;
import com.tfg.charmreader.interfacesAPI.I_ApiUsuario;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.Usuario;

import retrofit2.Response;

public class Utilidades {

    public static I_ApiLibro apiLibro = API.getInstancia().create(I_ApiLibro.class);
    public static  I_ApiLibrosDeUsuario apiLibrosDeUsuario = API.getInstancia().create(I_ApiLibrosDeUsuario.class);
    public static I_ApiUsuario apiUsuario = API.getInstancia().create(I_ApiUsuario.class);

    public static int obtenerIdUsuarioDesdeAPI(String correo) {
        try {
            Response<Usuario> resp = apiUsuario.getIdUsuarioPorCorreo(correo).execute(); // llama a tu API
            if (resp.isSuccessful() && resp.body() != null) {
                int idUsuario = resp.body().getId();
                Log.e("PROGRESO", "El id del usuario es: " + idUsuario);
                //Toast.makeText(this, "El id del usuario es: " + idUsuario, Toast.LENGTH_SHORT).show();
                return idUsuario; // devuelve el ID del usuario
            } else {
                Log.e("API_ERROR_USUARIO (Clase:CargarNuevoLibro, metodo:obtenerIdUsuarioDesdeAPI)", "Código: " + resp.code());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1; // devuelve -1 si hubo error
    }




}
