package com.tfg.charmreader.viewmodel.publ.misGrupos.creados;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.tfg.charmreader.data.model.*;
import com.tfg.charmreader.data.repository.priv.proximamente.BookRepository;
import com.tfg.charmreader.data.repository.publ.InfoGrupoRepository;
import com.tfg.charmreader.data.repository.priv.proximamente.SearchRepository;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NuevoLibroPropuestoViewModel extends AndroidViewModel {
    private final SearchRepository searchRepo = new SearchRepository();
    private final BookRepository bookRepo = new BookRepository();
    private final InfoGrupoRepository grupoRepo = new InfoGrupoRepository();

    private final MutableLiveData<List<Book>> resultados = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isSaving = new MutableLiveData<>(false);
    private final MutableLiveData<String> mensaje = new MutableLiveData<>();
    private final MutableLiveData<Boolean> successAction = new MutableLiveData<>(false);

    public NuevoLibroPropuestoViewModel(@NonNull Application application) { super(application); }

    public LiveData<List<Book>> getResultados() { return resultados; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getIsSaving() { return isSaving; }
    public LiveData<String> getMensaje() { return mensaje; }
    public LiveData<Boolean> getSuccessAction() { return successAction; }

    public void buscarLibro(String query) {
        if (query.isEmpty()) { resultados.setValue(new ArrayList<>()); return; }
        isLoading.setValue(true);
        searchRepo.buscarEnOpenLibrary(query, new Callback<BookResponse>() {
            @Override
            public void onResponse(Call<BookResponse> call, Response<BookResponse> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    resultados.postValue(response.body().getBooks());
                } else { resultados.postValue(new ArrayList<>()); }
            }
            @Override public void onFailure(Call<BookResponse> call, Throwable t) {
                isLoading.postValue(false);
                resultados.postValue(new ArrayList<>());
            }
        });
    }

    public void proponerLibroAGrupo(Book book, int idGrupo) {
        int idUsuario = AuthRepository.getInstance(getApplication()).getIdUsuario();
        if (idUsuario == -1 || idGrupo == -1) {
            mensaje.setValue("Error de sesión o de grupo");
            return;
        }

        isSaving.setValue(true);
        BookEn bookEn = new BookEn(book, idUsuario, false);

        // Paso 1: Añadir/Obtener libro en nuestra DB interna
        bookRepo.anadirBook(bookEn, new Callback<BookEn>() {
            @Override
            public void onResponse(Call<BookEn> call, Response<BookEn> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Paso 2: Crear la propuesta en el catálogo
                    CatalogoLectura propuesta = new CatalogoLectura();
                    propuesta.setIdGrupo(idGrupo);
                    propuesta.setIdBook(response.body().getId());
                    propuesta.setEstado(CatalogoLectura.EstadoLectura.PROPUESTO);

                    grupoRepo.añadirLibroAlCatalogo(propuesta, new Callback<CatalogoLectura>() {
                        @Override
                        public void onResponse(Call<CatalogoLectura> c, Response<CatalogoLectura> r) {
                            isSaving.postValue(false);
                            if (r.isSuccessful()) successAction.postValue(true);
                            else mensaje.postValue("Error al proponer el libro");
                        }
                        @Override public void onFailure(Call<CatalogoLectura> c, Throwable t) {
                            isSaving.postValue(false);
                            mensaje.postValue("Error de conexión");
                        }
                    });
                } else {
                    isSaving.postValue(false);
                    mensaje.postValue("Error al procesar el libro");
                }
            }
            @Override public void onFailure(Call<BookEn> call, Throwable t) {
                isSaving.postValue(false);
                mensaje.postValue("Error de red");
            }
        });
    }
}