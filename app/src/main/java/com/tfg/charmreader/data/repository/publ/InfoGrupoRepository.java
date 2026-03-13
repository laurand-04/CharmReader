package com.tfg.charmreader.data.repository.publ;

import com.tfg.charmreader.data.model.BookEn;
import com.tfg.charmreader.data.model.CatalogoLectura;
import com.tfg.charmreader.data.model.GrupoLectura;
import com.tfg.charmreader.data.model.Miembro;
import com.tfg.charmreader.data.model.Valoracion;
import com.tfg.charmreader.data.network.API.API;
import com.tfg.charmreader.data.network.interfacesAPI.I_APICatalogo;
import com.tfg.charmreader.data.network.interfacesAPI.I_ApiMiembro;
import com.tfg.charmreader.data.network.interfacesAPI.I_ApiValoracion;

import java.util.List;
import retrofit2.Callback;

public class InfoGrupoRepository {
    private final I_ApiMiembro apiMiembro = API.getInstancia().create(I_ApiMiembro.class);
    private final I_APICatalogo apiCatalogo = API.getInstancia().create(I_APICatalogo.class);
    private final I_ApiValoracion apiValoracion = API.getInstancia().create(I_ApiValoracion.class);

    public void obtenerMiembros(int idGrupo, Callback<List<Miembro>> cb) { apiMiembro.obtenerPorGrupo(idGrupo).enqueue(cb); }
    public void unirse(Miembro m, Callback<Miembro> cb) { apiMiembro.unirse(m).enqueue(cb); }
    public void salir(int idG, int idU, Callback<Void> cb) { apiMiembro.salirDeGrupo(idG, idU).enqueue(cb); }
    public void obtenerCatalogo(int idG, Callback<List<CatalogoLectura>> cb) { apiCatalogo.verCatalogo(idG).enqueue(cb); }
    public void obtenerReseñas(int idG, Callback<List<Valoracion>> cb) {
        apiValoracion.verValoraciones(Valoracion.TipoValoracion.GRUPO, idG).enqueue(cb);
    }
    public void cerrarVotaciones(int idGrupo, Callback<Void> cb){
        apiCatalogo.cerrarVotaciones(idGrupo).enqueue(cb);
    }
    public void añadirLibroAlCatalogo(CatalogoLectura propuesta, Callback<CatalogoLectura> callback) {
        apiCatalogo.añadirLibro(propuesta).enqueue(callback);
    }
    public void obtenerLibroActual(int idGrupo, Callback<BookEn> callback) {
        apiCatalogo.obtenerLibroActual(idGrupo).enqueue(callback);
    }
    public void obtenerLibroPropuestas(int idGrupo, Callback<List<BookEn>> callback) {
        apiCatalogo.obtenerLibroPropuestas(idGrupo).enqueue(callback);
    }
    public void obtenerHistorial(int idGrupo, Callback<List<BookEn>> callback) {
        apiCatalogo.obtenerHistorial(idGrupo).enqueue(callback);
    }

    public void obtenerGruposDondeEsMiembro(int idUsuario, Callback<List<GrupoLectura>> callback) {
        apiMiembro.obtenerGruposDondeEsMiembro(idUsuario).enqueue(callback);
    }
}