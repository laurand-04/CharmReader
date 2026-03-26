package com.tfg.charmreader.data.repository;

import android.net.Uri;

import com.cloudinary.android.callback.UploadCallback;
import com.tfg.charmreader.data.model.BookEn;
import com.tfg.charmreader.data.model.CatalogoLectura;
import com.tfg.charmreader.data.model.GrupoLectura;
import com.tfg.charmreader.data.network.API.API;
import com.tfg.charmreader.data.network.API.CloudinaryClient;
import com.tfg.charmreader.data.network.interfacesAPI.I_APICatalogo;
import com.tfg.charmreader.data.network.interfacesAPI.I_ApiGrupoLectura;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class GrupoRepository {
    private final I_ApiGrupoLectura apiGrupoLectura;
    private I_APICatalogo apiCatalogo = API.getInstancia().create(I_APICatalogo.class);

    public GrupoRepository() {
        this.apiGrupoLectura = API.getInstancia().create(I_ApiGrupoLectura.class);
    }

    public void obtenerGrupos(Callback<List<GrupoLectura>> callback) {
        apiGrupoLectura.obtenerGrupos().enqueue(callback);
    }

    public void eliminarGrupo(int idGrupo, Callback<Void> callback) {
        apiGrupoLectura.eliminarGrupo(idGrupo).enqueue(callback);
    }

    public void crearGrupo(GrupoLectura grupo, Callback<GrupoLectura> cb) {
        apiGrupoLectura.crearGrupo(grupo).enqueue(cb);
    }

    public void actualizarGrupo(GrupoLectura grupo, Callback<GrupoLectura> cb) {
        apiGrupoLectura.actualizar(grupo).enqueue(cb);
    }

    public void buscarPorNombre(String query, Callback<GrupoLectura> cb) {
        apiGrupoLectura.buscarGrupoPorNombre(query).enqueue(cb);
    }

    public void obtenerGrupoPorId(int idGrupo, Callback<GrupoLectura> cb) {
        apiGrupoLectura.obtenerGrupoPorId(idGrupo).enqueue(cb);
    }

    public void obtenerHistorialCatalogo(int idGrupo, Callback<List<CatalogoLectura>> cb) {
        apiCatalogo.obtenerHistorial2(idGrupo).enqueue(cb);
    }

    public void obtenerHistorialLibros(int idGrupo, Callback<List<BookEn>> cb) {
        apiCatalogo.obtenerHistorial(idGrupo).enqueue(cb);
    }

    public void obtenerLibroPropuestas(int idGrupo, Callback<List<BookEn>> cb) {
        apiCatalogo.obtenerLibroPropuestas(idGrupo).enqueue(cb);
    }

    public void eliminarPropuesta(int idGrupo, int idLibro, Callback<Void> cb) {
        apiCatalogo.eliminarPropuesta(idGrupo, idLibro).enqueue(cb);
    }

    public void obtenerGruposPorAdmin(int idUsuario, Callback<List<GrupoLectura>> callback) {
        apiGrupoLectura.obtenerGruposPorAdmin(idUsuario).enqueue(callback);
    }

    public void gestionarSalidaAdmin(int idGrupo, int idUsuario, Callback<ResponseBody> callback) {
        apiGrupoLectura.gestionarSalidaAdmin(idGrupo, idUsuario).enqueue(callback);
    }

    public void subirImagen(Uri uri, UploadCallback callback) {
        CloudinaryClient.nuevoUpload(uri).callback(callback).dispatch();
    }
}