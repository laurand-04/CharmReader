package com.tfg.charmreader.data.repository.admin;

import com.tfg.charmreader.data.network.API.API;
import com.tfg.charmreader.data.network.interfacesAPI.I_ApiAdmin;
import retrofit2.Callback;

public class AdminRepository {
    private final I_ApiAdmin api;

    public AdminRepository() {
        this.api = API.getInstancia().create(I_ApiAdmin.class);
    }

    public void getTotalUsuarios(Callback<Long> cb) { api.getTotalUsuarios().enqueue(cb); }
    public void getLecturasActivas(Callback<Long> cb) { api.getLecturasActivas().enqueue(cb); }
    public void getNombreGrupoTop(Callback<String> cb) { api.getNombreGrupoTop().enqueue(cb); }
    public void getDensidad(Callback<Double> cb) { api.getDensidad().enqueue(cb); }
    public void getTiempoMedio(Callback<Double> cb) { api.getTiempoMedioLectura().enqueue(cb); }
    public void getFinalizadosMes(Callback<Long> cb) { api.getLibrosFinalizadosMes().enqueue(cb); }
}