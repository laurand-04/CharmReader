package com.tfg.charmreader.utils;

import com.tfg.charmreader.data.network.interfacesAPI.I_ApiLibro;
import com.tfg.charmreader.data.network.interfacesAPI.I_ApiLibrosDeUsuario;
import com.tfg.charmreader.data.network.interfacesAPI.I_ApiUsuario;
import com.tfg.charmreader.data.network.API.API;

public class Utilidades {

    public static I_ApiLibro apiLibro =
            API.getInstancia().create(I_ApiLibro.class);
    public static I_ApiLibrosDeUsuario apiLibrosDeUsuario =
            API.getInstancia().create(I_ApiLibrosDeUsuario.class);
    public static I_ApiUsuario apiUsuario =
            API.getInstancia().create(I_ApiUsuario.class);
}
