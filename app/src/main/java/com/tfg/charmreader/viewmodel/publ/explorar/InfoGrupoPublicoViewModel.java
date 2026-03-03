package com.tfg.charmreader.viewmodel.publ.explorar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tfg.charmreader.data.model.*;
import com.tfg.charmreader.data.repository.priv.proximamente.BookRepository;
import com.tfg.charmreader.data.repository.publ.InfoGrupoRepository;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InfoGrupoPublicoViewModel extends ViewModel {
    private final InfoGrupoRepository repository = new InfoGrupoRepository();
    private final BookRepository bookRepo = new BookRepository();

    private final MutableLiveData<Boolean> esMiembro = new MutableLiveData<>(false);
    private final MutableLiveData<List<BookEn>> librosFinalizados = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Valoracion>> reseñas = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> snackbarMessage = new MutableLiveData<>();

    public LiveData<Boolean> getEsMiembro() { return esMiembro; }
    public LiveData<List<BookEn>> getLibrosFinalizados() { return librosFinalizados; }
    public LiveData<List<Valoracion>> getReseñas() { return reseñas; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getSnackbarMessage() { return snackbarMessage; }

    public void verificarPertenencia(int idG, int idULog) {
        repository.obtenerMiembros(idG, new Callback<List<Miembro>>() {
            @Override
            public void onResponse(Call<List<Miembro>> call, Response<List<Miembro>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean miembro = false;
                    for (Miembro m : response.body()) if (m.getIdUsuario() == idULog) miembro = true;
                    esMiembro.postValue(miembro);
                }
            }
            @Override public void onFailure(Call<List<Miembro>> call, Throwable t) {}
        });
    }

    public void unirseAlGrupo(int idG, int idU) {
        isLoading.setValue(true);
        repository.unirse(new Miembro(idG, idU), new Callback<Miembro>() {
            @Override public void onResponse(Call<Miembro> c, Response<Miembro> r) {
                isLoading.postValue(false);
                if (r.isSuccessful()) { esMiembro.postValue(true); snackbarMessage.postValue("¡Bienvenido al grupo!"); }
            }
            @Override public void onFailure(Call<Miembro> c, Throwable t) { isLoading.postValue(false); }
        });
    }

    public void salirDelGrupo(int idG, int idU) {
        isLoading.setValue(true);
        repository.salir(idG, idU, new Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                isLoading.postValue(false);
                if (r.isSuccessful() || r.code() == 204) { esMiembro.postValue(false); snackbarMessage.postValue("Has abandonado el grupo"); }
            }
            @Override public void onFailure(Call<Void> c, Throwable t) { isLoading.postValue(false); }
        });
    }

    public void cargarContenido(int idG) {
        isLoading.setValue(true);
        // Cargar Reseñas
        repository.obtenerReseñas(idG, new Callback<List<Valoracion>>() {
            @Override public void onResponse(Call<List<Valoracion>> c, Response<List<Valoracion>> r) {
                if (r.isSuccessful()) reseñas.postValue(r.body());
            }
            @Override public void onFailure(Call<List<Valoracion>> c, Throwable t) {}
        });

        // Cargar Catálogo (Libros Finalizados)
        repository.obtenerCatalogo(idG, new Callback<List<CatalogoLectura>>() {
            @Override public void onResponse(Call<List<CatalogoLectura>> c, Response<List<CatalogoLectura>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    List<BookEn> temporal = new ArrayList<>();
                    for (CatalogoLectura cl : r.body()) {
                        if (cl.getEstado() == CatalogoLectura.EstadoLectura.FINALIZADO) {
                            bookRepo.obtenerBookPorId(cl.getIdBook(), new Callback<BookEn>() {
                                @Override public void onResponse(Call<BookEn> c2, Response<BookEn> r2) {
                                    if (r2.isSuccessful()) {
                                        temporal.add(r2.body());
                                        librosFinalizados.postValue(new ArrayList<>(temporal));
                                    }
                                }
                                @Override public void onFailure(Call<BookEn> c2, Throwable t2) {}
                            });
                        }
                    }
                }
                isLoading.postValue(false);
            }
            @Override public void onFailure(Call<List<CatalogoLectura>> c, Throwable t) { isLoading.postValue(false); }
        });
    }
}