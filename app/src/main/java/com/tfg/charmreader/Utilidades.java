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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Utilidades {

    public static I_ApiLibro apiLibro =
            API.getInstancia().create(I_ApiLibro.class);
    public static I_ApiLibrosDeUsuario apiLibrosDeUsuario =
            API.getInstancia().create(I_ApiLibrosDeUsuario.class);
    public static I_ApiUsuario apiUsuario =
            API.getInstancia().create(I_ApiUsuario.class);
}
