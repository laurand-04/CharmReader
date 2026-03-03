package com.tfg.charmreader.viewmodel.publ.fragmentView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tfg.charmreader.data.model.BookEn;
import com.tfg.charmreader.data.model.CatalogoLectura;
import com.tfg.charmreader.data.repository.GrupoRepository;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistorialFragmentViewModel extends ViewModel {
    private final GrupoRepository repository = new GrupoRepository();

    private final MutableLiveData<List<BookEn>> libros = new MutableLiveData<>();
    private final MutableLiveData<List<CatalogoLectura>> catalogo = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isEmpty = new MutableLiveData<>(false);

    public LiveData<List<BookEn>> getLibros() { return libros; }
    public LiveData<List<CatalogoLectura>> getCatalogo() { return catalogo; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getIsEmpty() { return isEmpty; }

    public void cargarHistorial(int idGrupo) {
        isLoading.setValue(true);

        // PASO 1: Obtener fechas del catálogo
        repository.obtenerHistorialCatalogo(idGrupo, new Callback<List<CatalogoLectura>>() {
            @Override
            public void onResponse(Call<List<CatalogoLectura>> call, Response<List<CatalogoLectura>> responseCata) {
                if (responseCata.isSuccessful() && responseCata.body() != null && !responseCata.body().isEmpty()) {
                    List<CatalogoLectura> listaCata = responseCata.body();

                    // PASO 2: Obtener detalles de los libros
                    repository.obtenerHistorialLibros(idGrupo, new Callback<List<BookEn>>() {
                        @Override
                        public void onResponse(Call<List<BookEn>> c, Response<List<BookEn>> responseLibros) {
                            isLoading.postValue(false);
                            if (responseLibros.isSuccessful() && responseLibros.body() != null) {
                                catalogo.postValue(listaCata);
                                libros.postValue(responseLibros.body());
                                isEmpty.postValue(responseLibros.body().isEmpty());
                            } else {
                                isEmpty.postValue(true);
                            }
                        }
                        @Override public void onFailure(Call<List<BookEn>> c, Throwable t) {
                            isLoading.postValue(false);
                            isEmpty.postValue(true);
                        }
                    });
                } else {
                    isLoading.postValue(false);
                    isEmpty.postValue(true);
                }
            }
            @Override public void onFailure(Call<List<CatalogoLectura>> call, Throwable t) {
                isLoading.postValue(false);
                isEmpty.postValue(true);
            }
        });
    }
}