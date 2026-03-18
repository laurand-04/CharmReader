package com.tfg.charmreader.data.repository.publ;

import com.tfg.charmreader.data.model.Valoracion;
import com.tfg.charmreader.data.network.API.API;
import com.tfg.charmreader.data.network.interfacesAPI.I_ApiValoracion;

import java.util.List;

import retrofit2.Callback;

public class ValoracionRepository {
    private final I_ApiValoracion apiValoracion;

    public ValoracionRepository() {
        this.apiValoracion = API.getInstancia().create(I_ApiValoracion.class);
    }

    public void verValoraciones (Valoracion.TipoValoracion tipo, int id, Callback<List<Valoracion>> cb) {
        apiValoracion.verValoraciones(tipo, id).enqueue(cb);
    }

    public void verValoracionesTitulo (String titulo, Callback<List<Valoracion>> cb) {
        apiValoracion.verValoracionesTitulo(titulo).enqueue(cb);
    }

    public double calcularMedia(List<Valoracion> valoraciones) {
        if (valoraciones == null || valoraciones.isEmpty()) return 0.0;
        double suma = 0;
        for (Valoracion v : valoraciones) {
            suma += v.getCalificacion();
        }
        return suma / valoraciones.size();
    }
}
